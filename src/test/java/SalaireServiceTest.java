import org.example.enums.SalaireStatus;
import org.example.model.salaire.Salaire;
import org.example.model.user.UserAccount;
import org.example.services.salaire.SalaireService;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


//excution test selon ordre
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SalaireServiceTest {

    static SalaireService salaireService;
    static Salaire testSalaire;
    static UserAccount user;

    @BeforeAll
    static void setup() {
        salaireService = new SalaireService();

        // User pour le test (id déjà existant dans ta base)
        user = new UserAccount();
        user.setId(1);
        user.setName("Mohamed Manager");
        user.setEmail("Mohamed.manager@mail.com");
    }

    @Test
    @Order(1)
    void testCreateSalaire() {
        testSalaire = new Salaire(user, 1000.0, LocalDate.now());
        salaireService.create(testSalaire);

        List<Salaire> salaires = salaireService.getAll();
        assertFalse(salaires.isEmpty(), "La liste des salaires est vide");
        assertTrue(
                salaires.stream().anyMatch(s -> s.getBaseAmount() == 1000.0),
                "le salaire crée nest pas dans la base, insertion invalide"
        );
    }

    @Test
    @Order(2)
    void testUpdateSalaire() throws SQLException {
        Salaire salaire = salaireService.getById(5);
        assertNotNull(salaire, "Le salaire n'existe pas en base");
        // nouveau donnee
        LocalDate newDatePaiement = LocalDate.of(2026, 3, 1);
        SalaireStatus status= SalaireStatus.PAYÉ;
        //modification
        salaire.setStatus(status);
        salaire.setDatePaiement(newDatePaiement);
        salaireService.update(salaire);

        // verif
        Salaire updatedSalaire = salaireService.getById(salaire.getId());
        assertNotNull(updatedSalaire, "Le salaire n'existe plus après update");
        assertEquals(SalaireStatus.PAYÉ, updatedSalaire.getStatus(), "le statut n'a pas été mis à jour");
        assertEquals(newDatePaiement, updatedSalaire.getDatePaiement(), "la date de paiement n'a pas été mise à jour");
    }

    @Test
    @Order(3)
    void testDeleteSalaire() {
        salaireService.delete(6);

        Salaire deleted = salaireService.getById(4);
        assertNull(deleted, "le salaire nest pas supprime de la base");
    }

    @AfterAll
    static void cleanUp() {
        // pour nettoyer tous les salaires test
        //salaireService.delete(testSalaire.getId());
    }
}
