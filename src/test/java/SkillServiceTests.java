import org.example.model.formation.Skill;
import org.example.model.formation.TrainingProgram;
import org.junit.jupiter.api.*;
import org.example.services.SkillService;
import org.example.services.TrainingProgramService;

import java.util.Date;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SkillServiceTests {

    static SkillService skillService;
    static TrainingProgramService trainingService;
    static int testSkillId;
    static int testTrainingId;

    @BeforeAll
    static void setUp() {
        skillService = new SkillService();
        trainingService = new TrainingProgramService();
        System.out.println("=== DÉBUT DES TESTS SKILL SERVICE ===");
    }

    @Test
    @Order(1)
    void AjouterSkillTest() {
        System.out.println("\n--- Test 1: Ajouter une compétence ---");
        Skill skill = new Skill("C++", "Langage de programmation", "technique", 2);
        skill.setLevelRequired(5);
        skillService.create(skill);
        testSkillId = skill.getId();

        List<Skill> skills = skillService.getAll();
        Assertions.assertNotNull(skills);
        Assertions.assertTrue(skills.size() > 0);
        System.out.println("✅ Skill ajoutée avec ID: " + testSkillId);
    }

    @Test
    @Order(2)
    void UpdateSkillTest() {
        System.out.println("\n--- Test 2: Mettre à jour une compétence ---");
        Skill skill = skillService.getById(testSkillId);
        Assertions.assertNotNull(skill);

        skill.setDescription("nouvelle description");
        skillService.update(skill);

        Skill updatedSkill = skillService.getById(testSkillId);
        Assertions.assertEquals("nouvelle description", updatedSkill.getDescription());
        System.out.println("✅ Skill mise à jour");
    }

    @Test
    @Order(3)
    void DisplaySkillTest() {
        System.out.println("\n--- Test 3: Afficher une compétence ---");
        Skill skill = skillService.getById(testSkillId);
        Assertions.assertNotNull(skill);
        skillService.displayAll();
        System.out.println("✅ Affichage réussi");
    }

    // ✅ NOUVEAU TEST : Relation One-to-Many
    @Test
    @Order(4)
    void AssignSkillToTrainingTest() {
        System.out.println("\n--- Test 4: Assigner une compétence à une formation ---");

        // Créer une formation
        TrainingProgram training = new TrainingProgram(
                "Formation C++",
                "Formation avancée en C++",
                60,
                new Date(),
                new Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000),
                "en ligne"
        );
        trainingService.create(training);
        testTrainingId = training.getId();

        // Assigner la compétence à la formation
        skillService.assignToTraining(testSkillId, testTrainingId);

        // Vérifier
        Skill skill = skillService.getById(testSkillId);
        Assertions.assertNotNull(skill.getTrainingProgramId());
        Assertions.assertEquals(testTrainingId, skill.getTrainingProgramId());

        // Vérifier qu'on peut récupérer les compétences de la formation
        List<Skill> trainingSkills = skillService.getByTrainingProgramId(testTrainingId);
        Assertions.assertTrue(trainingSkills.size() > 0);

        System.out.println("✅ Compétence assignée à la formation");
    }

    @Test
    @Order(5)
    void RemoveSkillFromTrainingTest() {
        System.out.println("\n--- Test 5: Retirer une compétence d'une formation ---");

        skillService.removeFromTraining(testSkillId);

        Skill skill = skillService.getById(testSkillId);
        Assertions.assertNull(skill.getTrainingProgramId());

        System.out.println("✅ Compétence retirée de la formation");
    }

    @Test
    @Order(6)
    void DeleteSkillTest() {
        System.out.println("\n--- Test 6: Supprimer une compétence ---");
        Skill skill = skillService.getById(testSkillId);
        Assertions.assertNotNull(skill);

        skillService.delete(testSkillId);

        Skill deletedSkill = skillService.getById(testSkillId);
        Assertions.assertNull(deletedSkill);
        System.out.println("✅ Skill supprimée");
    }

    @AfterAll
    static void cleanUp() {
        // Nettoyer la formation de test
        if (testTrainingId > 0) {
            trainingService.delete(testTrainingId);
        }
        System.out.println("\n=== FIN DES TESTS SKILL SERVICE ===");
    }
}