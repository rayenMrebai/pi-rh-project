package org.example.services;

import org.example.interfaces.GlobalInterface;
import org.example.model.formation.TrainingProgram;
import org.example.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TrainingProgramService implements GlobalInterface<TrainingProgram> {

    private final Connection conn = DatabaseConnection.getInstance().getConnection();

    // ========== CREATE ==========
    @Override
    public void create(TrainingProgram training) {
        String sql = """
            INSERT INTO training_program(title, description, duration, start_date, end_date, type, status)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, training.getTitle());
            ps.setString(2, training.getDescription());
            ps.setInt(3, training.getDuration());
            ps.setDate(4, new java.sql.Date(training.getStartDate().getTime()));
            ps.setDate(5, new java.sql.Date(training.getEndDate().getTime()));
            ps.setString(6, training.getType());
            ps.setString(7, training.getStatus() != null ? training.getStatus() : "PROGRAMMÉ");
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                training.setId(rs.getInt(1));
                System.out.println("✓ Formation créée - ID: " + training.getId());
            }
        } catch (SQLException e) {
            System.out.println("Erreur create: " + e.getMessage());
        }
    }

    // ========== READ ALL ==========
    @Override
    public List<TrainingProgram> getAll() {
        List<TrainingProgram> trainings = new ArrayList<>();
        String sql = "SELECT id, title, description, duration, start_date, end_date, type, status FROM training_program";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                trainings.add(mapResultSetToTrainingProgram(rs));
            }
        } catch (SQLException e) {
            System.out.println("Erreur getAll: " + e.getMessage());
        }
        return trainings;
    }

    // ========== GET BY ID ==========
    public TrainingProgram getById(int id) {
        String sql = "SELECT id, title, description, duration, start_date, end_date, type, status FROM training_program WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapResultSetToTrainingProgram(rs);
        } catch (SQLException e) {
            System.out.println("Erreur getById: " + e.getMessage());
        }
        return null;
    }

    // ========== UPDATE ==========
    @Override
    public void update(TrainingProgram training) {
        String sql = """
            UPDATE training_program
            SET title=?, description=?, duration=?, start_date=?, end_date=?, type=?, status=?
            WHERE id=?
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, training.getTitle());
            ps.setString(2, training.getDescription());
            ps.setInt(3, training.getDuration());
            ps.setDate(4, new java.sql.Date(training.getStartDate().getTime()));
            ps.setDate(5, new java.sql.Date(training.getEndDate().getTime()));
            ps.setString(6, training.getType());
            ps.setString(7, training.getStatus() != null ? training.getStatus() : "PROGRAMMÉ");
            ps.setInt(8, training.getId());
            int rows = ps.executeUpdate();
            if (rows > 0) System.out.println("✓ Formation mise à jour");
            else System.out.println("✗ Aucune formation trouvée ID: " + training.getId());
        } catch (SQLException e) {
            System.out.println("Erreur update: " + e.getMessage());
        }
    }

    // ========== UPDATE STATUS ONLY ==========
    public void updateStatus(int id, String newStatus) {
        String sql = "UPDATE training_program SET status = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, id);
            int rows = ps.executeUpdate();
            if (rows > 0) System.out.println("✅ Statut mis à jour → " + newStatus);
        } catch (SQLException e) {
            System.out.println("Erreur updateStatus: " + e.getMessage());
        }
    }

    // ========== DELETE ==========
    @Override
    public void delete(int id) {
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM training_program WHERE id = ?")) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            if (rows > 0) System.out.println("✓ Formation supprimée");
            else System.out.println("✗ Aucune formation ID: " + id);
        } catch (SQLException e) {
            System.out.println("Erreur delete: " + e.getMessage());
        }
    }

    // ========== GET BY TYPE ==========
    public List<TrainingProgram> getByType(String type) {
        List<TrainingProgram> trainings = new ArrayList<>();
        String sql = "SELECT id, title, description, duration, start_date, end_date, type, status FROM training_program WHERE type = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, type);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) trainings.add(mapResultSetToTrainingProgram(rs));
        } catch (SQLException e) {
            System.out.println("Erreur getByType: " + e.getMessage());
        }
        return trainings;
    }

    // ========== SEARCH BY TITLE ==========
    public List<TrainingProgram> searchByTitle(String title) {
        List<TrainingProgram> trainings = new ArrayList<>();
        String sql = "SELECT id, title, description, duration, start_date, end_date, type, status FROM training_program WHERE title LIKE ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + title + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) trainings.add(mapResultSetToTrainingProgram(rs));
        } catch (SQLException e) {
            System.out.println("Erreur searchByTitle: " + e.getMessage());
        }
        return trainings;
    }

    // ========== GET CURRENT ==========
    public List<TrainingProgram> getCurrentTrainings() {
        List<TrainingProgram> trainings = new ArrayList<>();
        String sql = "SELECT id, title, description, duration, start_date, end_date, type, status FROM training_program WHERE CURDATE() BETWEEN start_date AND end_date";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) trainings.add(mapResultSetToTrainingProgram(rs));
        } catch (SQLException e) {
            System.out.println("Erreur getCurrentTrainings: " + e.getMessage());
        }
        return trainings;
    }

    // ========== GET UPCOMING ==========
    public List<TrainingProgram> getUpcomingTrainings() {
        List<TrainingProgram> trainings = new ArrayList<>();
        String sql = "SELECT id, title, description, duration, start_date, end_date, type, status FROM training_program WHERE start_date > CURDATE() ORDER BY start_date ASC";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) trainings.add(mapResultSetToTrainingProgram(rs));
        } catch (SQLException e) {
            System.out.println("Erreur getUpcomingTrainings: " + e.getMessage());
        }
        return trainings;
    }

    // ========== DISPLAY ==========
    public void displayAll() {
        List<TrainingProgram> trainings = getAll();
        System.out.println("\n" + "=".repeat(80));
        System.out.println("📚 LISTE DES FORMATIONS (" + trainings.size() + ")");
        System.out.println("=".repeat(80));
        for (TrainingProgram t : trainings) System.out.println(t);
        System.out.println("=".repeat(80) + "\n");
    }

    // ========== MAPPING ==========
    private TrainingProgram mapResultSetToTrainingProgram(ResultSet rs) throws SQLException {
        TrainingProgram training = new TrainingProgram();
        training.setId(rs.getInt("id"));
        training.setTitle(rs.getString("title"));
        training.setDescription(rs.getString("description"));
        training.setDuration(rs.getInt("duration"));
        training.setStartDate(rs.getDate("start_date"));
        training.setEndDate(rs.getDate("end_date"));
        training.setType(rs.getString("type"));

        // ✅ Lire statut depuis BDD
        try {
            String status = rs.getString("status");
            training.setStatus(status != null ? status : "PROGRAMMÉ");
        } catch (SQLException e) {
            training.setStatus("PROGRAMMÉ");
        }

        return training;
    }
}