package org.example.model.projet;

public class EmployesDTO {

    private int userId;
    private String username;

    public EmployesDTO(int userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public String toString() {
        return username; // Important for ComboBox display
    }
}
