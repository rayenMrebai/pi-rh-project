import org.example.model.formation.Skill;
import org.junit.jupiter.api.*;
import org.example.services.SkillService;

import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SkillServiceTests {

    static SkillService skillService;
    static int testSkillId;  // ✅ ID dynamique récupéré après création

    @BeforeAll
    static void setUp() {
        skillService = new SkillService();
        System.out.println("=== DÉBUT DES TESTS SKILL SERVICE ===");
    }

    @Test
    @Order(1)
    void AjouterSkillTest() {
        System.out.println("\n--- Test 1: Ajouter une compétence ---");

        // Créer une nouvelle compétence
        Skill skill = new Skill("C++", "Langage de programmation", "technique",2);
        skill.setLevelRequired(5);

        // Ajouter en base
        skillService.create(skill);

        // ✅ Récupérer l'ID généré automatiquement
        testSkillId = skill.getId();

        // Vérifications
        List<Skill> skills = skillService.getAll();
        Assertions.assertNotNull(skills, "La liste ne devrait pas être null");
        Assertions.assertFalse(skills.isEmpty(), "La liste devrait contenir au moins 1 skill");
        Assertions.assertTrue(testSkillId > 0, "L'ID devrait être > 0");

        System.out.println("✅ Skill ajoutée avec ID: " + testSkillId);
    }

    @Test
    @Order(2)
    void UpdateSkillTest() {
        System.out.println("\n--- Test 2: Mettre à jour une compétence ---");

        // Récupérer la compétence créée dans le test précédent
        Skill skill = skillService.getById(testSkillId);
        Assertions.assertNotNull(skill, "Le skill avec ID " + testSkillId + " devrait exister");

        // Modifier la description
        String oldDescription = skill.getDescription();
        skill.setDescription("nouvelle description");

        // Mettre à jour en base
        skillService.update(skill);

        // Vérifier que la modification a bien été effectuée
        Skill updatedSkill = skillService.getById(testSkillId);
        Assertions.assertEquals("nouvelle description", updatedSkill.getDescription(),
                "La description devrait être mise à jour");

        System.out.println("✅ Description changée de '" + oldDescription + "' à '" + updatedSkill.getDescription() + "'");
    }

    @Test
    @Order(3)
    void DisplaySkillTest() {
        System.out.println("\n--- Test 3: Afficher une compétence ---");

        // Récupérer la compétence
        Skill skill = skillService.getById(testSkillId);
        Assertions.assertNotNull(skill, "Le skill devrait exister");
        Assertions.assertEquals("nouvelle description", skill.getDescription(),
                "La description devrait être 'nouvelle description'");

        // Afficher toutes les compétences
        System.out.println("\nAffichage de toutes les compétences :");
        skillService.displayAll();

        System.out.println("✅ Affichage réussi");
    }

    @Test
    @Order(4)
    void DeleteSkillTest() {
        System.out.println("\n--- Test 4: Supprimer une compétence ---");

        // Vérifier que la compétence existe avant suppression
        Skill skill = skillService.getById(testSkillId);
        Assertions.assertNotNull(skill, "Le skill devrait exister avant suppression");

        // Supprimer
        skillService.delete(testSkillId);

        // Vérifier que la suppression a réussi
        Skill deletedSkill = skillService.getById(testSkillId);
        Assertions.assertNull(deletedSkill, "Le skill ne devrait plus exister après suppression");

        System.out.println("✅ Skill avec ID " + testSkillId + " supprimée avec succès");
    }

    @AfterAll
    static void cleanUp() {
        System.out.println("\n=== FIN DES TESTS SKILL SERVICE ===");

        // Optionnel : Nettoyage supplémentaire si nécessaire
        // Par exemple, supprimer tous les skills de test
    }
}