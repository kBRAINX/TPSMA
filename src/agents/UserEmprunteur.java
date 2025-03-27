package agents;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * UserEmprunteur - Agent à but (emprunter un livre spécifique)
 * Architecture: BDI simplifiée
 * Objectif: Emprunter un livre spécifique pour une période donnée
 */
public class UserEmprunteur extends Agent {
    private String titreLivreRecherche;
    private int nombreExemplaires;

    @Override
    protected void setup() {
        // Récupération des arguments
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            titreLivreRecherche = (String) args[0];

            // Si le nombre d'exemplaires est précisé
            if (args.length > 1) {
                nombreExemplaires = Integer.parseInt((String) args[1]);
            } else {
                nombreExemplaires = 1; // Par défaut
            }

            System.out.println("Agent emprunteur " + getLocalName() + " souhaite emprunter " +
                nombreExemplaires + " exemplaire(s) du livre: " + titreLivreRecherche);

            // Notifier l'observateur de la création de l'utilisateur
            informerObservateur("USER_NOUVEAU:Emprunteur souhaitant emprunter " + nombreExemplaires +
                " exemplaire(s) de \"" + titreLivreRecherche + "\"");

            // Démarrage du comportement d'emprunt
            addBehaviour(new EmprunterLivre());
        } else {
            System.out.println("Pas de livre spécifié pour l'agent emprunteur " + getLocalName());
            doDelete();
        }
    }

    @Override
    protected void takeDown() {
        // Notifier l'observateur de la suppression de l'utilisateur
        informerObservateur("USER_FIN:L'emprunteur a quitté la bibliothèque");
        System.out.println("Agent emprunteur " + getLocalName() + " s'est terminé.");
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

    // Comportement pour emprunter un livre
    private class EmprunterLivre extends Behaviour {
        private boolean done = false;
        private int etape = 0;
        private DFAgentDescription[] bibliothecaires;

        @Override
        public void action() {
            switch(etape) {
                case 0: // Recherche de la bibliothécaire
                    DFAgentDescription template = new DFAgentDescription();
                    ServiceDescription sd = new ServiceDescription();
                    sd.setType("service-bibliotheque");
                    template.addServices(sd);

                    try {
                        bibliothecaires = DFService.search(myAgent, template);
                        if (bibliothecaires.length > 0) {
                            System.out.println(getLocalName() + " a trouvé la bibliothécaire: " +
                                bibliothecaires[0].getName().getLocalName());
                            informerObservateur("USER_INFO:A trouvé la bibliothécaire");

                            // Envoi de la demande à la bibliothécaire
                            ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                            cfp.addReceiver(bibliothecaires[0].getName());
                            cfp.setContent(titreLivreRecherche + ":" + nombreExemplaires);
                            cfp.setConversationId("emprunt-livre");
                            myAgent.send(cfp);

                            informerObservateur("USER_INFO:Demande s'il peut emprunter " + nombreExemplaires +
                                " exemplaire(s) de \"" + titreLivreRecherche + "\"");
                            etape = 1;
                        } else {
                            System.out.println(getLocalName() + " n'a pas trouvé de bibliothécaire");
                            informerObservateur("USER_INFO:N'a pas trouvé de bibliothécaire");
                            done = true;
                        }
                    } catch (FIPAException e) {
                        e.printStackTrace();
                        done = true;
                    }
                    break;

                case 1: // Attendre la réponse "recherche en cours"
                    MessageTemplate mt1 = MessageTemplate.and(
                        MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                        MessageTemplate.MatchConversationId("recherche-emprunt")
                    );

                    ACLMessage msgRecherche = myAgent.receive(mt1);
                    if (msgRecherche != null) {
                        if (msgRecherche.getContent().equals("recherche-en-cours")) {
                            System.out.println(getLocalName() + " attend pendant que la bibliothécaire recherche le livre");
                            informerObservateur("USER_INFO:Patiente pendant la recherche du livre");
                            etape = 2;
                        }
                    } else {
                        block();
                    }
                    break;

                case 2: // Attendre le résultat de la recherche (PROPOSE ou REFUSE)
                    MessageTemplate mt2 = MessageTemplate.and(
                        MessageTemplate.or(
                            MessageTemplate.MatchPerformative(ACLMessage.PROPOSE),
                            MessageTemplate.MatchPerformative(ACLMessage.REFUSE)
                        ),
                        MessageTemplate.MatchConversationId("emprunt-livre")
                    );

                    ACLMessage msgResultat = myAgent.receive(mt2);

                    if (msgResultat != null) {
                        if (msgResultat.getPerformative() == ACLMessage.PROPOSE) {
                            // Le livre est disponible, avec sa durée d'emprunt
                            String[] parts = msgResultat.getContent().split(":");
                            int dureeMax = Integer.parseInt(parts[1]);

                            System.out.println(getLocalName() + " apprend que le livre " + titreLivreRecherche +
                                " est disponible pour " + dureeMax + " jours");
                            informerObservateur("USER_INFO:Apprend que le livre \"" + titreLivreRecherche +
                                "\" est disponible pour " + dureeMax + " jours");

                            // Demander combien de temps il peut emprunter les livres (déjà connu dans la réponse)
                            System.out.println(getLocalName() + " demande: Pour combien de temps puis-je emprunter ce livre ?");
                            informerObservateur("USER_INFO:Demande la durée d'emprunt maximale");

                            // Simuler une pause pour la conversation
                            try {
                                Thread.sleep(1500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            // Accepter l'emprunt
                            ACLMessage accept = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                            accept.addReceiver(msgResultat.getSender());
                            accept.setContent(titreLivreRecherche + ":" + nombreExemplaires);
                            accept.setConversationId("emprunt-livre");
                            myAgent.send(accept);

                            System.out.println(getLocalName() + " : " + dureeMax + " jours me conviennent parfaitement, j'accepte.");
                            informerObservateur("USER_INFO:Accepte d'emprunter le livre pour " + dureeMax + " jours");

                            etape = 3;
                        } else {
                            // Le livre n'est pas disponible
                            String content = msgResultat.getContent();

                            if (content.startsWith("exemplaires-insuffisants:")) {
                                int disponibles = Integer.parseInt(content.split(":")[1]);
                                System.out.println(getLocalName() + " apprend qu'il n'y a que " + disponibles +
                                    " exemplaire(s) disponible(s) du livre " + titreLivreRecherche);
                                informerObservateur("USER_INFO:Apprend qu'il n'y a que " + disponibles +
                                    " exemplaire(s) disponible(s) du livre \"" + titreLivreRecherche + "\"");
                            } else {
                                System.out.println(getLocalName() + " apprend que le livre " + titreLivreRecherche +
                                    " n'est pas disponible");
                                informerObservateur("USER_INFO:Apprend que le livre \"" + titreLivreRecherche +
                                    "\" n'est pas disponible");
                            }

                            // Remercier la bibliothécaire et partir
                            ACLMessage thanks = new ACLMessage(ACLMessage.INFORM);
                            thanks.addReceiver(msgResultat.getSender());
                            thanks.setContent("merci quand même");
                            myAgent.send(thanks);

                            System.out.println(getLocalName() + " : Merci quand même pour votre aide.");
                            informerObservateur("USER_INFO:Remercie la bibliothécaire et quitte la bibliothèque");

                            // Terminer le comportement
                            done = true;

                            // Supprimer l'agent après un court délai
                            myAgent.addBehaviour(new OneShotBehaviour() {
                                @Override
                                public void action() {
                                    try {
                                        Thread.sleep(2000);
                                        myAgent.doDelete();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    } else {
                        block();
                    }
                    break;

                case 3: // Attendre la confirmation de l'emprunt
                    MessageTemplate mt3 = MessageTemplate.and(
                        MessageTemplate.or(
                            MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                            MessageTemplate.MatchPerformative(ACLMessage.FAILURE)
                        ),
                        MessageTemplate.MatchConversationId("emprunt-livre")
                    );

                    ACLMessage msgConfirmation = myAgent.receive(mt3);

                    if (msgConfirmation != null) {
                        if (msgConfirmation.getPerformative() == ACLMessage.INFORM) {
                            // L'emprunt est confirmé
                            String[] parts = msgConfirmation.getContent().split(":");
                            int duree = Integer.parseInt(parts[1]);

                            System.out.println(getLocalName() + " a emprunté le livre " + titreLivreRecherche +
                                " pour " + duree + " jours");
                            informerObservateur("USER_INFO:A emprunté " + nombreExemplaires +
                                " exemplaire(s) du livre \"" + titreLivreRecherche +
                                "\" pour " + duree + " jours");

                            // Remercier la bibliothécaire
                            ACLMessage thanks = new ACLMessage(ACLMessage.INFORM);
                            thanks.addReceiver(msgConfirmation.getSender());
                            thanks.setContent("merci beaucoup");
                            myAgent.send(thanks);

                            System.out.println(getLocalName() + " : Merci beaucoup ! Je vous le rendrai dans les délais.");
                            informerObservateur("USER_INFO:Remercie la bibliothécaire et promet de rendre le livre à temps");
                        } else {
                            // L'emprunt a échoué
                            System.out.println(getLocalName() + " n'a pas pu emprunter le livre " + titreLivreRecherche);
                            informerObservateur("USER_INFO:N'a pas pu emprunter le livre \"" + titreLivreRecherche + "\"");

                            // Remercier quand même la bibliothécaire
                            ACLMessage thanks = new ACLMessage(ACLMessage.INFORM);
                            thanks.addReceiver(msgConfirmation.getSender());
                            thanks.setContent("merci quand même");
                            myAgent.send(thanks);

                            System.out.println(getLocalName() + " : Merci quand même pour votre aide.");
                            informerObservateur("USER_INFO:Remercie quand même la bibliothécaire");
                        }

                        // Terminer le comportement
                        done = true;

                        // Supprimer l'agent après un court délai
                        myAgent.addBehaviour(new OneShotBehaviour() {
                            @Override
                            public void action() {
                                try {
                                    Thread.sleep(2000);
                                    myAgent.doDelete();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    } else {
                        block();
                    }
                    break;
            }
        }

        @Override
        public boolean done() {
            return done;
        }
    }
}
