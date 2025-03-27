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
import jade.lang.acl.UnreadableException;

import java.io.IOException;
import java.util.Map;

import model.Livre;
import model.ListeLivres;
import model.ResultatRecherche;

/**
 * UserListe - Agent à utilité (recherche une liste de livres et décide en fonction de l'utilité)
 * Architecture: Agent à utilité
 * Objectif: Obtenir le maximum de livres d'une liste en fonction de l'utilité perçue
 */
public class UserListe extends Agent {
    private ListeLivres listeLivresRecherche;
    private float seuilUtilite = 0.6f; // Seuil d'utilité (proportion minimum de livres trouvés) pour décider d'emprunter

    @Override
    protected void setup() {
        // Récupération des arguments
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            // Le premier argument est un tableau de titres de livres
            String[] titres = (String[]) args[0];

            // Créer la liste de livres à rechercher
            listeLivresRecherche = new ListeLivres();
            for (String titre : titres) {
                listeLivresRecherche.ajouterLivre(titre);
            }

            // Si un seuil d'utilité est précisé
            if (args.length > 1) {
                seuilUtilite = Float.parseFloat((String) args[1]);
            }

            System.out.println("Agent chercheur de liste " + getLocalName() +
                " recherche " + listeLivresRecherche.getNombreLivres() +
                " livres avec un seuil d'utilité de " + seuilUtilite);

            // Notifier l'observateur de la création de l'utilisateur
            informerObservateur("USER_NOUVEAU:Chercheur avec une liste de " +
                listeLivresRecherche.getNombreLivres() + " livres" +
                " (seuil d'utilité: " + seuilUtilite + ")");

            // Démarrage du comportement de recherche
            addBehaviour(new RechercherListe());
        } else {
            System.out.println("Pas de liste de livres spécifiée pour l'agent " + getLocalName());
            doDelete();
        }
    }

    @Override
    protected void takeDown() {
        // Notifier l'observateur de la suppression de l'utilisateur
        informerObservateur("USER_FIN:Le chercheur de liste a quitté la bibliothèque");
        System.out.println("Agent chercheur de liste " + getLocalName() + " s'est terminé.");
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

    // Comportement pour rechercher une liste de livres
    private class RechercherListe extends Behaviour {
        private boolean done = false;
        private int etape = 0;
        private ResultatRecherche resultatRecherche;
        private int nombreLivresSouhaites;

        @Override
        public void action() {
            switch(etape) {
                case 0: // Recherche de la bibliothécaire
                    nombreLivresSouhaites = listeLivresRecherche.getNombreLivres();
                    DFAgentDescription template = new DFAgentDescription();
                    ServiceDescription sd = new ServiceDescription();
                    sd.setType("service-bibliotheque");
                    template.addServices(sd);

                    try {
                        DFAgentDescription[] result = DFService.search(myAgent, template);
                        if (result.length > 0) {
                            System.out.println(getLocalName() + " a trouvé la bibliothécaire: " +
                                result[0].getName().getLocalName());
                            informerObservateur("USER_INFO:A trouvé la bibliothécaire");

                            // Envoi de la liste à la bibliothécaire
                            ACLMessage confirm = new ACLMessage(ACLMessage.CONFIRM);
                            confirm.addReceiver(result[0].getName());
                            confirm.setContentObject(listeLivresRecherche);
                            confirm.setConversationId("recherche-liste");
                            myAgent.send(confirm);

                            informerObservateur("USER_INFO:Demande la disponibilité d'une liste de " +
                                nombreLivresSouhaites + " livres");
                            etape = 1;
                        } else {
                            System.out.println(getLocalName() + " n'a pas trouvé de bibliothécaire");
                            informerObservateur("USER_INFO:N'a pas trouvé de bibliothécaire");
                            done = true;
                        }
                    } catch (FIPAException | IOException e) {
                        e.printStackTrace();
                        done = true;
                    }
                    break;

                case 1: // Attendre la réponse "recherche en cours"
                    MessageTemplate mt1 = MessageTemplate.and(
                        MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                        MessageTemplate.MatchContent("recherche-en-cours")
                    );

                    ACLMessage msgRecherche = myAgent.receive(mt1);
                    if (msgRecherche != null) {
                        System.out.println(getLocalName() + " attend pendant que la bibliothécaire recherche les livres");
                        informerObservateur("USER_INFO:Patiente pendant la recherche des livres");
                        etape = 2;
                    } else {
                        block();
                    }
                    break;

                case 2: // Attendre le résultat de la recherche
                    MessageTemplate mt2 = MessageTemplate.and(
                        MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                        MessageTemplate.not(MessageTemplate.MatchContent("recherche-en-cours"))
                    );

                    ACLMessage msgResultat = myAgent.receive(mt2);

                    if (msgResultat != null) {
                        try {
                            resultatRecherche = (ResultatRecherche) msgResultat.getContentObject();

                            int livresTrouves = resultatRecherche.getNombreLivresTrouves();
                            int livresNonTrouves = resultatRecherche.getNombreLivresNonTrouves();

                            System.out.println(getLocalName() + " a reçu le résultat de recherche: " +
                                livresTrouves + " livres trouvés, " +
                                livresNonTrouves + " non trouvés");

                            informerObservateur("USER_INFO:Reçoit les résultats: " +
                                livresTrouves + "/" + nombreLivresSouhaites +
                                " livres disponibles");

                            // Informer la bibliothécaire qu'on réfléchit
                            ACLMessage thinking = new ACLMessage(ACLMessage.INFORM);
                            thinking.addReceiver(msgResultat.getSender());
                            thinking.setContent("reflection-en-cours");
                            myAgent.send(thinking);

                            informerObservateur("USER_INFO:Demande à la bibliothécaire de patienter pendant sa réflexion");

                            // Simuler un temps de réflexion
                            try {
                                Thread.sleep(3000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            // Calculer l'utilité perçue
                            float utilite = (float) livresTrouves / nombreLivresSouhaites;

                            if (utilite >= seuilUtilite) {
                                // Décider d'emprunter les livres trouvés
                                System.out.println(getLocalName() + " décide d'emprunter les livres trouvés (utilité: " +
                                    utilite + " >= seuil: " + seuilUtilite + ")");

                                informerObservateur("USER_INFO:Décide d'emprunter les livres disponibles (utilité suffisante: " +
                                    String.format("%.2f", utilite) + ")");

                                // Construire la liste des titres à emprunter
                                StringBuilder titresAEmprunter = new StringBuilder();
                                for (Map.Entry<String, Livre> entry : resultatRecherche.getLivresTrouves().entrySet()) {
                                    titresAEmprunter.append(entry.getKey()).append(";");
                                }

                                // Envoyer la demande d'emprunt
                                ACLMessage emprunt = new ACLMessage(ACLMessage.AGREE);
                                emprunt.addReceiver(msgResultat.getSender());
                                emprunt.setContent(titresAEmprunter.toString());
                                emprunt.setConversationId("emprunt-liste");
                                myAgent.send(emprunt);

                                etape = 3;
                            } else {
                                // Décider de ne pas emprunter
                                System.out.println(getLocalName() + " décide de ne pas emprunter les livres (utilité: " +
                                    utilite + " < seuil: " + seuilUtilite + ")");

                                informerObservateur("USER_INFO:Décide de ne pas emprunter les livres (utilité insuffisante: " +
                                    String.format("%.2f", utilite) + ")");

                                // Remercier la bibliothécaire et partir
                                ACLMessage thanks = new ACLMessage(ACLMessage.INFORM);
                                thanks.addReceiver(msgResultat.getSender());
                                thanks.setContent("merci-non-emprunt");
                                myAgent.send(thanks);

                                System.out.println(getLocalName() + " : Merci pour votre recherche, mais je ne souhaite pas emprunter.");
                                informerObservateur("USER_INFO:Remercie la bibliothécaire et quitte sans emprunter");

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
                        } catch (UnreadableException e) {
                            e.printStackTrace();
                            done = true;
                        }
                    } else {
                        block();
                    }
                    break;

                case 3: // Attendre la confirmation de l'emprunt
                    MessageTemplate mt3 = MessageTemplate.and(
                        MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                        MessageTemplate.MatchConversationId("emprunt-liste")
                    );

                    ACLMessage msgEmprunt = myAgent.receive(mt3);

                    if (msgEmprunt != null) {
                        String content = msgEmprunt.getContent();

                        if (content.startsWith("tous-empruntes:")) {
                            int duree = Integer.parseInt(content.split(":")[1]);
                            System.out.println(getLocalName() + " a emprunté tous les livres pour " + duree + " jours");
                            informerObservateur("USER_INFO:A emprunté tous les livres disponibles pour " + duree + " jours");
                        } else if (content.startsWith("certains-empruntes:")) {
                            String rapport = content.split(":")[1];
                            System.out.println(getLocalName() + " a emprunté certains livres: " + rapport);
                            informerObservateur("USER_INFO:A emprunté certains livres: " + rapport);
                        }

                        // Remercier la bibliothécaire
                        ACLMessage thanks = new ACLMessage(ACLMessage.INFORM);
                        thanks.addReceiver(msgEmprunt.getSender());
                        thanks.setContent("merci-beaucoup");
                        myAgent.send(thanks);

                        System.out.println(getLocalName() + " : Merci beaucoup pour votre aide !");
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
