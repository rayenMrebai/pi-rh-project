package org.example.util;

import org.mindrot.jbcrypt.BCrypt;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PasswordUpdater {
    public static void main(String[] args) {
        String plainPassword = "admin";
        String hashed = BCrypt.hashpw(plainPassword, BCrypt.gensalt());

        String sql = "UPDATE user_account SET passwordHash = ? WHERE username = 'admin'";

        // ✅ Utiliser le singleton
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hashed);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✅ Mot de passe mis à jour !");
                System.out.println("🔑 Hash : " + hashed);
            } else {
                System.out.println("⚠️ Aucun utilisateur 'admin' trouvé.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}