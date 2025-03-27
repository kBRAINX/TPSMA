package agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.HashMap;
import java.util.Map;

import model.Livre;
import model.ListeLivres;
import model.ResultatRecherche;

/**
 * Agent Bibliothecaire - Agent à état (reactif avec mémoire)
 * Architecture: Réactive avec mémoire
 * Objectif: Gérer les livres de la bibliothèque et répondre aux demandes des utilisateurs
 */
public class Bibliothecaire extends Agent {
    // État interne de l'agent
    private Map<String, Livre> catalogue = new HashMap<>();

    // Compteur pour les statistiques
    private int demandes = 0;
    private int emprunts = 0;

    @Override
    protected void setup() {
        System.out.println("Agent bibliothécaire " + getLocalName() + " est prêt.");

        // Initialisation du catalogue avec quelques livres
        initialiserCatalogue();

        // Enregistrement au service DF (Directory Facilitator)
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("service-bibliotheque");
        sd.setName(getLocalName() + "-bibliothecaire");
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
            System.out.println("Agent bibliothécaire " + getLocalName() + " est enregistré au service DF.");

            // Notifier l'observateur de la création du bibliothécaire
            informerObservateur("BIBLIO_NOUVEAU:");
        } catch (FIPAException e) {
            e.printStackTrace();
        }

