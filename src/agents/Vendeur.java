// Classe Vendeur.java (Agent réactif avec état)
package agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.Livre;

public class Vendeur extends Agent {
    // État interne de l'agent vendeur (réactif avec état)
    private Map<String, List<Livre>> catalogue = new HashMap<>();

    @Override
    protected void setup() {
        System.out.println("Agent vendeur " + getLocalName() + " est prêt.");

        // Initialisation du catalogue avec quelques livres
        initialiserCatalogue();

        // Enregistrement au service DF (Directory Facilitator)
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("vente-livres");
        sd.setName(getLocalName() + "-vendeur");
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
            System.out.println("Agent vendeur " + getLocalName() + " est enregistré au service DF.");

            // Notifier l'observateur de la création du vendeur
            informerObservateur("VENDEUR_NOUVEAU:");
        } catch (FIPAException e) {
            e.printStackTrace();
        }

        // Comportements de l'agent
        addBehaviour(new RepondreDemandesDisponibilite());
        addBehaviour(new TraiterAchats());
    }

    @Override
    protected void takeDown() {
        // Désenregistrement du service DF
        try {
            DFService.deregister(this);
            System.out.println("Agent vendeur " + getLocalName() + " s'est désenregistré du service DF.");
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        System.out.println("Agent vendeur " + getLocalName() + " s'est terminé.");
    }

    private void initialiserCatalogue() {
        // Création de quelques livres pour le catalogue
        List<Livre> romans = new ArrayList<>();
        romans.add(new Livre("1984", "George Orwell", 15.99));
        romans.add(new Livre("Le Petit Prince", "Antoine de Saint-Exupéry", 12.50));
        romans.add(new Livre("Harry Potter", "J.K. Rowling", 20.25));

        List<Livre> scienceFiction = new ArrayList<>();
        scienceFiction.add(new Livre("Dune", "Frank Herbert", 18.75));
        scienceFiction.add(new Livre("Fondation", "Isaac Asimov", 16.90));

        List<Livre> philosophie = new ArrayList<>();
        philosophie.add(new Livre("Ainsi parlait Zarathoustra", "Friedrich Nietzsche", 14.50));

        // Ajout des livres au catalogue
        catalogue.put("Roman", romans);
        catalogue.put("Science-Fiction", scienceFiction);
        catalogue.put("Philosophie", philosophie);

        // Affichage du catalogue
        System.out.println("Catalogue de l'agent vendeur " + getLocalName() + ":");
        for (Map.Entry<String, List<Livre>> entry : catalogue.entrySet()) {
            System.out.println("Catégorie: " + entry.getKey());
            for (Livre livre : entry.getValue()) {
                System.out.println("  - " + livre);
            }
        }

        // Notifier l'observateur du catalogue
        for (Map.Entry<String, List<Livre>> entry : catalogue.entrySet()) {
            String info = "Catalogue " + entry.getKey() + ": " + entry.getValue().size() + " livres";
            informerObservateur("VENDEUR_INFO:" + info);
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

    // Comportement pour répondre aux demandes de disponibilité
    private class RepondreDemandesDisponibilite extends CyclicBehaviour {
        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage msg = myAgent.receive(mt);

            if (msg != null) {
                try {
                    // Extraction du titre du livre demandé
                    String titreDemande = msg.getContent();
                    System.out.println(getLocalName() + " a reçu une demande pour le livre: " + titreDemande);

                    // Recherche du livre dans le catalogue
                    Livre livreFound = rechercherLivre(titreDemande);

                    ACLMessage reply = msg.createReply();

                    if (livreFound != null) {
                        // Envoi du prix si le livre est disponible
                        reply.setPerformative(ACLMessage.PROPOSE);
                        reply.setContentObject(livreFound);
                        System.out.println(getLocalName() + " propose le livre " + livreFound.getTitre() + " à " + livreFound.getPrix() + "€");
                        informerObservateur("VENDEUR_INFO:Propose " + livreFound.getTitre() + " à " + livreFound.getPrix() + "€ à " + msg.getSender().getLocalName());
                    } else {
                        // Refus si le livre n'est pas disponible
                        reply.setPerformative(ACLMessage.REFUSE);
                        reply.setContent("livre-non-disponible");
                        System.out.println(getLocalName() + " n'a pas le livre " + titreDemande);
                        informerObservateur("VENDEUR_INFO:N'a pas le livre " + titreDemande + " demandé par " + msg.getSender().getLocalName());
                    }

                    myAgent.send(reply);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                block();
            }
        }

        private Livre rechercherLivre(String titre) {
            // Parcours de toutes les catégories pour trouver le livre
            for (List<Livre> livres : catalogue.values()) {
                for (Livre livre : livres) {
                    if (livre.getTitre().equalsIgnoreCase(titre)) {
                        return livre;
                    }
                }
            }
            return null;
        }
    }

    // Comportement pour traiter les achats
    private class TraiterAchats extends CyclicBehaviour {
        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
            ACLMessage msg = myAgent.receive(mt);

            if (msg != null) {
                try {
                    // Extraction du livre à acheter
                    Livre livre = (Livre) msg.getContentObject();
                    System.out.println(getLocalName() + " a reçu une demande d'achat pour le livre: " + livre.getTitre());

                    // Vérification de la disponibilité
                    boolean livreDisponible = retirerLivreDuCatalogue(livre.getTitre());

                    ACLMessage reply = msg.createReply();

                    if (livreDisponible) {
                        // Confirmation de la vente
                        reply.setPerformative(ACLMessage.INFORM);
                        reply.setContent("vente-confirmée");
                        System.out.println(getLocalName() + " a vendu le livre " + livre.getTitre() + " à " + msg.getSender().getLocalName());
                        informerObservateur("VENDEUR_INFO:Vend " + livre.getTitre() + " à " + msg.getSender().getLocalName());
                        informerObservateur("TRANSACTION:Vente de " + livre.getTitre() + " par " + getLocalName() + " à " + msg.getSender().getLocalName() + " pour " + livre.getPrix() + "€");
                    } else {
                        // Refus si le livre n'est plus disponible
                        reply.setPerformative(ACLMessage.FAILURE);
                        reply.setContent("livre-plus-disponible");
                        System.out.println(getLocalName() + " ne peut plus vendre le livre " + livre.getTitre());
                        informerObservateur("VENDEUR_INFO:Ne peut plus vendre " + livre.getTitre() + " à " + msg.getSender().getLocalName());
                    }

                    myAgent.send(reply);
                } catch (UnreadableException e) {
                    e.printStackTrace();
                }
            } else {
                block();
            }
        }

        private boolean retirerLivreDuCatalogue(String titre) {
            // Parcours de toutes les catégories pour retirer le livre
            for (List<Livre> livres : catalogue.values()) {
                for (int i = 0; i < livres.size(); i++) {
                    if (livres.get(i).getTitre().equalsIgnoreCase(titre)) {
                        livres.remove(i);
                        return true;
                    }
                }
            }
            return false;
        }
    }
}
