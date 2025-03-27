package containers;

import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;

/**
 * Conteneur pour l'agent bibliothécaire
 */
public class BibliothequeContainer {
    public static void main(String[] args) {
        try {
            Runtime rt = Runtime.instance();

            // Configuration pour se connecter au MainContainer
            ProfileImpl p = new ProfileImpl(false);
            p.setParameter(ProfileImpl.MAIN_HOST, "localhost");

            // Création du conteneur pour la bibliothécaire
            AgentContainer container = rt.createAgentContainer(p);

            System.out.println("Conteneur de bibliothèque créé");

            // Création de l'agent bibliothécaire
            AgentController bibliothecaire = container.createNewAgent("bibliothecaire", "agents.Bibliothecaire", new Object[]{});
            bibliothecaire.start();

            System.out.println("Agent bibliothécaire démarré");
        } catch (ControllerException e) {
            e.printStackTrace();
        }
    }
}
