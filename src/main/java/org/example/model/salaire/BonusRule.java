package org.example.model.salaire;

import org.example.enums.BonusRuleStatus;

import java.time.LocalDateTime;

public class BonusRule {

    private int id;
    private int salaryId;

    private String nomRegle;
    private double percentage;
    private double bonus;

    private String condition;
    private BonusRuleStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public BonusRule() {
    }

    // Création règle
    public BonusRule(int salaryId, String nomRegle, double percentage, String condition) {
        this.salaryId = salaryId;
        this.nomRegle = nomRegle;
        this.percentage = percentage;
        this.condition = condition;
        this.bonus = 0;
        this.status = BonusRuleStatus.CRÉE;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // ===== Getters =====

    public int getId() { return id; }
    public int getSalaryId() { return salaryId; }
    public String getNomRegle() { return nomRegle; }
    public double getPercentage() { return percentage; }
    public double getBonus() { return bonus; }
    public String getCondition() { return condition; }
    public BonusRuleStatus getStatus() { return status; }

    // ===== Setters AVANT activation =====

    public void setNomRegle(String nomRegle) {
        this.nomRegle = nomRegle;
        this.updatedAt = LocalDateTime.now();
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
        this.updatedAt = LocalDateTime.now();
    }

    public void setCondition(String condition) {
        this.condition = condition;
        this.updatedAt = LocalDateTime.now();
    }

    // ===== Activation =====

    public void activate(double baseAmount) {
        this.bonus = baseAmount * (percentage / 100);
        this.status = BonusRuleStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }


    @Override
    public String toString() {
        return "BonusRule {" +
                "id=" + id +
                ", salaryId=" + salaryId +
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