        // Comportements de l'agent
        addBehaviour(new RepondreDemandesDisponibilite());
    }

    @Override
    protected void takeDown() {
        // Désenregistrement du service DF
        try {
            DFService.deregister(this);
            System.out.println("Agent bibliothécaire " + getLocalName() + " s'est désenregistré du service DF.");
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        System.out.println("Agent bibliothécaire " + getLocalName() + " s'est terminé.");
    }

    private void initialiserCatalogue() {
        // Création de quelques livres pour le catalogue
        catalogue.put("1984", new Livre("1984", "George Orwell", 3, 21));
        catalogue.put("Le Petit Prince", new Livre("Le Petit Prince", "Antoine de Saint-Exupéry", 5, 14));
        catalogue.put("Harry Potter", new Livre("Harry Potter", "J.K. Rowling", 2, 30));
        catalogue.put("Dune", new Livre("Dune", "Frank Herbert", 1, 21));
        catalogue.put("Fondation", new Livre("Fondation", "Isaac Asimov", 3, 14));
        catalogue.put("Ainsi parlait Zarathoustra", new Livre("Ainsi parlait Zarathoustra", "Friedrich Nietzsche", 2, 30));
        catalogue.put("L'Étranger", new Livre("L'Étranger", "Albert Camus", 4, 21));
        catalogue.put("Les Misérables", new Livre("Les Misérables", "Victor Hugo", 2, 30));
        catalogue.put("Crime et Châtiment", new Livre("Crime et Châtiment", "Fyodor Dostoevsky", 1, 14));
        catalogue.put("Le Seigneur des Anneaux", new Livre("Le Seigneur des Anneaux", "J.R.R. Tolkien", 3, 30));

        // Affichage du catalogue
        System.out.println("Catalogue de l'agent bibliothécaire " + getLocalName() + ":");
        for (Map.Entry<String, Livre> entry : catalogue.entrySet()) {
            System.out.println("  - " + entry.getValue());
        }

        // Notifier l'observateur du catalogue
        for (Map.Entry<String, Livre> entry : catalogue.entrySet()) {
            String info = "Catalogue: " + entry.getValue().toString();
            informerObservateur("BIBLIO_INFO:" + info);
        }
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

    // Comportement pour répondre aux demandes des utilisateurs
    private class RepondreDemandesDisponibilite extends CyclicBehaviour {
        @Override
        public void action() {
            // Traiter différents types de messages
            MessageTemplate mt1 = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
            MessageTemplate mt2 = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            MessageTemplate mt3 = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
            MessageTemplate mt4 = MessageTemplate.MatchPerformative(ACLMessage.CONFIRM);
            MessageTemplate mt5 = MessageTemplate.MatchPerformative(ACLMessage.AGREE);

            MessageTemplate mt = MessageTemplate.or(
                MessageTemplate.or(mt1, mt2),
                MessageTemplate.or(MessageTemplate.or(mt3, mt4), mt5)
            );

            ACLMessage msg = myAgent.receive(mt);

            if (msg != null) {
                String sender = msg.getSender().getLocalName();

                if (msg.getPerformative() == ACLMessage.REQUEST) {
                    // Demande de disponibilité d'un livre simple
                    try {
                        String titreDemande = msg.getContent();
                        demandes++;

                        System.out.println(getLocalName() + " a reçu une demande pour le livre: " + titreDemande);
                        informerObservateur("BIBLIO_INFO:Recherche du livre \"" + titreDemande + "\" demandé par " + sender);

                        // Simuler un délai de recherche
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        // Indiquer à l'utilisateur qu'on recherche son livre
                        ACLMessage reply = msg.createReply();
                        reply.setPerformative(ACLMessage.INFORM);
                        reply.setContent("recherche-en-cours");
                        reply.setConversationId("recherche-livre");
                        myAgent.send(reply);

                        // Faire la recherche et préparer la réponse
                        Livre livreFound = catalogue.get(titreDemande);

                        try {
                            Thread.sleep(2000); // Délai supplémentaire pour la recherche
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        ACLMessage reponse = new ACLMessage(ACLMessage.INFORM);
                        reponse.addReceiver(msg.getSender());
                        reponse.setConversationId("resultat-recherche");

                        if (livreFound != null && livreFound.estDisponible()) {
                            reponse.setPerformative(ACLMessage.INFORM);
                            reponse.setContent("livre-disponible");
                            System.out.println(getLocalName() + " a trouvé le livre " + titreDemande);
                            informerObservateur("BIBLIO_INFO:Le livre \"" + titreDemande + "\" est disponible pour " + sender);
                        } else {
                            reponse.setPerformative(ACLMessage.FAILURE);
                            reponse.setContent("livre-non-disponible");
                            System.out.println(getLocalName() + " n'a pas le livre " + titreDemande + " ou il n'est plus disponible");
                            informerObservateur("BIBLIO_INFO:Le livre \"" + titreDemande + "\" n'est pas disponible pour " + sender);
                        }

                        myAgent.send(reponse);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else if (msg.getPerformative() == ACLMessage.CFP) {
                    // Demande d'emprunt d'un livre
                    try {
                        String titreDemande = msg.getContent();
                        int nombreExemplaires = 1;

                        // Vérifier si le message contient le nombre d'exemplaires
                        if (titreDemande.contains(":")) {
                            String[] parts = titreDemande.split(":");
                            titreDemande = parts[0];
                            nombreExemplaires = Integer.parseInt(parts[1]);
                        }

                        demandes++;

                        System.out.println(getLocalName() + " a reçu une demande d'emprunt pour " +
                            nombreExemplaires + " exemplaire(s) du livre: " + titreDemande);
                        informerObservateur("BIBLIO_INFO:Recherche pour emprunt de " + nombreExemplaires +
                            " exemplaire(s) du livre \"" + titreDemande + "\" par " + sender);

                        // Simuler un délai de recherche
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        // Indiquer à l'utilisateur qu'on recherche son livre
                        ACLMessage reply = msg.createReply();
                        reply.setPerformative(ACLMessage.INFORM);
                        reply.setContent("recherche-en-cours");
                        reply.setConversationId("recherche-emprunt");
                        myAgent.send(reply);

                        // Faire la recherche et préparer la réponse
                        Livre livreFound = catalogue.get(titreDemande);

                        try {
                            Thread.sleep(2000); // Délai supplémentaire pour la recherche
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        ACLMessage reponse = msg.createReply();

                        if (livreFound != null && livreFound.estDisponible(nombreExemplaires)) {
                            reponse.setPerformative(ACLMessage.PROPOSE);
                            reponse.setContent("livre-disponible:" + livreFound.getDureeEmpruntMax());
                            System.out.println(getLocalName() + " a trouvé " + nombreExemplaires +
                                " exemplaire(s) du livre " + titreDemande);
                            informerObservateur("BIBLIO_INFO:Le livre \"" + titreDemande + "\" est disponible pour l'emprunt par " + sender +
                                " (durée max: " + livreFound.getDureeEmpruntMax() + " jours)");
                        } else {
                            reponse.setPerformative(ACLMessage.REFUSE);
                            if (livreFound == null) {
                                reponse.setContent("livre-non-trouve");
                                System.out.println(getLocalName() + " n'a pas le livre " + titreDemande);
                                informerObservateur("BIBLIO_INFO:Le livre \"" + titreDemande + "\" n'existe pas dans le catalogue pour " + sender);
                            } else {
                                reponse.setContent("exemplaires-insuffisants:" + livreFound.getQuantiteDisponible());
                                System.out.println(getLocalName() + " n'a pas assez d'exemplaires du livre " + titreDemande);
                                informerObservateur("BIBLIO_INFO:Pas assez d'exemplaires du livre \"" + titreDemande +
                                    "\" pour " + sender + " (demandé: " + nombreExemplaires +
                                    ", disponible: " + livreFound.getQuantiteDisponible() + ")");
                            }
                        }

                        myAgent.send(reponse);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else if (msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
                    // Acceptation de la proposition d'emprunt
                    try {
                        String[] parts = msg.getContent().split(":");
                        String titreLivre = parts[0];
                        int nombreExemplaires = Integer.parseInt(parts[1]);

                        System.out.println(getLocalName() + " a reçu une acceptation d'emprunt pour " +
                            nombreExemplaires + " exemplaire(s) du livre: " + titreLivre);

                        Livre livre = catalogue.get(titreLivre);

                        if (livre != null && livre.estDisponible(nombreExemplaires)) {
                            // Mettre à jour l'inventaire
                            livre.reduireQuantite(nombreExemplaires);
                            emprunts++;

                            ACLMessage reply = msg.createReply();
                            reply.setPerformative(ACLMessage.INFORM);
                            reply.setContent("emprunt-confirme:" + livre.getDureeEmpruntMax());

                            informerObservateur("BIBLIO_INFO:Prête " + nombreExemplaires + " exemplaire(s) du livre \"" +
                                titreLivre + "\" à " + sender + " pour " + livre.getDureeEmpruntMax() + " jours");
                            informerObservateur("TRANSACTION:Emprunt de " + nombreExemplaires + " exemplaire(s) de \"" +
                                titreLivre + "\" par " + sender + " pour " + livre.getDureeEmpruntMax() + " jours");

                            myAgent.send(reply);
                        } else {
                            ACLMessage reply = msg.createReply();
                            reply.setPerformative(ACLMessage.FAILURE);
                            reply.setContent("emprunt-impossible");

                            informerObservateur("BIBLIO_INFO:Ne peut plus prêter \"" + titreLivre + "\" à " + sender);

                            myAgent.send(reply);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else if (msg.getPerformative() == ACLMessage.CONFIRM) {
                    // Réception d'une liste de livres à rechercher
                    try {
                        ListeLivres listeLivres = (ListeLivres) msg.getContentObject();
                        int nombreLivres = listeLivres.getNombreLivres();

                        System.out.println(getLocalName() + " a reçu une demande de recherche pour une liste de " +
                            nombreLivres + " livres de " + sender);
                        informerObservateur("BIBLIO_INFO:Recherche d'une liste de " + nombreLivres + " livres pour " + sender);

                        // Informer l'utilisateur que la recherche est en cours
                        ACLMessage reply = msg.createReply();
                        reply.setPerformative(ACLMessage.INFORM);
                        reply.setContent("recherche-en-cours");
                        myAgent.send(reply);

                        // Simuler un délai de recherche proportionnel au nombre de livres
                        try {
                            Thread.sleep(1000 + nombreLivres * 500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        // Rechercher les livres
                        ResultatRecherche resultat = new ResultatRecherche();

                        for (String titre : listeLivres.getTitres()) {
                            Livre livre = catalogue.get(titre);
                            if (livre != null && livre.estDisponible()) {
                                resultat.ajouterLivreTrouve(titre, livre);
                                informerObservateur("BIBLIO_INFO:Livre trouvé: \"" + titre + "\" (" +
                                    livre.getQuantiteDisponible() + " exemplaires disponibles)");
                            } else {
                                resultat.ajouterLivreNonTrouve(titre);
                                informerObservateur("BIBLIO_INFO:Livre non trouvé: \"" + titre + "\"");
                            }
                        }

                        // Envoyer le résultat
                        ACLMessage reponse = msg.createReply();
                        reponse.setPerformative(ACLMessage.INFORM);
                        reponse.setContentObject(resultat);

                        if (resultat.tousLesTitresTrouves()) {
                            System.out.println(getLocalName() + " a trouvé tous les livres demandés par " + sender);
                            informerObservateur("BIBLIO_INFO:Tous les livres demandés par " + sender + " sont disponibles");
                        } else {
                            System.out.println(getLocalName() + " n'a pas trouvé tous les livres demandés par " + sender +
                                " (" + resultat.getNombreLivresTrouves() + "/" + nombreLivres + ")");
                            informerObservateur("BIBLIO_INFO:Seulement " + resultat.getNombreLivresTrouves() + "/" +
                                nombreLivres + " livres trouvés pour " + sender);
                        }

                        myAgent.send(reponse);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else if (msg.getPerformative() == ACLMessage.AGREE) {
                    // Réception d'une demande d'emprunt de plusieurs livres
                    try {
                        // Le contenu contient la liste des titres séparés par des ";"
                        String content = msg.getContent();
                        String[] livresAEmprunter = content.split(";");

                        System.out.println(getLocalName() + " a reçu une demande d'emprunt pour " +
                            livresAEmprunter.length + " livres de " + sender);

                        // Vérifier tous les livres et les emprunter
                        boolean tousDisponibles = true;
                        StringBuilder rapport = new StringBuilder();
                        int dureeMin = Integer.MAX_VALUE;

                        for (String titre : livresAEmprunter) {
                            Livre livre = catalogue.get(titre);
                            if (livre != null && livre.estDisponible()) {
                                livre.reduireQuantite(1);
                                emprunts++;
                                rapport.append(titre).append(" (").append(livre.getDureeEmpruntMax()).append(" jours);");
                                dureeMin = Math.min(dureeMin, livre.getDureeEmpruntMax());

                                informerObservateur("BIBLIO_INFO:Prête le livre \"" + titre + "\" à " + sender);
                                informerObservateur("TRANSACTION:Emprunt de \"" + titre + "\" par " + sender +
                                    " pour " + livre.getDureeEmpruntMax() + " jours");
                            } else {
                                tousDisponibles = false;
                                rapport.append(titre).append(" (non disponible);");

                                informerObservateur("BIBLIO_INFO:Ne peut pas prêter le livre \"" + titre + "\" à " + sender);
                            }
                        }

                        // Envoyer la confirmation
                        ACLMessage reply = msg.createReply();

                        if (tousDisponibles) {
                            reply.setPerformative(ACLMessage.INFORM);
                            reply.setContent("tous-empruntes:" + dureeMin);
                            System.out.println(getLocalName() + " a prêté tous les livres demandés à " + sender);
                            informerObservateur("BIBLIO_INFO:Tous les livres ont été prêtés à " + sender +
                                " (durée: " + dureeMin + " jours)");
                        } else {
                            reply.setPerformative(ACLMessage.INFORM);
                            reply.setContent("certains-empruntes:" + rapport.toString());
                            System.out.println(getLocalName() + " n'a pas pu prêter tous les livres demandés à " + sender);
                            informerObservateur("BIBLIO_INFO:Certains livres seulement ont été prêtés à " + sender);
                        }

                        myAgent.send(reply);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                block();
            }
        }
    }
}
