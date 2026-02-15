package org.example.services.recrutement;

import org.example.model.recrutement.Candidat;
import org.example.model.recrutement.JobPosition;
import org.junit.jupiter.api.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CandidatServiceTest {

    private static CandidatService candidatService;
    private static JobPositionService jobPositionService;
    private static int idJobTest;
    private static String testEmail = "test.user@example.com";

    @BeforeAll
    static void setup() {
        candidatService = new CandidatService();
        jobPositionService = new JobPositionService();

        // Créer un JobPosition pour les tests
        JobPosition job = new JobPosition();
        job.setTitle("Test Job");
        job.setDepartement("IT");
        job.setEmployeeType("CDI");
        job.setDescription("Test Description");
        job.setStatus("Open");
        job.setPostedAt(LocalDate.now());

        jobPositionService.create(job);
        idJobTest = job.getIdJob();

        System.out.println("✅ Setup completed - Job test créé avec ID: " + idJobTest);
    }

    @Test
    @Order(1)
    void testAjouterCandidat() {
        System.out.println("🔵 Test 1: Ajout d'un candidat");

        // Créer un candidat de test
        Candidat candidat = new Candidat();
        candidat.setFirstName("Test");
        candidat.setLastName("User");
        candidat.setEmail(testEmail);
        candidat.setPhone(12345678);
        candidat.setEducationLevel("Master");
        candidat.setSkills("Java, SQL");
        candidat.setStatus("NEW");

        // Associer le JobPosition
        JobPosition job = new JobPosition();
        job.setIdJob(idJobTest);
        candidat.setJobPosition(job);

        // Ajouter le candidat
        candidatService.create(candidat);

        // Vérifier que l'ID a été généré (devrait être > 0)
        assertTrue(candidat.getId() > 0, "L'ID du candidat doit être > 0 après insertion");

        System.out.println("✅ Candidat ajouté avec ID: " + candidat.getId());
    }

    @Test
    @Order(2)
    void testGetAllCandidats() {
        System.out.println("🔵 Test 2: Récupération de tous les candidats");

        List<Candidat> candidats = candidatService.getAll();

        // Vérifier que la liste n'est pas vide
        assertFalse(candidats.isEmpty(), "La liste des candidats ne doit pas être vide");

        // Vérifier que notre candidat de test est dans la liste
        boolean found = candidats.stream()
                .anyMatch(c -> testEmail.equals(c.getEmail()));

        assertTrue(found, "Le candidat de test doit être dans la liste");

        System.out.println("✅ " + candidats.size() + " candidats trouvés");
    }

    @Test
    @Order(3)
    void testModifierCandidat() {
        System.out.println("🔵 Test 3: Modification d'un candidat");

        // Récupérer tous les candidats
        List<Candidat> candidats = candidatService.getAll();

        // Trouver notre candidat de test par email
        Candidat candidatAModifier = candidats.stream()
                .filter(c -> testEmail.equals(c.getEmail()))
                .findFirst()
                .orElse(null);

        assertNotNull(candidatAModifier, "Le candidat à modifier doit exister");

        // Sauvegarder l'ID pour référence
        int idCandidat = candidatAModifier.getId();
        System.out.println("Modification du candidat ID: " + idCandidat);

        // Modifier les données
        candidatAModifier.setFirstName("TestModifie");
        candidatAModifier.setLastName("UserModifie");
        candidatAModifier.setEmail("modifie@example.com");
        candidatAModifier.setPhone(87654321);
        candidatAModifier.setStatus("IN_REVIEW");

        // Appliquer la modification
        candidatService.update(candidatAModifier);

        // Récupérer tous les candidats à nouveau
        List<Candidat> candidatsApresModif = candidatService.getAll();

        // Vérifier que la modification a été appliquée
        boolean found = candidatsApresModif.stream()
                .anyMatch(c -> c.getId() == idCandidat &&
                        "TestModifie".equals(c.getFirstName()) &&
                        "IN_REVIEW".equals(c.getStatus()));

        assertTrue(found, "Le candidat modifié doit avoir les nouvelles valeurs");

        System.out.println("✅ Candidat modifié avec succès");
    }

    @Test
    @Order(4)
    void testSupprimerCandidat() {
        System.out.println("🔵 Test 4: Suppression d'un candidat");

        // Récupérer tous les candidats
        List<Candidat> candidats = candidatService.getAll();

        // Trouver notre candidat modifié par email
        Candidat candidatASupprimer = candidats.stream()
                .filter(c -> "modifie@example.com".equals(c.getEmail()))
                .findFirst()
                .orElse(null);

        // Si on ne trouve pas par email, essayer par firstName
        if (candidatASupprimer == null) {
            candidatASupprimer = candidats.stream()
                    .filter(c -> "TestModifie".equals(c.getFirstName()))
                    .findFirst()
                    .orElse(null);
        }

        assertNotNull(candidatASupprimer, "Le candidat à supprimer doit exister");

        int idASupprimer = candidatASupprimer.getId();
        System.out.println("Suppression du candidat ID: " + idASupprimer);

        // Supprimer le candidat
        candidatService.delete(idASupprimer);

        // Récupérer la liste après suppression
        List<Candidat> candidatsApresSuppr = candidatService.getAll();

        // Vérifier que le candidat n'est plus dans la liste
        boolean exists = candidatsApresSuppr.stream()
                .anyMatch(c -> c.getId() == idASupprimer);

        assertFalse(exists, "Le candidat ne doit plus exister après suppression");

        System.out.println("✅ Candidat supprimé avec succès");
    }

    @Test
    @Order(5)
    void testCandidatSansJob() {
        System.out.println("🔵 Test 5: Ajout d'un candidat sans JobPosition");

        // Créer un candidat sans JobPosition
        Candidat candidat = new Candidat();
        candidat.setFirstName("SansJob");
        candidat.setLastName("Test");
        candidat.setEmail("sans.job@example.com");
        candidat.setPhone(11111111);
        candidat.setEducationLevel("Bachelor");
        candidat.setSkills("Python");
        candidat.setStatus("NEW");
        candidat.setJobPosition(null); // Pas de JobPosition

        // Ajouter le candidat
        candidatService.create(candidat);

        assertTrue(candidat.getId() > 0, "L'ID du candidat doit être > 0");

        // Récupérer et vérifier
        List<Candidat> candidats = candidatService.getAll();
        boolean found = candidats.stream()
                .anyMatch(c -> "sans.job@example.com".equals(c.getEmail()));

        assertTrue(found, "Le candidat sans job doit être dans la liste");

        // Vérifier que jobPosition est null
        Candidat cTrouve = candidats.stream()
                .filter(c -> "sans.job@example.com".equals(c.getEmail()))
                .findFirst()
                .orElse(null);

        assertNotNull(cTrouve, "Le candidat doit être trouvé");
        assertNull(cTrouve.getJobPosition(), "Le jobPosition doit être null");

        // Nettoyer
        candidatService.delete(cTrouve.getId());

        System.out.println("✅ Candidat sans job ajouté et supprimé avec succès");
    }

    @AfterAll
    static void cleanUp() {
        System.out.println("🧹 Nettoyage final...");

        // Supprimer tous les candidats de test qui pourraient rester
        try {
            List<Candidat> candidats = candidatService.getAll();
            for (Candidat c : candidats) {
                if (c.getEmail() != null && (
                        c.getEmail().contains("test") ||
                                c.getEmail().contains("modifie") ||
                                c.getEmail().contains("sans.job"))) {
                    candidatService.delete(c.getId());
                    System.out.println("  Nettoyé candidat ID: " + c.getId());
                }
            }
        } catch (Exception e) {
            System.out.println("Erreur lors du nettoyage: " + e.getMessage());
        }

        // Supprimer le JobPosition de test
        if (idJobTest > 0) {
            jobPositionService.delete(idJobTest);
            System.out.println("✅ Job de test supprimé avec ID: " + idJobTest);
        }

        System.out.println("✅ Tests terminés - Base de données nettoyée");
    }
}