import org.example.model.formation.Skill;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.example.services.SkillService;

import java.util.List;

public class SkillServiceTests {
    //var
    static SkillService skillService;

    @BeforeAll
    static void setUp() {
        skillService = new SkillService();
    }

    @Test
    void AjouterSkillTest() {
        Skill skill = new Skill("c++", "langage de programmation de base", "technique", 5);
        skillService.create(skill);
        List<Skill> Skill = skillService.getAll();
        Assertions.assertNotNull(Skill);
    }
    @Test
    void UpdateSkillTest() {
        Skill skill = skillService.getById(2);
        Assertions.assertNotNull(skill);
        skill.setDescription("new description");
        skillService.update(skill);

    }
    @Test
    void DisplaySkillTest() {
        Skill skill = skillService.getById(2);
        Assertions.assertNotNull(skill);
        Assertions.assertEquals("new description", skill.getDescription());
        skillService.displayAll();

    }
    @Test
    void DeleteSkillTest() {
        Skill skill = skillService.getById(2);
        Assertions.assertNotNull(skill);
        skillService.delete(2);
    }
    //nottoyage
    @AfterAll
    static void cleanUp() {
       // skillService.delete();
    }
}
