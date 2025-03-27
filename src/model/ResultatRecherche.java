package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Cette classe représente le résultat d'une recherche de livres
 */
public class ResultatRecherche implements Serializable {
    private static final long serialVersionUID = 1L;
    private Map<String, Livre> livresTrouves;
    private List<String> livresNonTrouves;

    public ResultatRecherche() {
        this.livresTrouves = new HashMap<>();
        this.livresNonTrouves = new ArrayList<>();
    }

    public void ajouterLivreTrouve(String titre, Livre livre) {
        livresTrouves.put(titre, livre);
    }

    public void ajouterLivreNonTrouve(String titre) {
        livresNonTrouves.add(titre);
    }

    public Map<String, Livre> getLivresTrouves() {
        return livresTrouves;
    }

    public List<String> getLivresNonTrouves() {
        return livresNonTrouves;
    }

    public int getNombreLivresTrouves() {
        return livresTrouves.size();
    }

    public int getNombreLivresNonTrouves() {
        return livresNonTrouves.size();
    }

    public boolean tousLesTitresTrouves() {
        return livresNonTrouves.isEmpty();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        if (!livresTrouves.isEmpty()) {
            sb.append("Livres trouvés : ").append(livresTrouves.size()).append("\n");
            for (Map.Entry<String, Livre> entry : livresTrouves.entrySet()) {
                sb.append(" - ").append(entry.getValue().toString()).append("\n");
            }
        }

        if (!livresNonTrouves.isEmpty()) {
            sb.append("Livres non trouvés : ").append(livresNonTrouves.size()).append("\n");
            for (String titre : livresNonTrouves) {
                sb.append(" - ").append(titre).append("\n");
            }
        }

        return sb.toString();
    }
}
