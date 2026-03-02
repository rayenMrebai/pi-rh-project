package org.example.util;
import org.example.enums.UserRole;
import org.example.model.user.UserAccount;
import org.example.services.user.UserAccountService;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserTest {

    private static UserAccountService service;
    private static UserAccount testUser;

    @BeforeAll
    public static void setup() {
        service = new UserAccountService();
    }

    @Test
    @Order(1)
    public void testCreateUser() {

        // username unique pour éviter conflit


        testUser = new UserAccount();
        testUser.setUsername("rayen");
        testUser.setEmail("rayenmribai1@gmail.com");
        testUser.setRole(UserRole.ADMINISTRATEUR);
        testUser.setActive(true);
        testUser.setAccountStatus("ACTIVE");
        testUser.setAccountCreatedDate(LocalDateTime.now());

        service.createUser(testUser, "password123");

        assertTrue(testUser.getUserId() > 0, "L'utilisateur doit être inséré avec un ID généré");
    }

    @Test
    @Order(2)
    public void testAuthenticateUser() {

        assertNotNull(testUser, "Le user doit exister");

        UserAccount authenticated =
                service.authenticate(testUser.getUsername(), "password123");

        assertNotNull(authenticated, "L'authentification doit réussir");
        assertEquals(testUser.getUsername(), authenticated.getUsername());
    }

    @Test
    @Order(3)
    public void testDeleteUser() {

        assertNotNull(testUser, "Le user doit exister");

        service.delete(testUser.getUserId());

        UserAccount deleted = service.getById(testUser.getUserId());

        assertNull(deleted, "L'utilisateur doit être supprimé");
    }
}