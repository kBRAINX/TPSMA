package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Cette classe représente une liste de livres demandée par un utilisateur
 */
public class ListeLivres implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<String> titres;

    public ListeLivres() {
        this.titres = new ArrayList<>();
    }

    public ListeLivres(List<String> titres) {
        this.titres = titres;
    }

    public void ajouterLivre(String titre) {
        titres.add(titre);
    }

    public List<String> getTitres() {
        return titres;
    }

    public int getNombreLivres() {
        return titres.size();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Liste de livres [");
        for (int i = 0; i < titres.size(); i++) {
            sb.append(titres.get(i));
            if (i < titres.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
