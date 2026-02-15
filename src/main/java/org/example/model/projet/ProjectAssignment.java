package org.example.model.projet;

import java.time.LocalDate;

public class ProjectAssignment {

    private int idAssignment;
    private Project project;     // 🔥 relation objet
    private int employeeId;

    private String role;
    private int allocationRate;

    private LocalDate assignedFrom;
    private LocalDate assignedTo;

    // ===== Constructeur vide =====
    public ProjectAssignment() {
    }

    // ===== Constructeur métier =====
    public ProjectAssignment(Project project,
                             int employeeId,
                             String role,
                             int allocationRate,
                             LocalDate assignedFrom,
                             LocalDate assignedTo) {

        this.project = project;
        this.employeeId = employeeId;
        this.role = role;
        this.allocationRate = allocationRate;
        this.assignedFrom = assignedFrom;
        this.assignedTo = assignedTo;
    }

    // ===== Getters =====

    public int getIdAssignment() {
        return idAssignment;
    }

    public Project getProject() {
        return project;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public String getRole() {
        return role;
    }

    public int getAllocationRate() {
        return allocationRate;
    }

    public LocalDate getAssignedFrom() {
        return assignedFrom;
    }

    public LocalDate getAssignedTo() {
        return assignedTo;
    }

    // ===== Setters =====

    public void setIdAssignment(int idAssignment) {
        this.idAssignment = idAssignment;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setAllocationRate(int allocationRate) {
        this.allocationRate = allocationRate;
    }

    public void setAssignedFrom(LocalDate assignedFrom) {
        this.assignedFrom = assignedFrom;
    }

    public void setAssignedTo(LocalDate assignedTo) {
        this.assignedTo = assignedTo;
    }

    @Override
    public String toString() {
        return "ProjectAssignment{" +
                "idAssignment=" + idAssignment +
                ", projectId=" + (project != null ? project.getProjectId() : null) +
                ", employeeId=" + employeeId +
                ", role='" + role + '\'' +
                ", allocationRate=" + allocationRate +
                '}';
    }
}
