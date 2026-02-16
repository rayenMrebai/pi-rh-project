package org.example.util;

import org.example.util.DatabaseConnection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import java.sql.Connection;
import java.lang.reflect.Field;


import static org.junit.jupiter.api.Assertions.*;

public class DatabaseConnectionTest {

    @BeforeEach
    @AfterEach
    public void resetSingleton() throws Exception {
        // Réinitialiser le singleton entre chaque test
        Field instance = DatabaseConnection.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, null);
    }

    @Test
    public void testGetInstance_NotNull() {
        // Act
        DatabaseConnection dbConnection = DatabaseConnection.getInstance();

        // Assert
        assertNotNull(dbConnection, "L'instance de DatabaseConnection ne doit pas être null");
    }

    @Test
    public void testSingleton_SameInstance() {
        // Act
        DatabaseConnection instance1 = DatabaseConnection.getInstance();
        DatabaseConnection instance2 = DatabaseConnection.getInstance();

        // Assert
        assertSame(instance1, instance2, "Les deux instances doivent être identiques (Singleton)");
    }

    @Test
    public void testGetConnection_NotNull() {
        // Arrange
        DatabaseConnection dbConnection = DatabaseConnection.getInstance();

        // Act
        Connection connection = dbConnection.getConnection();

        // Assert
        assertNotNull(connection, "La connexion ne doit pas être null");
    }

    @Test
    public void testConnection_IsValid() throws Exception {
        // Arrange
        DatabaseConnection dbConnection = DatabaseConnection.getInstance();
        Connection connection = dbConnection.getConnection();

        // Act & Assert
        assertNotNull(connection, "La connexion ne doit pas être null");
        assertFalse(connection.isClosed(), "La connexion ne doit pas être fermée");
        assertTrue(connection.isValid(2), "La connexion doit être valide");
    }
}