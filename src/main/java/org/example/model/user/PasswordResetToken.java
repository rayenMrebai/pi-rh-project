package org.example.model.user;

import java.time.LocalDateTime;

public class PasswordResetToken {
    private int id;
    private int userId;
    private String token;
    private LocalDateTime expiryDate;
    private boolean used;

    // Constructors
    public PasswordResetToken() {}

    public PasswordResetToken(int userId, String token, LocalDateTime expiryDate) {
        this.userId = userId;
        this.token = token;
        this.expiryDate = expiryDate;
        this.used = false;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public LocalDateTime getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; }

    public boolean isUsed() { return used; }
    public void setUsed(boolean used) { this.used = used; }
}