package org.example.services.user;

import org.example.enums.UserRole;
import org.example.interfaces.GlobalInterface;
import org.example.model.user.UserAccount;
import org.example.util.DatabaseConnection;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserAccountService implements GlobalInterface<UserAccount> {

    @Override
    public void create(UserAccount entity) {
        String sql = "INSERT INTO user_account (username, email, passwordHash, role, isActive, accountStatus, accountCreatedDate) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, entity.getUsername());
            ps.setString(2, entity.getEmail());
            ps.setString(3, entity.getPasswordHash());
            ps.setString(4, entity.getRole().name());
            ps.setBoolean(5, entity.isActive());
            ps.setString(6, entity.getAccountStatus());
            ps.setTimestamp(7, Timestamp.valueOf(entity.getAccountCreatedDate()));
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                entity.setUserId(rs.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error creating user: " + e.getMessage());
        }
    }

    public void createUser(UserAccount user, String plainPassword) {
        String hash = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
        System.out.println("=== createUser ===");
        System.out.println("Nom d'utilisateur : " + user.getUsername());
        System.out.println("Hash généré : " + hash);
        user.setPasswordHash(hash);
        create(user);
    }

    @Override
    public List<UserAccount> getAll() {
        List<UserAccount> users = new ArrayList<>();
        String sql = "SELECT * FROM user_account";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                users.add(mapUser(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    @Override
    public void update(UserAccount entity) {
        String sql = "UPDATE user_account SET username = ?, email = ?, role = ?, isActive = ?, accountStatus = ? WHERE userId = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, entity.getUsername());
            ps.setString(2, entity.getEmail());
            ps.setString(3, entity.getRole().name());
            ps.setBoolean(4, entity.isActive());
            ps.setString(5, entity.getAccountStatus());
            ps.setInt(6, entity.getUserId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error updating user: " + e.getMessage());
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM user_account WHERE userId = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error deleting user: " + e.getMessage());
        }
    }

    public UserAccount authenticate(String username, String password) {
        System.out.println("=== authenticate ===");
        System.out.println("Tentative pour : " + username);
        String sql = "SELECT * FROM user_account WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                UserAccount user = mapUser(rs);
                System.out.println("Hash stocké : " + user.getPasswordHash());
                boolean match = BCrypt.checkpw(password, user.getPasswordHash());
                System.out.println("Correspondance : " + match);
                if (match) {
                    updateLastLogin(user.getUserId());
                    return user;
                }
            } else {
                System.out.println("Utilisateur non trouvé.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void updateLastLogin(int userId) {
        String sql = "UPDATE user_account SET lastLogin = ? WHERE userId = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void changePassword(int userId, String newPassword) {
        String hash = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        String sql = "UPDATE user_account SET passwordHash = ? WHERE userId = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hash);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error changing password: " + e.getMessage());
        }
    }

    public void activateUser(int userId) {
        updateAccountStatus(userId, "ACTIVE");
    }

    public void deactivateUser(int userId) {
        updateAccountStatus(userId, "DISABLED");
    }

    private void updateAccountStatus(int userId, String status) {
        String sql = "UPDATE user_account SET accountStatus = ? WHERE userId = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public UserAccount getById(int id) {
        String sql = "SELECT * FROM user_account WHERE userId = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapUser(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private UserAccount mapUser(ResultSet rs) throws SQLException {
        UserAccount user = new UserAccount();
        user.setUserId(rs.getInt("userId"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("passwordHash"));
        user.setRole(UserRole.valueOf(rs.getString("role")));
        user.setActive(rs.getBoolean("isActive"));
        user.setAccountStatus(rs.getString("accountStatus"));
        Timestamp lastLoginTs = rs.getTimestamp("lastLogin");
        if (lastLoginTs != null) {
            user.setLastLogin(lastLoginTs.toLocalDateTime());
        }
        user.setAccountCreatedDate(rs.getTimestamp("accountCreatedDate").toLocalDateTime());
        return user;
    }
}