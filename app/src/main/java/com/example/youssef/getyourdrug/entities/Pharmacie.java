package com.example.youssef.getyourdrug.entities;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by youssef on 28/05/2018.
 */

public class Pharmacie {

    private int id;
    private String nom;
    private String adresse;
    private String ville;
    private double latitude;
    private double longitude;

    private String marker;

    public Pharmacie(int id, String nom, String adresse, String ville, double latitude, double longitude) {
        this.id         = id;
        this.nom        = nom;
        this.adresse    = adresse;
        this.ville      = ville;
        this.latitude   = latitude;
        this.longitude = longitude;
    }

    public Pharmacie(Pharmacie p) {
        this.id         = p.getId();
        this.nom        = p.getNom();
        this.adresse    = p.getAdresse();
        this.ville      = p.getVille();
        this.latitude   = p.getLatitude();
        this.longitude = p.getLongitude();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getMarker() {
        return marker;
    }

    public void setMarker(String marker) {
        this.marker = marker;
    }

    @Override
    public String toString() {
        return this.nom;
    }

    public static Pharmacie findByLatLng(LatLng position, List<Pharmacie> pharmacies) {

        for(Pharmacie pharmacie : pharmacies) {

            if(pharmacie.getLatitude() == position.latitude && pharmacie.getLongitude() == position.longitude) {
                return pharmacie;
            }
        }
        return null;
    }
}
