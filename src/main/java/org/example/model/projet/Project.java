package org.example.model.projet;

import java.time.LocalDate;

public class Project {

    private int projectId;   // correspond EXACTEMENT à la DB
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private double budget;

    // ===== Constructeur vide (obligatoire JDBC) =====
    public Project() {
    }

    // ===== Constructeur métier =====
    public Project(String name, String description,
                   LocalDate startDate, LocalDate endDate,
                   String status, double budget) {

        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.budget = budget;
    }

    // ===== Getters =====

    public int getProjectId() {
        return projectId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public String getStatus() {
        return status;
    }

    public double getBudget() {
        return budget;
    }

    // ===== Setters =====

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setBudget(double budget) {
        this.budget = budget;
    }

    @Override
    public String toString() {
        return "Project{" +
                "projectId=" + projectId +
                ", name='" + name + '\'' +
                ", status='" + status + '\'' +
                ", budget=" + budget +
                '}';
    }
}
