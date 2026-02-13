package org.example.model.salaire;

import org.example.enums.SalaireStatus;
import org.example.model.user.UserAccount;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Salaire {

    private int id;;
    private double baseAmount;
    private double bonusAmount;
    private double totalAmount;
    private SalaireStatus status;
    private LocalDate datePaiement;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    // Association par avec user
    private UserAccount user;

    // association avec regle
    private List<BonusRule> bonusRules = new ArrayList<>();



    public Salaire() {
    }

    // Constructeur de creation
    public Salaire(UserAccount user, double baseAmount, LocalDate datePaiement) {
        this.user = user;
        this.baseAmount = baseAmount;
        this.bonusAmount = 0;
        this.totalAmount = baseAmount;
        this.status = SalaireStatus.CREÉ;
        this.datePaiement = datePaiement;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // constructeur de creation et affichage
    public Salaire(int id, UserAccount user, double baseAmount, double bonusAmount, double totalAmount, SalaireStatus status, LocalDate datePaiement, LocalDateTime createdAt, LocalDateTime updatedAt) {

        this.id = id;
        this.user = user;
        this.baseAmount = baseAmount;
        this.bonusAmount = bonusAmount;
        this.totalAmount = totalAmount;
        this.status = status;
        this.datePaiement = datePaiement;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters

    public int getId() { return id; }
    public double getBaseAmount() { return baseAmount; }
    public double getBonusAmount() { return bonusAmount; }
    public double getTotalAmount() { return totalAmount; }
    public SalaireStatus getStatus() { return status; }
    public LocalDate getDatePaiement() { return datePaiement; }

    // Setters

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

    public void setBaseAmount(double baseAmount) {
        this.baseAmount = baseAmount;
    }

    public void setBonusAmount(double bonusAmount) {
        this.bonusAmount = bonusAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    // add getter et setter user
    public UserAccount getUser() {
        return user;
    }

    public void setUser(UserAccount user) {
        this.user = user;
    }

    // get et set list
    public List<BonusRule> getBonusRules() {
        return bonusRules;
    }

    public void setBonusRules(List<BonusRule> bonusRules) {
        this.bonusRules = bonusRules;
    }

    // ajout regle
    public void addBonusRule(BonusRule rule) {
        this.bonusRules.add(rule);
    }

    // Logique métier

    public void addBonus(double bonus) {
        this.bonusAmount += bonus;
        this.totalAmount = this.baseAmount + this.bonusAmount;
        this.updatedAt = LocalDateTime.now();
    }

    // affichage
    @Override
    public String toString() {
        return "Salaire {" +
                "id=" + id +
                ", userId=" + user.getId() +
                ", userName='" + user.getName() + '\'' +
                ", userEmail='" + user.getEmail() + '\'' +
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
