package com.example.youssef.getyourdrug.entities;

/**
 * Created by youssef on 28/05/2018.
 */

public class Affectation {

    private int id;
    private int medicament_id;
    private int pharmacie_id;
    private int quantite;

    public Affectation(int id, int medicament_id, int pharmacie_id, int quantite) {
        this.id            = id;
        this.medicament_id = medicament_id;
        this.pharmacie_id  = pharmacie_id;
        this.quantite      = quantite;
    }

    public Affectation(Affectation a) {
        this.id            = a.getId();
        this.medicament_id = a.getMedicament_id();
        this.pharmacie_id  = a.getPharmacie_id();
        this.quantite      = a.getQuantite();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMedicament_id() {
        return medicament_id;
    }

    public void setMedicament_id(int medicament_id) {
        this.medicament_id = medicament_id;
    }

    public int getPharmacie_id() {
        return pharmacie_id;
    }

    public void setPharmacie_id(int pharmacie_id) {
        this.pharmacie_id = pharmacie_id;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    @Override
    public String toString() {
        return "" + this.quantite;
    }
}
