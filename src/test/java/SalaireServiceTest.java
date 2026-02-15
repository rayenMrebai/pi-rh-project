package org.example.tests;

import org.example.enums.SalaireStatus;
import org.example.model.salaire.Salaire;
import org.example.model.user.UserAccount;
import org.example.services.salaire.SalaireService;
import org.example.services.user.UserAccountService;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SalaireServiceTest {

    private static SalaireService salaireService;
    private static UserAccountService userAccountService;
    private static UserAccount testUser;
    private static int createdSalaireId;

    @BeforeAll
    static void setup() {
        salaireService = new SalaireService();
        userAccountService = new UserAccountService();
        testUser = userAccountService.getById(1);
    }

    @Test
    @Order(1)
    void testCreateSalaire() {
        Salaire salaire = new Salaire(testUser, 3000.0, LocalDate.now().plusDays(30));
        salaireService.create(salaire);

        List<Salaire> salaires = salaireService.getAll();
        Salaire created = salaires.stream()
                .filter(s -> s.getUser().getId() == testUser.getId() && s.getBaseAmount() == 3000.0)
                .findFirst()
                .orElse(null);

        assertNotNull(created, "Échec de création du salaire");
        createdSalaireId = created.getId();
        assertEquals(3000.0, created.getBaseAmount(), 0.01);
    }

    @Test
    @Order(2)
    void testReadSalaire() {
        Salaire salaire = salaireService.getById(createdSalaireId);

        assertNotNull(salaire, "Salaire introuvable");
        assertEquals(3000.0, salaire.getBaseAmount(), 0.01);
        assertEquals(testUser.getId(), salaire.getUser().getId());
    }

    @Test
    @Order(3)
    void testUpdateSalaire() {
        Salaire salaire = salaireService.getById(createdSalaireId);
        salaire.setStatus(SalaireStatus.EN_COURS);
        salaire.setDatePaiement(LocalDate.now().plusDays(15));

        salaireService.update(salaire);

        Salaire updated = salaireService.getById(createdSalaireId);
        assertEquals(SalaireStatus.EN_COURS, updated.getStatus());
    }

    @Test
    @Order(4)
    void testDeleteSalaire() {
        salaireService.delete(createdSalaireId);

        Salaire deleted = salaireService.getById(createdSalaireId);
        assertNull(deleted, "Échec suppression du salaire");
    }
}