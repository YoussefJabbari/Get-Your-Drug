package com.example.youssef.getyourdrug.entities;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by youssef on 28/05/2018.
 */

public class Reservation {

    private int id;
    private int utilisateur_id;
    private int affectation_id;
    private int quantite;
    private Date dateReservation;
    private Date dateAchat;

    private String pharmacie;
    private String medicament;
    private String image;

    public Reservation(int id, int utilisateur_id, int affectation_id, int quantite, Date dateReservation, Date dateAchat, String pharmacie, String medicament, String image) {
        this.id              = id;
        this.utilisateur_id  = utilisateur_id;
        this.affectation_id  = affectation_id;
        this.quantite        = quantite;
        this.dateReservation = new Date(String.valueOf(dateReservation));
        this.dateAchat       = new Date(String.valueOf(dateAchat));
        this.pharmacie       = pharmacie;
        this.medicament      = medicament;
        this.image           = image;
    }

    public Reservation(Reservation r) {
        this.id              = r.getId();
        this.utilisateur_id  = r.getUtilisateur_id();
        this.affectation_id  = r.getAffectation_id();
        this.quantite        = r.getQuantite();
        this.dateReservation = new Date(String.valueOf(r.getDateReservation()));
        this.dateAchat       = new Date(String.valueOf(r.getDateAchat()));
        this.pharmacie       = r.getPharmacie();
        this.medicament      = r.getMedicament();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUtilisateur_id() {
        return utilisateur_id;
    }

    public void setUtilisateur_id(int utilisateur_id) {
        this.utilisateur_id = utilisateur_id;
    }

    public int getAffectation_id() {
        return affectation_id;
    }

    public void setAffectation_id(int affectation_id) {
        this.affectation_id = affectation_id;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public Date getDateReservation() {
        return dateReservation;
    }

    public void setDateReservation(Date dateReservation) {
        this.dateReservation = new Date(String.valueOf(dateReservation));
    }

    public Date getDateAchat() {
        return dateAchat;
    }

    public void setDateAchat(Date dateAchat) {
        this.dateAchat = new Date(String .valueOf(dateAchat));
    }

    public String getPharmacie() {
        return pharmacie;
    }

    public void setPharmacie(String pharmacie) {
        this.pharmacie = pharmacie;
    }

    public String getMedicament() {
        return medicament;
    }

    public void setMedicament(String medicament) {
        this.medicament = medicament;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getStrDateReservation()
    {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(this.dateReservation);
    }

    public String getStrDateAchat()
    {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(this.dateAchat);
    }

    @Override
    public String toString() {
        return "Réservation de: " +
                    this.quantite + " " +
                    this.medicament +
                    " chez " +
                    this.pharmacie;
    }

}
