package model;

import java.io.Serializable;

public class Livre implements Serializable {
    private static final long serialVersionUID = 1L;
    private String titre;
    private String auteur;
    private int quantiteDisponible;
    private int dureeEmpruntMax; // en jours

    public Livre(String titre, String auteur, int quantiteDisponible, int dureeEmpruntMax) {
        this.titre = titre;
        this.auteur = auteur;
        this.quantiteDisponible = quantiteDisponible;
        this.dureeEmpruntMax = dureeEmpruntMax;
    }

    public String getTitre() {
        return titre;
    }

    public String getAuteur() {
        return auteur;
    }

    public int getQuantiteDisponible() {
        return quantiteDisponible;
    }

    public void setQuantiteDisponible(int quantiteDisponible) {
        this.quantiteDisponible = quantiteDisponible;
    }

    public int getDureeEmpruntMax() {
        return dureeEmpruntMax;
    }

    public boolean estDisponible() {
        return quantiteDisponible > 0;
    }

    public boolean estDisponible(int nombreExemplaires) {
        return quantiteDisponible >= nombreExemplaires;
    }

    public void reduireQuantite(int nombre) {
        if (quantiteDisponible >= nombre) {
            quantiteDisponible -= nombre;
        }
    }

    @Override
    public String toString() {
        return "Livre [titre=" + titre + ", auteur=" + auteur + ", exemplaires disponibles=" + quantiteDisponible +
            ", durée d'emprunt max=" + dureeEmpruntMax + " jours]";
    }
}
