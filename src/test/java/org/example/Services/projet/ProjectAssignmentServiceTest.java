package org.example.Services.projet;

import org.example.model.projet.EmployesDTO;
import org.example.model.projet.Project;
import org.example.model.projet.ProjectAssignment;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;



    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class ProjectAssignmentServiceTest {

        private static ProjectAssignmentService assignmentService;
        private static ProjectService projectService;
        private static int testProjectId;          // ID du projet temporaire
        private static int createdAssignmentId;     // ID de l'affectation créée

        @BeforeAll
        static void setup() {
            assignmentService = new ProjectAssignmentService();
            projectService = new ProjectService();

            // Créer un projet temporaire pour les tests d'affectation
            Project tempProject = new Project(
                    "Temp Project for Assignments",
                    "Projet créé uniquement pour les tests",
                    LocalDate.now(),
                    LocalDate.now().plusMonths(6),
                    "PLANNING",
                    5000.0
            );
            projectService.create(tempProject);
            testProjectId = tempProject.getProjectId();
            assertTrue(testProjectId > 0, "Le projet temporaire doit avoir un ID valide");
        }

        @AfterAll
        static void tearDown() {
            // Nettoyage : supprimer le projet temporaire (les affectations seront supprimées en cascade
            // si la BD est configurée avec ON DELETE CASCADE, sinon on les supprime manuellement)
            if (testProjectId > 0) {
                // Supprimer d'abord les affectations liées (si pas de cascade)
                List<ProjectAssignment> assignments = assignmentService.getByProjectId(testProjectId);
                for (ProjectAssignment a : assignments) {
                    assignmentService.delete(a.getIdAssignment());
                }
                // Puis supprimer le projet
                projectService.delete(testProjectId);
            }
        }

        @Test
        @Order(1)
        void testAjouterAffectation() {
            // Créer une affectation pour le projet temporaire
            Project project = new Project();
            project.setProjectId(testProjectId); // on ne charge que l'ID nécessaire

            // Utiliser un employé fictif (les IDs 101 à 105 sont dans la liste du dashboard)
            int employeeId = 101; // Sarah Johnson
            String role = "Chef de projet test";
            int allocation = 80;
            LocalDate start = LocalDate.now();
            LocalDate end = LocalDate.now().plusMonths(2);

            ProjectAssignment assignment = new ProjectAssignment(
                    project,
                    employeeId,
                    role,
                    allocation,
                    start,
                    end
            );

            assignmentService.create(assignment);

            assertTrue(assignment.getIdAssignment() > 0, "L'ID de l'affectation doit être > 0");
            createdAssignmentId = assignment.getIdAssignment();

            // Vérifier la présence
            List<ProjectAssignment> all = assignmentService.getByProjectId(testProjectId);
            boolean found = all.stream().anyMatch(a -> a.getIdAssignment() == createdAssignmentId);
            assertTrue(found, "L'affectation doit être présente dans la liste");
        }

        @Test
        @Order(2)
        void testModifierAffectation() {
            assertTrue(createdAssignmentId > 0, "Une affectation doit avoir été créée avant modification");

            // Récupérer l'affectation
            List<ProjectAssignment> all = assignmentService.getByProjectId(testProjectId);
            ProjectAssignment assignment = all.stream()
                    .filter(a -> a.getIdAssignment() == createdAssignmentId)
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("Affectation introuvable"));

            // Modifier
            assignment.setRole("Nouveau rôle");
            assignment.setAllocationRate(100);
            assignment.setAssignedTo(LocalDate.now().plusMonths(3));

            assignmentService.update(assignment);

            // Vérifier
            all = assignmentService.getByProjectId(testProjectId);
            ProjectAssignment modified = all.stream()
                    .filter(a -> a.getIdAssignment() == createdAssignmentId)
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("Affectation introuvable après modification"));

            assertEquals("Nouveau rôle", modified.getRole());
            assertEquals(100, modified.getAllocationRate());
            assertEquals(LocalDate.now().plusMonths(3), modified.getAssignedTo());
        }

        @Test
        @Order(3)
        void testSupprimerAffectation() {
            assertTrue(createdAssignmentId > 0, "Une affectation doit avoir été créée avant suppression");

            assignmentService.delete(createdAssignmentId);

            List<ProjectAssignment> all = assignmentService.getByProjectId(testProjectId);
            boolean found = all.stream().anyMatch(a -> a.getIdAssignment() == createdAssignmentId);
            assertFalse(found, "L'affectation ne doit plus être présente après suppression");
        }
    }
