package org.example.model.user;
import org.example.enums.UserRole;

public class UserAccount {

    private int id;
    private String name;
    private String email;
    private UserRole role;

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
}
