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
 * UserSimple - Agent réactif simple (recherche juste un livre)
 * Architecture: Réactive simple
 * Objectif: Vérifier si un livre est disponible dans la bibliothèque
 */
public class UserSimple extends Agent {
    private String titreLivreRecherche;

    @Override
    protected void setup() {
        // Récupération des arguments
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            titreLivreRecherche = (String) args[0];
            System.out.println("Agent utilisateur simple " + getLocalName() + " recherche le livre: " + titreLivreRecherche);

            // Notifier l'observateur de la création de l'utilisateur
            informerObservateur("USER_NOUVEAU:Simple utilisateur recherchant \"" + titreLivreRecherche + "\"");

            // Démarrage du comportement de recherche
            addBehaviour(new RechercherLivre());
        } else {
            System.out.println("Pas de livre spécifié pour l'agent utilisateur " + getLocalName());
            doDelete();
        }
    }

    @Override
    protected void takeDown() {
        // Notifier l'observateur de la suppression de l'utilisateur
        informerObservateur("USER_FIN:L'utilisateur simple a quitté la bibliothèque");
        System.out.println("Agent utilisateur " + getLocalName() + " s'est terminé.");
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

    // Comportement pour rechercher un livre
    private class RechercherLivre extends Behaviour {
        private boolean done = false;
        private int etape = 0;

        @Override
        public void action() {
            switch (etape) {
                case 0: // Recherche de la bibliothécaire
                    DFAgentDescription template = new DFAgentDescription();
                    ServiceDescription sd = new ServiceDescription();
                    sd.setType("service-bibliotheque");
                    template.addServices(sd);

                    try {
                        DFAgentDescription[] result = DFService.search(myAgent, template);
                        if (result.length > 0) {
                            System.out.println(getLocalName() + " a trouvé la bibliothécaire: " + result[0].getName().getLocalName());
                            informerObservateur("USER_INFO:A trouvé la bibliothécaire");

                            // Envoi de la demande à la bibliothécaire
                            ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
                            request.addReceiver(result[0].getName());
                            request.setContent(titreLivreRecherche);
                            request.setConversationId("recherche-livre");
                            myAgent.send(request);

                            informerObservateur("USER_INFO:Demande si le livre \"" + titreLivreRecherche + "\" est disponible");
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
                        MessageTemplate.MatchConversationId("recherche-livre")
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

                case 2: // Attendre le résultat de la recherche
                    MessageTemplate mt2 = MessageTemplate.MatchConversationId("resultat-recherche");
                    ACLMessage msgResultat = myAgent.receive(mt2);

                    if (msgResultat != null) {
                        if (msgResultat.getPerformative() == ACLMessage.INFORM) {
                            System.out.println(getLocalName() + " a reçu une réponse positive pour le livre " + titreLivreRecherche);
                            informerObservateur("USER_INFO:Apprend que le livre \"" + titreLivreRecherche + "\" est disponible");

                            // Envoyer un message de remerciement
                            ACLMessage thanks = new ACLMessage(ACLMessage.INFORM);
                            thanks.addReceiver(msgResultat.getSender());
                            thanks.setContent("merci");
                            myAgent.send(thanks);

                            System.out.println(getLocalName() + " : Merci pour l'information !");
                            informerObservateur("USER_INFO:Remercie la bibliothécaire et quitte la bibliothèque");
                        } else {
                            System.out.println(getLocalName() + " a reçu une réponse négative pour le livre " + titreLivreRecherche);
                            informerObservateur("USER_INFO:Apprend que le livre \"" + titreLivreRecherche + "\" n'est pas disponible");

                            // Envoyer un message de remerciement quand même
                            ACLMessage thanks = new ACLMessage(ACLMessage.INFORM);
                            thanks.addReceiver(msgResultat.getSender());
                            thanks.setContent("merci quand même");
                            myAgent.send(thanks);

                            System.out.println(getLocalName() + " : Merci quand même pour votre aide.");
                            informerObservateur("USER_INFO:Remercie quand même la bibliothécaire et quitte la bibliothèque");
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
