package gui;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ObservateurAgent extends Agent {
    private static final long serialVersionUID = 1L;
    private LibrairieGUI gui;

    @Override
    protected void setup() {
        System.out.println("Agent observateur démarré");

        // Initialisation de la GUI
        gui = LibrairieGUI.getInstance();
        gui.setVisible(true);

        // S'enregistrer comme observateur
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("observateur");
        sd.setName("JADE-observateur");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        // Ajouter un comportement pour surveiller les nouveaux agents
        addBehaviour(new SurveillerAgentsBehaviour());

        // Envoyer un message à tous les agents existants pour se présenter
        saluerAgentsExistants();
    }

    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        gui.dispose();
        System.out.println("Agent observateur terminé");
    }

    private void saluerAgentsExistants() {
        // Rechercher des vendeurs
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("vente-livres");
        template.addServices(sd);

        try {
            DFAgentDescription[] result = DFService.search(this, template);
            for (DFAgentDescription agent : result) {
                gui.ajouterVendeur(agent.getName().getLocalName());

                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.addReceiver(agent.getName());
                msg.setContent("observe");
                send(msg);
            }
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    private class SurveillerAgentsBehaviour extends CyclicBehaviour {
        private static final long serialVersionUID = 1L;

        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            ACLMessage msg = myAgent.receive(mt);

            if (msg != null) {
                String sender = msg.getSender().getLocalName();
                String content = msg.getContent();

                if (content.startsWith("VENDEUR_NOUVEAU:")) {
                    gui.ajouterVendeur(sender);
                }
                else if (content.startsWith("ACHETEUR_NOUVEAU:")) {
                    String livre = content.substring("ACHETEUR_NOUVEAU:".length()).trim();
                    gui.ajouterAcheteur(sender, livre);
                }
                else if (content.startsWith("ACHETEUR_FIN:")) {
                    gui.supprimerAcheteur(sender);
                }
                else if (content.startsWith("VENDEUR_INFO:")) {
                    String info = content.substring("VENDEUR_INFO:".length()).trim();
                    gui.miseAJourVendeur(sender, info);
                }
                else if (content.startsWith("ACHETEUR_INFO:")) {
                    String info = content.substring("ACHETEUR_INFO:".length()).trim();
                    gui.miseAJourAcheteur(sender, info);
                }
                else if (content.startsWith("TRANSACTION:")) {
                    String transaction = content.substring("TRANSACTION:".length()).trim();
                    gui.logTransaction(transaction);
                }
            } else {
                block();
            }
        }
    }
}
