public class Main {
    public static void main(String[] args) {
        try {
            // Démarrer le conteneur principal avec l'observateur
            System.out.println("Démarrage du conteneur principal...");
            Thread mainContainerThread = new Thread(() -> {
                containers.MainContainer.main(null);
            });
            mainContainerThread.start();

            // Attendre que le conteneur principal soit prêt
            Thread.sleep(2000);

            // Démarrer le conteneur de la bibliothèque
            System.out.println("Démarrage du conteneur de la bibliothèque...");
            Thread bibliothequeThread = new Thread(() -> {
                containers.BibliothequeContainer.main(null);
            });
            bibliothequeThread.start();

            // Attendre que la bibliothèque soit prête
            Thread.sleep(3000);

            // Démarrer le conteneur des utilisateurs
            System.out.println("Démarrage du conteneur des utilisateurs...");
            Thread utilisateursThread = new Thread(() -> {
                containers.UtilisateursContainer.main(null);
            });
            utilisateursThread.start();

            System.out.println("Tous les conteneurs ont été démarrés");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
