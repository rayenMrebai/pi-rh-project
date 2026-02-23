package org.example.services.excel;

import org.example.enums.SalaireStatus;

import java.time.LocalDate;
import java.util.List;

public class ExportFilter {

    // Période
    private String periodeType; // "TOUS", "MOIS", "ANNEE", "PERSONNALISEE"
    private Integer mois; // 1-12
    private Integer annee;
    private LocalDate dateDebut;
    private LocalDate dateFin;

    // Filtres
    private List<SalaireStatus> statusFilters;
    private Integer userId;
    private Double montantMin;

    // Options
    private boolean inclureStatistiques = true;
    private boolean inclureBonus = false;
    private boolean appliquerFormatage = true;

    // Constructeur
    public ExportFilter() {
        this.periodeType = "TOUS";
    }

    // Getters et Setters
    public String getPeriodeType() {
        return periodeType;
    }

    public void setPeriodeType(String periodeType) {
        this.periodeType = periodeType;
    }

    public Integer getMois() {
        return mois;
    }

    public void setMois(Integer mois) {
        this.mois = mois;
    }

    public Integer getAnnee() {
        return annee;
    }

    public void setAnnee(Integer annee) {
        this.annee = annee;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    public List<SalaireStatus> getStatusFilters() {
        return statusFilters;
    }

    public void setStatusFilters(List<SalaireStatus> statusFilters) {
        this.statusFilters = statusFilters;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Double getMontantMin() {
        return montantMin;
    }

    public void setMontantMin(Double montantMin) {
        this.montantMin = montantMin;
    }

    public boolean isInclureStatistiques() {
        return inclureStatistiques;
    }

    public void setInclureStatistiques(boolean inclureStatistiques) {
        this.inclureStatistiques = inclureStatistiques;
    }

    public boolean isInclureBonus() {
        return inclureBonus;
    }

    public void setInclureBonus(boolean inclureBonus) {
        this.inclureBonus = inclureBonus;
    }

    public boolean isAppliquerFormatage() {
        return appliquerFormatage;
    }

    public void setAppliquerFormatage(boolean appliquerFormatage) {
        this.appliquerFormatage = appliquerFormatage;
    }
}