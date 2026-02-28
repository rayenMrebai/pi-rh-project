package org.example.model.user;

import org.example.enums.UserRole;
import org.example.model.salaire.Salaire;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserAccount {
    private int userId;
    private String username;
    private String email;
    private String passwordHash;
    private UserRole role;
    private boolean isActive;
    private LocalDateTime lastLogin;
    private LocalDateTime accountCreatedDate;
    private String accountStatus; // ACTIVE, SUSPENDED, DISABLED

    public UserAccount() {}

    public UserAccount(String username, String email, String passwordHash, UserRole role) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.isActive = true;
        this.accountStatus = "ACTIVE";
        this.accountCreatedDate = LocalDateTime.now();
    }

    public UserAccount(int userId, String username, String email, String passwordHash,
                       UserRole role, boolean isActive, LocalDateTime lastLogin,
                       LocalDateTime accountCreatedDate, String accountStatus) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.isActive = isActive;
        this.lastLogin = lastLogin;
        this.accountCreatedDate = accountCreatedDate;
        this.accountStatus = accountStatus;
    }
    // Constructeur user
    public UserAccount(int id, String name, String email) {
        this.userId = id;
        this.username = name;
        this.email = email;
    }
    // association avec salaire
    private List<Salaire> salaires = new ArrayList<>();


    // Getters et setters
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }

    public LocalDateTime getAccountCreatedDate() { return accountCreatedDate; }
    public void setAccountCreatedDate(LocalDateTime accountCreatedDate) { this.accountCreatedDate = accountCreatedDate; }

    public String getAccountStatus() { return accountStatus; }
    public void setAccountStatus(String accountStatus) { this.accountStatus = accountStatus; }

    @Override
    public String toString() {
        return "UserAccount{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", accountStatus='" + accountStatus + '\'' +
                '}';
    }
}