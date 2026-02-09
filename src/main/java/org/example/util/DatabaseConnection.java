package org.example.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static DatabaseConnection instance;
    private Connection connection;

    // ✅ CORRECTION : formation au lieu de formation_db
    private static final String URL = "jdbc:mysql://localhost:3306/formation";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // Mettez votre mot de passe si vous en avez un

    private DatabaseConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ Connexion à la base de données réussie !");
            System.out.println("📊 Base de données : " + connection.getCatalog());
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Driver MySQL non trouvé : " + e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("❌ Erreur de connexion à la base de données : " + e.getMessage());
            System.err.println("URL : " + URL);
            System.err.println("USER : " + USER);
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
        try {
            if (connection == null || connection.isClosed()) {
                System.err.println("⚠️ Connexion fermée, tentative de reconnexion...");
                instance = new DatabaseConnection();
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la vérification de la connexion");
            e.printStackTrace();
        }
        return connection;
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("✅ Connexion fermée");
            } catch (SQLException e) {
                System.err.println("❌ Erreur lors de la fermeture : " + e.getMessage());
            }
        }
    }
}