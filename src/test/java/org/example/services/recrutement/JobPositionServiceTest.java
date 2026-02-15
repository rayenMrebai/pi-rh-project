package org.example.services.recrutement;

import org.example.model.recrutement.JobPosition;
import org.junit.jupiter.api.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JobPositionServiceTest {

    private static JobPositionService jobPositionService;
    private static int idJobTest;

    @BeforeAll
    static void setup() {
        jobPositionService = new JobPositionService();
        System.out.println("✅ Setup completed - JobPositionService initialisé");
    }

    @Test
    @Order(1)
    void testAjouterJobPosition() throws SQLException {
        System.out.println("🔵 Test 1: Ajout d'un poste");

        // Créer un JobPosition de test
        JobPosition job = new JobPosition();
        job.setTitle("Développeur Test");
        job.setDepartement("IT Test");
        job.setEmployeeType("CDI");
        job.setDescription("Poste de test pour les tests unitaires");
        job.setStatus("Open");
        job.setPostedAt(LocalDate.now());

        // Ajouter le poste
        jobPositionService.create(job);

        // Vérifier que l'ID a été généré
        assertTrue(job.getIdJob() > 0, "L'ID du poste doit être > 0 après insertion");
        idJobTest = job.getIdJob();

        // Récupérer tous les postes
        List<JobPosition> jobs = jobPositionService.getAll();

        // Vérifier que la liste n'est pas vide
        assertFalse(jobs.isEmpty(), "La liste des postes ne doit pas être vide");

        // Vérifier que notre poste est dans la liste
        boolean found = jobs.stream()
                .anyMatch(j -> j.getTitle().equals("Développeur Test"));

        assertTrue(found, "Le poste ajouté doit être trouvé dans la liste");

        System.out.println("✅ Poste ajouté avec ID: " + job.getIdJob());
    }

    @Test
    @Order(2)
    void testModifierJobPosition() throws SQLException {
        System.out.println("🔵 Test 2: Modification d'un poste");

        // Vérifier que nous avons un ID de test
        assertTrue(idJobTest > 0, "L'ID du poste test doit être > 0");

        // Récupérer le poste par son ID
        JobPosition jobAModifier = jobPositionService.findById(idJobTest);

        assertNotNull(jobAModifier, "Le poste à modifier doit exister");

        // Modifier les données
        jobAModifier.setTitle("Développeur Test Modifié");
        jobAModifier.setDepartement("IT Modifié");
        jobAModifier.setStatus("Closed");
        jobAModifier.setDescription("Description modifiée");

        // Appliquer la modification
        jobPositionService.update(jobAModifier);

        // Récupérer le poste modifié
        JobPosition jobModifie = jobPositionService.findById(idJobTest);

        // Vérifier que la modification a été appliquée
        assertNotNull(jobModifie, "Le poste modifié doit exister");
        assertEquals("Développeur Test Modifié", jobModifie.getTitle(), "Le titre doit être modifié");
        assertEquals("IT Modifié", jobModifie.getDepartement(), "Le département doit être modifié");
        assertEquals("Closed", jobModifie.getStatus(), "Le statut doit être modifié");

        System.out.println("✅ Poste modifié avec succès");
    }

    @Test
    @Order(3)
    void testSupprimerJobPosition() throws SQLException {
        System.out.println("🔵 Test 3: Suppression d'un poste");

        // Vérifier que nous avons un ID de test
        assertTrue(idJobTest > 0, "L'ID du poste test doit être > 0");

        // Supprimer le poste
        jobPositionService.delete(idJobTest);

        // Essayer de récupérer le poste supprimé
        JobPosition jobSupprime = jobPositionService.findById(idJobTest);

        // Vérifier que le poste n'existe plus
        assertNull(jobSupprime, "Le poste ne doit plus exister après suppression");

        // Vérifier aussi avec getAll()
        List<JobPosition> jobs = jobPositionService.getAll();
        boolean exists = jobs.stream()
                .anyMatch(j -> j.getIdJob() == idJobTest);

        assertFalse(exists, "Le poste ne doit plus être dans la liste après suppression");

        System.out.println("✅ Poste supprimé avec succès");
    }

    @Test
    @Order(4)
    void testFindById() throws SQLException {
        System.out.println("🔵 Test 4: Test de findById");

        // Créer un poste temporaire pour ce test
        JobPosition jobTemp = new JobPosition();
        jobTemp.setTitle("Poste Temporaire");
        jobTemp.setDepartement("Test");
        jobTemp.setEmployeeType("Stage");
        jobTemp.setDescription("Pour test findById");
        jobTemp.setStatus("Open");
        jobTemp.setPostedAt(LocalDate.now());

        jobPositionService.create(jobTemp);
        int idTemp = jobTemp.getIdJob();

        // Tester findById
        JobPosition jobTrouve = jobPositionService.findById(idTemp);

        assertNotNull(jobTrouve, "Le poste doit être trouvé par son ID");
        assertEquals("Poste Temporaire", jobTrouve.getTitle(), "Le titre doit correspondre");

        // Nettoyer
        jobPositionService.delete(idTemp);

        System.out.println("✅ Test findById réussi");
    }

    @AfterAll
    static void cleanUp() {
        System.out.println("🧹 Nettoyage final terminé");
    }
}