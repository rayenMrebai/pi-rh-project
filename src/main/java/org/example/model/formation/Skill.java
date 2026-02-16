package org.example.model.formation;

import java.util.ArrayList;
import java.util.List;

public class Skill {
    //Attributs
    private int id;
    private String nom;
    private String description;
    private  String categorie ;
    private int levelRequired;
    private Integer trainingProgramId;
    //constructeurs
    public Skill() {}
    public Skill(String nom, String description, String categorie, int levelRequired) {
        this.nom = nom;
        this.description = description;
        this.categorie = categorie;
        this.levelRequired = levelRequired;
    }
    public Skill(int id, String nom, String description, String categorie, int levelRequired) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.categorie = categorie;
        this.levelRequired = levelRequired;
    }
    //getters
    public String getDescription() {
        return description;
    }

    public int getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public String getCategorie() {
        return categorie;
    }

    public int getLevelRequired() {
        return levelRequired;
    }
    public Integer getTrainingProgramId() {
        return trainingProgramId;
    }
    //setters

    public void setId(int id) {
        this.id = id;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public void setLevelRequired(int levelRequired) {
        this.levelRequired = levelRequired;
    }
    public void setTrainingProgramId(Integer trainingProgramId) {
        this.trainingProgramId = trainingProgramId;
    }
    //toString

    @Override
    public String toString() {
        return "Skill{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", description='" + description + '\'' +
                ", categorie='" + categorie + '\'' +
                ", levelRequired=" + levelRequired +
                ", trainingProgramId=" + trainingProgramId +
                '}';
    }
}


