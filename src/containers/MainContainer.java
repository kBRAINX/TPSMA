package containers;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.util.ExtendedProperties;
import jade.util.leap.Properties;
import jade.wrapper.AgentContainer;
import jade.wrapper.ControllerException;

public class MainContainer {
    public static void main(String[] args) {
        try {
            Runtime rt = Runtime.instance();

            // Propriétés pour le MainContainer
            Properties props = new ExtendedProperties();
            props.setProperty(Profile.GUI, "true"); // Interface graphique JADE

            ProfileImpl p = new ProfileImpl(props);

            // Création du conteneur principal (MainContainer)
            AgentContainer mc = rt.createMainContainer(p);

            System.out.println("MainContainer prêt");

            // Créer l'agent observateur
            try {
                mc.createNewAgent("observateur", "gui.ObservateurAgent", null).start();
                System.out.println("Agent observateur créé");
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Le conteneur principal reste actif pour que les autres conteneurs puissent s'y connecter
            mc.start();
        } catch (ControllerException e) {
            e.printStackTrace();
        }
    }
}
