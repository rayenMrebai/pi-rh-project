package org.example.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static DatabaseConnection instance;
    private Connection connection;

    // ✅ Corriger port et nom de base selon votre config
    private final String URL = "jdbc:mysql://localhost:3306/integra_db";
    private final String USER = "root";
    private final String PASSWORD = "";

    private DatabaseConnection() {
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connexion à la base OK !");
        } catch (SQLException e) {
            System.err.println("❌ Connexion échouée : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}