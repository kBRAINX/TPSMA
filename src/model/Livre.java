package model;

import java.io.Serializable;

public class Livre implements Serializable {
    private String titre;
    private String auteur;
    private double prix;

    public Livre(String titre, String auteur, double prix) {
        this.titre = titre;
        this.auteur = auteur;
        this.prix = prix;
    }

    public String getTitre() {
        return titre;
    }

    public String getAuteur() {
        return auteur;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    @Override
    public String toString() {
        return "Livre [titre=" + titre + ", auteur=" + auteur + ", prix=" + prix + "]";
    }
}
