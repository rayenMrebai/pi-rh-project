package org.example.Services.projet;

import org.example.model.projet.Project;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class ProjectServiceTest {

        private static ProjectService service;
        private static int createdProjectId; // pour stocker l'ID du projet créé

        @BeforeAll
        static void setup() {
            service = new ProjectService();
        }

        @Test
        @Order(1)
        void testAjouterProjet() {
            // Création d'un projet avec des données uniques (timestamp pour éviter les collisions)
            String uniqueName = "Test Projet " + System.currentTimeMillis();
            Project p = new Project(
                    uniqueName,
                    "Description de test",
                    LocalDate.now(),
                    LocalDate.now().plusMonths(3),
                    "PLANNING",
                    10000.0
            );

            service.create(p);

            // L'ID doit avoir été généré automatiquement
            assertTrue(p.getProjectId() > 0, "L'ID du projet doit être > 0 après insertion");
            createdProjectId = p.getProjectId();

            // Vérification que le projet est bien présent dans la liste
            List<Project> all = service.getAll();
            boolean found = all.stream().anyMatch(proj -> proj.getProjectId() == createdProjectId);
            assertTrue(found, "Le projet ajouté doit être présent dans la liste");
        }

        @Test
        @Order(2)
        void testModifierProjet() {
            assertTrue(createdProjectId > 0, "Un projet doit avoir été créé avant modification");

            // Récupérer le projet créé précédemment (on le recharge depuis la BD)
            List<Project> all = service.getAll();
            Project p = all.stream()
                    .filter(proj -> proj.getProjectId() == createdProjectId)
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("Projet introuvable"));

            // Modifier quelques champs
            p.setName("Nom modifié " + System.currentTimeMillis());
            p.setStatus("IN PROGRESS");
            p.setBudget(20000.0);

            service.update(p);

            // Vérifier les modifications
            all = service.getAll();
            Project modified = all.stream()
                    .filter(proj -> proj.getProjectId() == createdProjectId)
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("Projet introuvable après modification"));

            assertEquals(p.getName(), modified.getName());
            assertEquals("IN PROGRESS", modified.getStatus());
            assertEquals(20000.0, modified.getBudget(), 0.001);
        }

        @Test
        @Order(3)
        void testSupprimerProjet() {
            assertTrue(createdProjectId > 0, "Un projet doit avoir été créé avant suppression");

            service.delete(createdProjectId);

            List<Project> all = service.getAll();
            boolean found = all.stream().anyMatch(proj -> proj.getProjectId() == createdProjectId);
            assertFalse(found, "Le projet ne doit plus être présent après suppression");
        }

        // Nettoyage optionnel : si un test échoue avant la suppression, on supprime le projet à la fin
        @AfterEach
        void cleanUpIfNeeded() {
            // Cette méthode peut être laissée vide ou utilisée pour un nettoyage supplémentaire
            // Ici on ne fait rien car les tests s'enchaînent proprement
        }
    }
