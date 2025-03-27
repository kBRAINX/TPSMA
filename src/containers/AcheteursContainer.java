package containers;

import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

public class AcheteursContainer {
    public static void main(String[] args) {
        try {
            Runtime rt = Runtime.instance();

            // Configuration pour se connecter au MainContainer
            ProfileImpl p = new ProfileImpl(false);
            p.setParameter(ProfileImpl.MAIN_HOST, "localhost");

            // Création du conteneur pour les acheteurs
            AgentContainer container = rt.createAgentContainer(p);

            System.out.println("Conteneur d'acheteurs créé");

            // Création des agents acheteurs avec les livres qu'ils recherchent
            // Pause entre chaque création pour visualiser les interactions
            AgentController ac1 = container.createNewAgent("acheteur1", "agents.Acheteur", new Object[]{"Harry Potter"});
            ac1.start();

            Thread.sleep(5000); // Pause de 2 secondes

            AgentController ac2 = container.createNewAgent("acheteur2", "agents.Acheteur", new Object[]{"1984"});
            ac2.start();

            Thread.sleep(5000); // Pause de 2 secondes

            AgentController ac3 = container.createNewAgent("acheteur3", "agents.Acheteur", new Object[]{"Dune"});
            ac3.start();

            Thread.sleep(5000); // Pause de 2 secondes

            // Un acheteur qui cherche un livre inexistant
            AgentController ac4 = container.createNewAgent("acheteur4", "agents.Acheteur", new Object[]{"Le Seigneur des Anneaux"});
            ac4.start();

            System.out.println("Agents acheteurs démarrés");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
