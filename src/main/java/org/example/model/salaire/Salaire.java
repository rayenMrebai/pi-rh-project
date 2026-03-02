package org.example.model.salaire;

import org.example.enums.SalaireStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Salaire {

    private int id;
    private int userId;

    private double baseAmount;
    private double bonusAmount;
    private double totalAmount;

    private SalaireStatus status;
    private LocalDate datePaiement;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String userName;
    private String userEmail;


    public Salaire() {
    }

    // Création salaire
    public Salaire(int userId, double baseAmount, LocalDate datePaiement) {
        this.userId = userId;
        this.baseAmount = baseAmount;
        this.bonusAmount = 0;
        this.totalAmount = baseAmount;
        this.status = SalaireStatus.CREÉ;
        this.datePaiement = datePaiement;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // ===== Getters =====

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public double getBaseAmount() { return baseAmount; }
    public double getBonusAmount() { return bonusAmount; }
    public double getTotalAmount() { return totalAmount; }
    public SalaireStatus getStatus() { return status; }
    public LocalDate getDatePaiement() { return datePaiement; }

    // ===== Setters autorisés =====

    public void setStatus(SalaireStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public void setDatePaiement(LocalDate datePaiement) {
        this.datePaiement = datePaiement;
        this.updatedAt = LocalDateTime.now();
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setBaseAmount(double baseAmount) {
        this.baseAmount = baseAmount;
    }

    public void setBonusAmount(double bonusAmount) {
        this.bonusAmount = bonusAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }



    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    // ===== Logique métier =====

    public void addBonus(double bonus) {
        this.bonusAmount += bonus;
        this.totalAmount = this.baseAmount + this.bonusAmount;
        this.updatedAt = LocalDateTime.now();
    }


    @Override
    public String toString() {
        return "Salaire {" +
                "id=" + id +
                ", userId=" + userId +
                ", userName='" + userName + '\'' +
                ", userEmail='" + userEmail + '\'' +
                ", baseAmount=" + baseAmount +
                ", bonusAmount=" + bonusAmount +
                ", totalAmount=" + totalAmount +
                ", status=" + status +
                ", datePaiement=" + datePaiement +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

}
