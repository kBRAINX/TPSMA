//(Agent réactif simple)
package agents;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.util.ArrayList;
import java.util.List;

import model.Livre;

public class Acheteur extends Agent {
    private String titreLivreRecherche;

    @Override
    protected void setup() {
        // Récupération des arguments
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            titreLivreRecherche = (String) args[0];
            System.out.println("Agent acheteur " + getLocalName() + " recherche le livre: " + titreLivreRecherche);

            // Notifier l'observateur de la création de l'acheteur
            informerObservateur("ACHETEUR_NOUVEAU:" + titreLivreRecherche);

            // Démarrage du comportement de recherche
            addBehaviour(new RechercherLivre());
        } else {
            System.out.println("Pas de livre spécifié pour l'agent acheteur " + getLocalName());
            doDelete();
        }
    }

    @Override
    protected void takeDown() {
        // Notifier l'observateur de la suppression de l'acheteur
        informerObservateur("ACHETEUR_FIN:");
        System.out.println("Agent acheteur " + getLocalName() + " s'est terminé.");
    }

    private void informerObservateur(String message) {
        // Rechercher l'agent observateur
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("observateur");
        template.addServices(sd);

        try {
            DFAgentDescription[] result = DFService.search(this, template);
            if (result.length > 0) {
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.addReceiver(result[0].getName());
                msg.setContent(message);
                send(msg);
            }
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    // Comportement de recherche de livre (OneShot car réactif simple)
    private class RechercherLivre extends OneShotBehaviour {
        @Override
        public void action() {
            // Recherche des vendeurs dans le DF
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("vente-livres");
            template.addServices(sd);

            try {
                DFAgentDescription[] result = DFService.search(myAgent, template);
                System.out.println("Agent acheteur " + getLocalName() + " a trouvé " + result.length + " vendeurs.");

                if (result.length > 0) {
                    // Envoi de demandes à tous les vendeurs
                    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                    for (DFAgentDescription dfad : result) {
                        cfp.addReceiver(dfad.getName());
                    }
                    cfp.setContent(titreLivreRecherche);
                    cfp.setConversationId("vente-livres");
                    cfp.setReplyWith("cfp-" + System.currentTimeMillis());
                    myAgent.send(cfp);

                    // Attente des réponses
                    myAgent.addBehaviour(new RecevoirPropositions(cfp.getReplyWith(), result.length));
                } else {
                    System.out.println("Aucun vendeur trouvé !");
                    doDelete();
                }
            } catch (FIPAException e) {
                e.printStackTrace();
            }
        }
    }

    // Comportement pour recevoir les propositions des vendeurs
    private class RecevoirPropositions extends OneShotBehaviour {
        private String conversationId;
        private int nombreVendeurs;
        private List<ACLMessage> propositions = new ArrayList<>();

        public RecevoirPropositions(String conversationId, int nombreVendeurs) {
            this.conversationId = conversationId;
            this.nombreVendeurs = nombreVendeurs;
        }

        @Override
        public void action() {
            // Template pour filtrer les messages
            MessageTemplate mt = MessageTemplate.and(
                MessageTemplate.MatchConversationId("vente-livres"),
                MessageTemplate.MatchInReplyTo(conversationId)
            );

            // Attente des réponses pendant un délai raisonnable
            long startTime = System.currentTimeMillis();
            long timeout = 5000;

            while (System.currentTimeMillis() - startTime < timeout && propositions.size() < nombreVendeurs) {
                ACLMessage reply = myAgent.receive(mt);
                if (reply != null) {
                    if (reply.getPerformative() == ACLMessage.PROPOSE) {
                        // Stockage des propositions
                        propositions.add(reply);
                        try {
                            Livre livre = (Livre) reply.getContentObject();
                            System.out.println(getLocalName() + " a reçu une proposition de " +
                                reply.getSender().getLocalName() +
                                " pour " + livre.getTitre() +
                                " à " + livre.getPrix() + "€");
                            informerObservateur("ACHETEUR_INFO:Reçoit proposition de " +
                                reply.getSender().getLocalName() +
                                " pour " + livre.getTitre() +
                                " à " + livre.getPrix() + "€");
                        } catch (UnreadableException e) {
                            e.printStackTrace();
                        }
                    } else {
                        System.out.println(getLocalName() + " a reçu un refus de " + reply.getSender().getLocalName());
                    }
                } else {
                    block(1000);
                }
            }

            // Traitement des propositions reçues
            if (!propositions.isEmpty()) {
                myAgent.addBehaviour(new ChoisirMeilleureOffre(propositions));
            } else {
                System.out.println("Aucune proposition reçue pour " + titreLivreRecherche);
                doDelete();
            }
        }
    }

    // Comportement pour choisir la meilleure offre
    private class ChoisirMeilleureOffre extends OneShotBehaviour {
        private List<ACLMessage> propositions;

        public ChoisirMeilleureOffre(List<ACLMessage> propositions) {
            this.propositions = propositions;
        }

        @Override
        public void action() {
            ACLMessage meilleureProp = null;
            Livre meilleurLivre = null;
            double meilleurPrix = Double.MAX_VALUE;

            // Recherche de la proposition avec le prix le plus bas
            for (ACLMessage proposition : propositions) {
                try {
                    Livre livre = (Livre) proposition.getContentObject();
                    if (livre.getPrix() < meilleurPrix) {
                        meilleurPrix = livre.getPrix();
                        meilleureProp = proposition;
                        meilleurLivre = livre;
                    }
                } catch (UnreadableException e) {
                    e.printStackTrace();
                }
            }

            if (meilleureProp != null && meilleurLivre != null) {
                System.out.println(getLocalName() + " a choisi la proposition de " +
                    meilleureProp.getSender().getLocalName() +
                    " pour " + meilleurLivre.getTitre() +
                    " à " + meilleurLivre.getPrix() + "€");
                informerObservateur("ACHETEUR_INFO:Choisit l'offre de " +
                    meilleureProp.getSender().getLocalName() +
                    " pour " + meilleurLivre.getTitre() +
                    " à " + meilleurLivre.getPrix() + "€");

                // Envoi de l'acceptation au vendeur choisi
                ACLMessage accept = meilleureProp.createReply();
                accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                try {
                    accept.setContentObject(meilleurLivre);
                    myAgent.send(accept);

                    // Attente de la confirmation
                    myAgent.addBehaviour(new RecevoirConfirmation(accept.getReplyWith()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("Impossible de choisir une proposition");
                doDelete();
            }
        }
    }

    // Comportement pour recevoir la confirmation d'achat
    private class RecevoirConfirmation extends OneShotBehaviour {
        private String replyWith;

        public RecevoirConfirmation(String replyWith) {
            this.replyWith = replyWith;
        }

        @Override
        public void action() {
            // Template pour filtrer les messages
            MessageTemplate mt = MessageTemplate.and(
                MessageTemplate.MatchConversationId("vente-livres"),
                MessageTemplate.MatchInReplyTo(replyWith)
            );

            // Attente de la réponse avec un timeout
            long startTime = System.currentTimeMillis();
            long timeout = 5000; // 5 secondes
            boolean reponseRecue = false;

            while (System.currentTimeMillis() - startTime < timeout && !reponseRecue) {
                ACLMessage reply = myAgent.receive(mt);
                if (reply != null) {
                    reponseRecue = true;

                    if (reply.getPerformative() == ACLMessage.INFORM) {
                        // Transaction réussie
                        System.out.println("🎉 " + getLocalName() + " est satisfait de l'achat du livre " +
                            titreLivreRecherche + " chez " + reply.getSender().getLocalName());
                        informerObservateur("ACHETEUR_INFO:🎉 Est satisfait de l'achat de " +
                            titreLivreRecherche + " chez " + reply.getSender().getLocalName());
                    } else {
                        // Échec de la transaction
                        System.out.println("❌ " + getLocalName() + " n'a pas pu acheter le livre " +
                            titreLivreRecherche + " chez " + reply.getSender().getLocalName());
                        informerObservateur("ACHETEUR_INFO:❌ N'a pas pu acheter " +
                            titreLivreRecherche + " chez " + reply.getSender().getLocalName());
                    }

                    // Suppression de l'agent après l'achat
                    doDelete();
                } else {
                    block(1000); // Attente de 100ms
                }
            }

            if (!reponseRecue) {
                System.out.println("Pas de réponse reçue dans le délai imparti");
                doDelete();
            }
        }
    }
}
