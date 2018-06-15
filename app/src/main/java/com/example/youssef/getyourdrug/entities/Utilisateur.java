package com.example.youssef.getyourdrug.entities;

/**
 * Created by youssef on 28/05/2018.
 */

public class Utilisateur {

    private int id;
    private String cin;
    private String nom;
    private String prenom;
    private String email;
    private String password;
    private String tel;
    private String adresse;

    public Utilisateur(int id, String cin, String nom, String prenom, String email, String password, String tel, String adresse) {
        this.id       = id;
        this.cin      = cin;
        this.nom      = nom;
        this.prenom   = prenom;
        this.email    = email;
        this.password = password;
        this.tel      = tel;
        this.adresse  = adresse;
    }

    public Utilisateur(Utilisateur u) {
        this.id       = u.getId();
        this.cin      = u.getCin();
        this.nom      = u.getNom();
        this.prenom   = u.getPrenom();
        this.email    = u.getEmail();
        this.password = u.getPassword();
        this.tel      = u.getTel();
        this.adresse  = u.getAdresse();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCin() {
        return cin;
    }

    public void setCin(String cin) {
        this.cin = cin;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    @Override
    public String toString() {
        return this.nom + this.prenom;
    }

}
