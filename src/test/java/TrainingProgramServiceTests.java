import org.example.model.formation.TrainingProgram;
import org.junit.jupiter.api.*;
import org.example.services.TrainingProgramService;

import java.util.Date;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TrainingProgramServiceTests {

    static TrainingProgramService trainingService;
    static int testTrainingId;  // ✅ ID dynamique

    @BeforeAll
    static void setUp() {
        trainingService = new TrainingProgramService();
        System.out.println("=== DÉBUT DES TESTS TRAINING PROGRAM SERVICE ===");
    }

    @Test
    @Order(1)
    void AjouterTrainingProgramTest() {
        System.out.println("\n--- Test 1: Ajouter une formation ---");

        // Dates : aujourd'hui + 30 jours
        Date startDate = new Date();
        Date endDate = new Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000);

        TrainingProgram training = new TrainingProgram(
                "Formation Java",
                "Formation avancée en programmation Java",
                40,
                startDate,
                endDate,
                "en ligne"
        );

        // Ajouter en base
        trainingService.create(training);

        // ✅ Récupérer l'ID généré
        testTrainingId = training.getId();

        // Vérifications
        List<TrainingProgram> trainings = trainingService.getAll();
        Assertions.assertNotNull(trainings, "La liste ne devrait pas être null");
        Assertions.assertTrue(trainings.size() > 0, "La liste devrait contenir au moins 1 formation");
        Assertions.assertTrue(testTrainingId > 0, "L'ID devrait être > 0");

        System.out.println("✅ Formation ajoutée avec ID: " + testTrainingId);
    }

    @Test
    @Order(2)
    void UpdateTrainingProgramTest() {
        System.out.println("\n--- Test 2: Mettre à jour une formation ---");

        // Utiliser l'ID créé dans le test 1
        TrainingProgram training = trainingService.getById(testTrainingId);
        Assertions.assertNotNull(training, "La formation avec ID " + testTrainingId + " devrait exister");

        // Modifier
        String oldDescription = training.getDescription();
        training.setDescription("nouvelle description de la formation");
        training.setDuration(50);  // Changer aussi la durée

        trainingService.update(training);

        // Vérifier
        TrainingProgram updatedTraining = trainingService.getById(testTrainingId);
        Assertions.assertEquals("nouvelle description de la formation", updatedTraining.getDescription(),
                "La description devrait être mise à jour");
        Assertions.assertEquals(50, updatedTraining.getDuration(),
                "La durée devrait être 50");

        System.out.println("✅ Formation mise à jour:");
        System.out.println("   - Description: '" + oldDescription + "' → '" + updatedTraining.getDescription() + "'");
        System.out.println("   - Durée: 40h → 50h");
    }

    @Test
    @Order(3)
    void DisplayTrainingProgramTest() {
        System.out.println("\n--- Test 3: Afficher une formation ---");

        TrainingProgram training = trainingService.getById(testTrainingId);
        Assertions.assertNotNull(training, "La formation devrait exister");
        Assertions.assertEquals("nouvelle description de la formation", training.getDescription(),
                "La description devrait être 'nouvelle description de la formation'");

        System.out.println("\nAffichage de toutes les formations :");
        trainingService.displayAll();

        System.out.println("✅ Affichage réussi");
    }

    @Test
    @Order(4)
    void DeleteTrainingProgramTest() {
        System.out.println("\n--- Test 4: Supprimer une formation ---");

        // Vérifier l'existence avant suppression
        TrainingProgram training = trainingService.getById(testTrainingId);
        Assertions.assertNotNull(training, "La formation devrait exister avant suppression");

        String titre = training.getTitle();

        // Supprimer
        trainingService.delete(testTrainingId);

        // Vérifier la suppression
        TrainingProgram deletedTraining = trainingService.getById(testTrainingId);
        Assertions.assertNull(deletedTraining, "La formation ne devrait plus exister après suppression");

        System.out.println("✅ Formation '" + titre + "' (ID: " + testTrainingId + ") supprimée avec succès");
    }

    @AfterAll
    static void cleanUp() {
        System.out.println("\n=== FIN DES TESTS TRAINING PROGRAM SERVICE ===");

        // Optionnel : Supprimer les données de test restantes si nécessaire
    }
}