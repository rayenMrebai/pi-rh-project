import org.example.model.formation.TrainingProgram;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.example.services.TrainingProgramService;

import java.util.Date;
import java.util.List;

public class TrainingProgramServiceTests {
    // Variables
    static TrainingProgramService trainingService;

    @BeforeAll
    static void setUp() {
        trainingService = new TrainingProgramService();
    }

    @Test
    void AjouterTrainingProgramTest() {
        TrainingProgram training = new TrainingProgram(
                "Formation Java",
                "Formation avancée en Java",
                40,
                new Date(),
                new Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000), // +30 jours
                "en ligne"
        );
        trainingService.create(training);
        List<TrainingProgram> trainings = trainingService.getAll();
        Assertions.assertNotNull(trainings);
    }

    @Test
    void UpdateTrainingProgramTest() {
        TrainingProgram training = trainingService.getById(2);
        Assertions.assertNotNull(training);
        training.setDescription("nouvelle description");
        trainingService.update(training);
    }

    @Test
    void DisplayTrainingProgramTest() {
        TrainingProgram training = trainingService.getById(2);
        Assertions.assertNotNull(training);
        Assertions.assertEquals("nouvelle description", training.getDescription());
        trainingService.displayAll();
    }

    @Test
    void DeleteTrainingProgramTest() {
        TrainingProgram training = trainingService.getById(2);
        Assertions.assertNotNull(training);
        trainingService.delete(2);
    }

    @AfterAll
    static void cleanUp() {
        // Nettoyage si nécessaire
    }
}