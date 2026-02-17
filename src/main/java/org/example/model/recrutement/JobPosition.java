package org.example.model.recrutement;

import java.time.LocalDate;

public class JobPosition {

    private int idJob;
    private String title;
    private String departement;
    private String employeeType;
    private String description;
    private String status;
    private LocalDate postedAt;

    // ✅ Constructeur vide
    public JobPosition() {
    }

    // ✅ Constructeur avec paramètres
    public JobPosition(String title, String departement,
                       String employeeType, String description,
                       String status, LocalDate postedAt) {
        this.title = title;
        this.departement = departement;
        this.employeeType = employeeType;
        this.description = description;
        this.status = status;
        this.postedAt = postedAt;
    }


    // ✅ Getters & Setters
    public int getIdJob() {
        return idJob;
    }

    public void setIdJob(int idJob) {
        this.idJob = idJob;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDepartement() {
        return departement;
    }

    public void setDepartement(String departement) {
        this.departement = departement;
    }

    public String getEmployeeType() {
        return employeeType;
    }

    public void setEmployeeType(String employeeType) {
        this.employeeType = employeeType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getPostedAt() {
        return postedAt;
    }

    public void setPostedAt(LocalDate postedAt) {
        this.postedAt = postedAt;
    }

    // ✅ toString
    @Override
    public String toString() {
        return "JobPosition{" +
                "idJob=" + idJob +
                ", title='" + title + '\'' +
                ", departement='" + departement + '\'' +
                ", employeeType='" + employeeType + '\'' +
                ", description='" + description + '\'' +
                ", status='" + status + '\'' +
                ", postedAt=" + postedAt +
                '}';
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JobPosition)) return false;
        return idJob == ((JobPosition) o).idJob;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(idJob);
    }

}
