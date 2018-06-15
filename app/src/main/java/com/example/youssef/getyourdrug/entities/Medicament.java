package com.example.youssef.getyourdrug.entities;

/**
 * Created by youssef on 28/05/2018.
 */

public class Medicament {

    private int id;
    private String codeBar;
    private String nom;
    private double prix;
    private String image;

    public Medicament(int id, String nom, double prix, String image) {
        this.id      = id;
        this.nom     = nom;
        this.prix    = prix;
        this.image   = image;
    }

    public Medicament(Medicament m) {
        this.id      = m.getId();
        this.nom     = m.getNom();
        this.prix    = m.getPrix();
        this.image   = m.getImage();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCodeBar() {
        return codeBar;
    }

    public void setCodeBar(String codeBar) {
        this.codeBar = codeBar;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public String toString() {
        return this.nom;
    }
}
