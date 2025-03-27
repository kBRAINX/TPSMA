package gui;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * ObservateurAgent - Agent de surveillance (agent réactif complexe)
 * Architecture: Réactive avec état
 * Objectif: Observer et enregistrer les interactions entre la bibliothécaire et les utilisateurs
 */
public class ObservateurAgent extends Agent {
    private static final long serialVersionUID = 1L;
    private BibliothequeGUI gui;

    @Override
    protected void setup() {
        System.out.println("Agent observateur démarré");

        // Initialisation de la GUI
        gui = BibliothequeGUI.getInstance();
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
        // Rechercher la bibliothécaire
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("service-bibliotheque");
        template.addServices(sd);

        try {
            DFAgentDescription[] result = DFService.search(this, template);
            for (DFAgentDescription agent : result) {
                gui.ajouterBibliothecaire(agent.getName().getLocalName());

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

                if (content.startsWith("BIBLIO_NOUVEAU:")) {
                    gui.ajouterBibliothecaire(sender);
                }
                else if (content.startsWith("USER_NOUVEAU:")) {
                    String info = content.substring("USER_NOUVEAU:".length()).trim();
                    gui.ajouterUtilisateur(sender, info);
                }
                else if (content.startsWith("USER_FIN:")) {
                    gui.supprimerUtilisateur(sender);
                }
                else if (content.startsWith("BIBLIO_INFO:")) {
                    String info = content.substring("BIBLIO_INFO:".length()).trim();
                    gui.miseAJourBibliothecaire(sender, info);
                }
                else if (content.startsWith("USER_INFO:")) {
                    String info = content.substring("USER_INFO:".length()).trim();
                    gui.miseAJourUtilisateur(sender, info);
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
