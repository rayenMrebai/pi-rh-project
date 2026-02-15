package org.example;

import org.example.model.projet.Project;
import org.example.model.projet.ProjectAssignment;
import org.example.Services.projet.ProjectService;
import org.example.Services.projet.ProjectAssignmentService;
import org.example.util.DatabaseConnection;

import java.sql.Connection;
import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {

        // ===== Connexion à la DB =====
        Connection conn = DatabaseConnection.getInstance().getConnection();
        if (conn != null) {
            System.out.println("✅ Connexion OK. Application prête à utiliser la base.");
        } else {
            System.out.println("❌ Connexion NULL. Vérifie la DB.");
            return;
        }

        ProjectService projectService = new ProjectService();
        ProjectAssignmentService assignmentService = new ProjectAssignmentService();

        // ===== Nettoyage pour tests =====
        System.out.println("🧹 Nettoyage tables pour tests...");
        projectService.getAll().forEach(p -> projectService.delete(p.getProjectId()));
        assignmentService.getAll().forEach(a -> assignmentService.delete(a.getIdAssignment()));

        // ===== CREATE PROJECTS =====
        Project p1 = new Project("ERP RH", "Gestion RH globale", LocalDate.now(), LocalDate.now().plusMonths(6), "CREATED", 50000);
        Project p2 = new Project("Application Paie", "Gestion des salaires", LocalDate.now(), LocalDate.now().plusMonths(3), "CREATED", 20000);

        projectService.create(p1);
        projectService.create(p2);

        // ===== READ PROJECTS =====
        System.out.println("\n📋 Liste des projets après création:");
        projectService.getAll().forEach(System.out::println);

        // ===== UPDATE PROJECT =====
        System.out.println("\n🔄 Mise à jour du projet ERP RH...");
        p1.setStatus("ACTIVE");
        p1.setBudget(60000);
        projectService.update(p1);

        System.out.println("\n📋 Liste des projets après mise à jour:");
        projectService.getAll().forEach(System.out::println);

        // ===== CREATE ASSIGNMENTS =====
        ProjectAssignment a1 = new ProjectAssignment(p1, 101, "Développeur Java", 100, LocalDate.now(), null);
        ProjectAssignment a2 = new ProjectAssignment(p1, 102, "Testeur QA", 50, LocalDate.now(), LocalDate.now().plusMonths(3));
        ProjectAssignment a3 = new ProjectAssignment(p2, 103, "Analyste Paie", 100, LocalDate.now(), null);

        assignmentService.create(a1);
        assignmentService.create(a2);
        assignmentService.create(a3);

        // ===== READ ASSIGNMENTS =====
        System.out.println("\n📋 Liste des affectations après création:");
        assignmentService.getAll().forEach(System.out::println);

        // ===== UPDATE ASSIGNMENT =====
        System.out.println("\n🔄 Mise à jour de l'affectation Testeur QA...");
        a2.setAllocationRate(80);
        assignmentService.update(a2);

        System.out.println("\n📋 Liste des affectations après mise à jour:");
        assignmentService.getAll().forEach(System.out::println);

        // ===== DELETE EXAMPLE =====
        System.out.println("\n🗑️ Suppression de l'affectation Analyste Paie...");
        assignmentService.delete(a3.getIdAssignment());

        System.out.println("\n🗑️ Suppression du projet Application Paie...");
        projectService.delete(p2.getProjectId());

        // ===== FINAL STATE =====
        System.out.println("\n✅ État final des projets:");
        projectService.getAll().forEach(System.out::println);

        System.out.println("\n✅ État final des affectations:");
        assignmentService.getAll().forEach(System.out::println);

        System.out.println("\n🎉 CRUD tests complets et propres.");
    }
}
