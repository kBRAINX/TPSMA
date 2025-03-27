package containers;

import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;

public class VendeursContainer {
    public static void main(String[] args) {
        try {
            Runtime rt = Runtime.instance();

            // Configuration pour se connecter au MainContainer
            ProfileImpl p = new ProfileImpl(false);
            p.setParameter(ProfileImpl.MAIN_HOST, "localhost");

            // Création du conteneur pour les vendeurs
            AgentContainer container = rt.createAgentContainer(p);

            System.out.println("Conteneur de vendeurs créé");

            // Création des agents vendeurs
            AgentController ac1 = container.createNewAgent("vendeur1", "agents.Vendeur", new Object[]{});
            AgentController ac2 = container.createNewAgent("vendeur2", "agents.Vendeur", new Object[]{});
            AgentController ac3 = container.createNewAgent("vendeur3", "agents.Vendeur", new Object[]{});

            // Démarrage des agents
            ac1.start();
            ac2.start();
            ac3.start();

            System.out.println("Agents vendeurs démarrés");
        } catch (ControllerException e) {
            e.printStackTrace();
        }
    }
}
