package org.example.services.user;

import org.example.interfaces.GlobalInterface;
import org.example.model.user.UserAccount;
import org.example.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserAccountService implements GlobalInterface<UserAccount> {

    private final Connection conn = DatabaseConnection.getInstance().getConnection();

    @Override
    public List<UserAccount> getAll() {
        List<UserAccount> users = new ArrayList<>();
        String sql = "SELECT id, name, email FROM useraccount";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                users.add(new UserAccount(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Erreur getAll users: " + e.getMessage());
        }

        return users;
    }

    @Override
    public void create(UserAccount user) {
        // À implémenter si nécessaire
    }

    @Override
    public void update(UserAccount user) {
        // À implémenter si nécessaire
    }

    @Override
    public void delete(int id) {
        // À implémenter si nécessaire
    }
}