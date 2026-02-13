package org.example.util;

import org.example.enums.BonusRuleStatus;
import org.example.enums.SalaireStatus;
import org.example.model.salaire.BonusRule;
import org.example.model.salaire.Salaire;
import org.example.services.salaire.BonusRuleService;
import org.example.services.salaire.SalaireService;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RegleBonusServiceTest {

    private static BonusRuleService bonusRuleService;
    private static SalaireService testSaService;
    private static Salaire testSalaire;
    private static int createdRuleId;

    @BeforeAll
    static void setup() throws SQLException {
        bonusRuleService = new BonusRuleService();
        testSaService = new SalaireService();

        // appel salaire pour test
        testSalaire = testSaService.getById(5);
    }

    @Test
    @Order(1)
    void testCreateBonusRule() {
        BonusRule rule = new BonusRule(testSalaire, "TestRule", 10, "Condition spéciale");
        bonusRuleService.create(rule);

        // verf regle exist apres creation
        BonusRule createdRule = bonusRuleService.getRulesBySalaire(testSalaire.getId())
                .stream()
                .filter(r -> r.getNomRegle().equals("TestRule"))
                .findFirst()
                .orElse(null);

        assertNotNull(createdRule, "Échec de création de la règle");
        createdRuleId = createdRule.getId(); // On garde l'ID pour les tests suivants
    }

    @Test
    @Order(2)
    void testReadBonusRule() {
        BonusRule rule = bonusRuleService.getById(createdRuleId);
        assertNotNull(rule, "Règle introuvable après création");
        assertEquals("TestRule", rule.getNomRegle(), "Nom de règle incorrect");
        assertEquals(10, rule.getPercentage(), 0.001, "Pourcentage incorrect");
    }

    @Test
    @Order(3)
    void testUpdateBonusRule() {
        BonusRule rule = bonusRuleService.getById(createdRuleId);
        assertNotNull(rule, "Règle introuvable avant update");

        // On modifie les champs
        rule.setNomRegle("TestRuleUpdated");
        rule.setPercentage(15);
        rule.setCondition("Nouvelle condition");
        rule.setStatus(BonusRuleStatus.ACTIVE);

        bonusRuleService.update(rule);

        // Vérification
        BonusRule updatedRule = bonusRuleService.getById(createdRuleId);
        assertEquals("TestRuleUpdated", updatedRule.getNomRegle(), "Échec update nomRegle");
        assertEquals(15, updatedRule.getPercentage(), 0.001, "Échec update percentage");
        assertEquals("Nouvelle condition", updatedRule.getCondition(), "Échec update condition");
        assertEquals(BonusRuleStatus.ACTIVE, updatedRule.getStatus(), "Échec update status");
    }
    /*
    @Test
    @Order(4)
    void testDeleteBonusRule() {
        bonusRuleService.delete(createdRuleId);

        BonusRule deletedRule = bonusRuleService.getById(createdRuleId);
        assertNull(deletedRule, "Échec suppression de la règle");
    }*/
}
