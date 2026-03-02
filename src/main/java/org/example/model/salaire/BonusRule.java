package org.example.model.salaire;

import org.example.enums.BonusRuleStatus;
import java.time.LocalDateTime;

public class BonusRule {

    private int id;
    private String nomRegle;
    private double percentage;
    private double bonus;
    private String condition;
    private BonusRuleStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Association avec salaire
    private Salaire salaire;

    public BonusRule() {}

    /*constructeur*/
    public BonusRule(Salaire salaire, String nomRegle, double percentage, String condition) {
        this.salaire = salaire;
        this.nomRegle = nomRegle;
        this.percentage = percentage;
        this.condition = condition;
        this.status = BonusRuleStatus.CRÉE;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        // calcul de bonus dapres salaire avec pourcentage
        this.bonus = salaire.getBaseAmount() * (percentage / 100);
    }

    // ===== Getters =====
    public int getId() { return id; }
    public String getNomRegle() { return nomRegle; }
    public double getPercentage() { return percentage; }
    public double getBonus() { return bonus; }
    public String getCondition() { return condition; }
    public BonusRuleStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public Salaire getSalaire() { return salaire; }

    // ===== Setters =====
    public void setId(int id) {
        this.id = id;
    }

    public void setNomRegle(String nomRegle) {
        this.nomRegle = nomRegle;
        this.updatedAt = LocalDateTime.now();
    }

    /* set de pourcentage aussi que salaire */
    public void setPercentage(double percentage) {
        this.percentage = percentage;
        // Recalculer le bonus automatiquement
        if (this.salaire != null) {
            this.bonus = this.salaire.getBaseAmount() * (percentage / 100);
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void setBonus(double bonus) {
        this.bonus = bonus;
    }

    public void setCondition(String condition) {
        this.condition = condition;
        this.updatedAt = LocalDateTime.now();
    }

    public void setStatus(BonusRuleStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setSalaire(Salaire salaire) {
        this.salaire = salaire;
    }


    /* mmethode de recalcule de salaire*/
    public void recalculateBonus() {
        if (this.salaire != null) {
            this.bonus = this.salaire.getBaseAmount() * (this.percentage / 100);
            this.updatedAt = LocalDateTime.now();
        }
    }

    // affichage
    @Override
    public String toString() {
        return "BonusRule {" +
                "id=" + id +
                ", salaryId=" + (salaire != null ? salaire.getId() : "null") +
                ", nomRegle='" + nomRegle + '\'' +
                ", percentage=" + percentage +
                ", bonus=" + bonus +
                ", condition='" + condition + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}