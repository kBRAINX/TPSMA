package containers;

import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

/**
 * Conteneur pour les agents utilisateurs
 * Crée et démarre les différents types d'utilisateurs séquentiellement pour simuler leurs interactions
 */
public class UtilisateursContainer {
    public static void main(String[] args) {
        try {
            Runtime rt = Runtime.instance();

            // Configuration pour se connecter au MainContainer
            ProfileImpl p = new ProfileImpl(false);
            p.setParameter(ProfileImpl.MAIN_HOST, "localhost");

            // Création du conteneur pour les utilisateurs
            AgentContainer container = rt.createAgentContainer(p);

            System.out.println("Conteneur d'utilisateurs créé");

            // Pause pour s'assurer que le bibliothécaire est prêt
            Thread.sleep(2000);

            // Création des utilisateurs à intervalle régulier
            // Utilisateur simple - qui demande juste si un livre est disponible
            AgentController user1 = container.createNewAgent("user1",
                "agents.UserSimple",
                new Object[]{"Le Petit Prince"});
            user1.start();
            Thread.sleep(8000); // Attendre que l'interaction soit terminée

            // Utilisateur emprunteur - qui demande un livre et veut l'emprunter
            AgentController user2 = container.createNewAgent("user2",
                "agents.UserEmprunteur",
                new Object[]{"1984", "2"});
            user2.start();
            Thread.sleep(15000); // Attendre que l'interaction soit terminée

            // Utilisateur avec liste - cas positif avec beaucoup de livres disponibles
            String[] listeLivres1 = {"Dune", "Fondation", "Les Misérables"};
            AgentController user3 = container.createNewAgent("user3",
                "agents.UserListe",
                new Object[]{listeLivres1, "0.5"});
            user3.start();
            Thread.sleep(15000); // Attendre que l'interaction soit terminée

            // Utilisateur avec liste - cas négatif avec peu de livres disponibles
            String[] listeLivres2 = {"Crime et Châtiment", "Le Seigneur des Anneaux", "Harry Potter", "Don Quichotte", "Hamlet"};
            AgentController user4 = container.createNewAgent("user4",
                "agents.UserListe",
                new Object[]{listeLivres2, "0.7"});
            user4.start();
            Thread.sleep(15000); // Attendre que l'interaction soit terminée

            // Utilisateur simple qui cherche un livre non disponible
            AgentController user5 = container.createNewAgent("user5",
                "agents.UserSimple",
                new Object[]{"Les Frères Karamazov"});
            user5.start();

            System.out.println("Tous les agents utilisateurs ont été démarrés");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
