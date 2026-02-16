package org.example.model.user;
import org.example.enums.UserRole;
import org.example.model.salaire.Salaire;

import java.util.List;
import java.util.ArrayList;

public class UserAccount {

    private int id;
    private String name;
    private String email;
    private UserRole role;


    // Constructeur user
    public UserAccount(int id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }
    public UserAccount() {}
    // association avec salaire
    private List<Salaire> salaires = new ArrayList<>();



    // Getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
    public UserRole getRole() {
        return role;
    }

    public List<Salaire> getSalaires() {
        return salaires;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    public void setRole(UserRole role) {
        this.role = role;
    }

    // methode d ajout
    public void addSalaire(Salaire salaire) {
        this.salaires.add(salaire);
    }
}
