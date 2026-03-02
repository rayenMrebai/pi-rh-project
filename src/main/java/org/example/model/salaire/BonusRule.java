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

    public BonusRule() {}

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
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // ===== Setters =====
    public void setId(int id) {
        this.id = id;
        this.updatedAt = LocalDateTime.now();
    }

    public void setSalaryId(int salaryId) {
        this.salaryId = salaryId;
        this.updatedAt = LocalDateTime.now();
    }

    public void setNomRegle(String nomRegle) {
        this.nomRegle = nomRegle;
        this.updatedAt = LocalDateTime.now();
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
        this.updatedAt = LocalDateTime.now();
    }

    public void setBonus(double bonus) {
        this.bonus = bonus;
        this.updatedAt = LocalDateTime.now();
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

    // ===== Activation =====
    public void activate(double baseAmount) {
        this.bonus = baseAmount * (percentage / 100);
        this.status = BonusRuleStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    // ===== ToString =====
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
