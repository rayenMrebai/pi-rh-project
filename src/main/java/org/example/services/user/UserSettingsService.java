package org.example.services.user;

import org.example.interfaces.GlobalInterface;
import org.example.model.user.UserSettings;
import org.example.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserSettingsService implements GlobalInterface<UserSettings> {

    @Override
    public void create(UserSettings entity) {
        String sql = "INSERT INTO user_settings (userId, theme, language, defaultModule, notificationsEnabled, dashboardLayout, accessPreferences) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, entity.getUserId());
            ps.setString(2, entity.getTheme());
            ps.setString(3, entity.getLanguage());
            ps.setString(4, entity.getDefaultModule());
            ps.setBoolean(5, entity.isNotificationsEnabled());
            ps.setString(6, entity.getDashboardLayout());
            ps.setString(7, entity.getAccessPreferences());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                entity.setSettingsId(rs.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error creating user settings: " + e.getMessage());
        }
    }

    @Override
    public List<UserSettings> getAll() {
        List<UserSettings> list = new ArrayList<>();
        String sql = "SELECT * FROM user_settings";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapSettings(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public void update(UserSettings entity) {
        String sql = "UPDATE user_settings SET theme = ?, language = ?, defaultModule = ?, notificationsEnabled = ?, dashboardLayout = ?, accessPreferences = ? WHERE userId = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, entity.getTheme());
            ps.setString(2, entity.getLanguage());
            ps.setString(3, entity.getDefaultModule());
            ps.setBoolean(4, entity.isNotificationsEnabled());
            ps.setString(5, entity.getDashboardLayout());
            ps.setString(6, entity.getAccessPreferences());
            ps.setInt(7, entity.getUserId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error updating user settings: " + e.getMessage());
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM user_settings WHERE settingsId = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error deleting user settings: " + e.getMessage());
        }
    }

    public UserSettings getSettingsByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM user_settings WHERE userId = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapSettings(rs);
            }
        }
        return null;
    }

    public void createOrUpdate(UserSettings settings) throws SQLException {
        if (getSettingsByUserId(settings.getUserId()) != null) {
            update(settings);
        } else {
            create(settings);
        }
    }

    private UserSettings mapSettings(ResultSet rs) throws SQLException {
        UserSettings settings = new UserSettings();
        settings.setSettingsId(rs.getInt("settingsId"));
        settings.setUserId(rs.getInt("userId"));
        settings.setTheme(rs.getString("theme"));
        settings.setLanguage(rs.getString("language"));
        settings.setDefaultModule(rs.getString("defaultModule"));
        settings.setNotificationsEnabled(rs.getBoolean("notificationsEnabled"));
        settings.setDashboardLayout(rs.getString("dashboardLayout"));
        settings.setAccessPreferences(rs.getString("accessPreferences"));
        return settings;
    }
}