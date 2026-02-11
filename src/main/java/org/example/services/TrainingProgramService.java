package org.example.services;

import org.example.interfaces.GlobalInterface;
import org.example.model.formation.TrainingProgram;
import org.example.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TrainingProgramService implements GlobalInterface<TrainingProgram> {

    private final Connection conn = DatabaseConnection.getInstance().getConnection();

    // ========== CRUD OPERATIONS ==========

    // CREATE
    @Override
    public void create(TrainingProgram training) {
        String sql = """
            INSERT INTO TrainingProgram(title, description, duration, startDate, endDate, type)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, training.getTitle());
            ps.setString(2, training.getDescription());
            ps.setInt(3, training.getDuration());
            ps.setDate(4, new java.sql.Date(training.getStartDate().getTime()));
            ps.setDate(5, new java.sql.Date(training.getEndDate().getTime()));
            ps.setString(6, training.getType());

            ps.executeUpdate();

            // Récupérer l'ID généré
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                training.setId(rs.getInt(1));
                System.out.println("✓ Formation créée avec succès - ID: " + training.getId());
            }

        } catch (SQLException e) {
            System.out.println("Erreur create: " + e.getMessage());
        }
    }

    // READ ALL
    @Override
    public List<TrainingProgram> getAll() {
        List<TrainingProgram> trainings = new ArrayList<>();

        String sql = """
            SELECT id, title, description, duration, startDate, endDate, type
            FROM TrainingProgram
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                TrainingProgram training = mapResultSetToTrainingProgram(rs);
                trainings.add(training);
            }

        } catch (SQLException e) {
            System.out.println("Erreur getAll: " + e.getMessage());
        }

        return trainings;
    }

    // UPDATE
    @Override
    public void update(TrainingProgram training) {
        String sql = """
            UPDATE TrainingProgram 
            SET title = ?, description = ?, duration = ?, startDate = ?, endDate = ?, type = ?
            WHERE id = ?
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, training.getTitle());
            ps.setString(2, training.getDescription());
            ps.setInt(3, training.getDuration());
            ps.setDate(4, new java.sql.Date(training.getStartDate().getTime()));
            ps.setDate(5, new java.sql.Date(training.getEndDate().getTime()));
            ps.setString(6, training.getType());
            ps.setInt(7, training.getId());

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✓ Formation mise à jour avec succès");
            } else {
                System.out.println("✗ Aucune formation trouvée avec l'ID: " + training.getId());
            }

        } catch (SQLException e) {
            System.out.println("Erreur update: " + e.getMessage());
        }
    }

    // DELETE
    @Override
    public void delete(int id) {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM TrainingProgram WHERE id = ?")) {
            ps.setInt(1, id);

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✓ Formation supprimée avec succès");
            } else {
                System.out.println("✗ Aucune formation trouvée avec l'ID: " + id);
            }

        } catch (SQLException e) {
            System.out.println("Erreur delete: " + e.getMessage());
        }
    }

    // ========== HELPER METHODS ==========

    // MAP RESULTSET TO TRAININGPROGRAM
    private TrainingProgram mapResultSetToTrainingProgram(ResultSet rs) throws SQLException {
        TrainingProgram training = new TrainingProgram();

        training.setId(rs.getInt("id"));
        training.setTitle(rs.getString("title"));
        training.setDescription(rs.getString("description"));
        training.setDuration(rs.getInt("duration"));
        training.setStartDate(rs.getDate("startDate"));
        training.setEndDate(rs.getDate("endDate"));
        training.setType(rs.getString("type"));

        return training;
    }

    // ========== ADDITIONAL METHODS ==========

    // GET BY ID
    public TrainingProgram getById(int id) {
        String sql = """
            SELECT id, title, description, duration, startDate, endDate, type
            FROM TrainingProgram
            WHERE id = ?
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapResultSetToTrainingProgram(rs);
            }

        } catch (SQLException e) {
            System.out.println("Erreur getById: " + e.getMessage());
        }

        return null;
    }

    // GET BY TYPE
    public List<TrainingProgram> getByType(String type) {
        List<TrainingProgram> trainings = new ArrayList<>();

        String sql = """
            SELECT id, title, description, duration, startDate, endDate, type
            FROM TrainingProgram
            WHERE type = ?
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, type);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                TrainingProgram training = mapResultSetToTrainingProgram(rs);
                trainings.add(training);
            }

        } catch (SQLException e) {
            System.out.println("Erreur getByType: " + e.getMessage());
        }

        return trainings;
    }

    // SEARCH BY TITLE
    public List<TrainingProgram> searchByTitle(String title) {
        List<TrainingProgram> trainings = new ArrayList<>();

        String sql = """
            SELECT id, title, description, duration, startDate, endDate, type
            FROM TrainingProgram
            WHERE title LIKE ?
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + title + "%");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                TrainingProgram training = mapResultSetToTrainingProgram(rs);
                trainings.add(training);
            }

        } catch (SQLException e) {
            System.out.println("Erreur searchByTitle: " + e.getMessage());
        }

        return trainings;
    }

    // GET CURRENT TRAININGS
    public List<TrainingProgram> getCurrentTrainings() {
        List<TrainingProgram> trainings = new ArrayList<>();

        String sql = """
            SELECT id, title, description, duration, startDate, endDate, type
            FROM TrainingProgram
            WHERE CURDATE() BETWEEN startDate AND endDate
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                TrainingProgram training = mapResultSetToTrainingProgram(rs);
                trainings.add(training);
            }

        } catch (SQLException e) {
            System.out.println("Erreur getCurrentTrainings: " + e.getMessage());
        }

        return trainings;
    }

    // GET UPCOMING TRAININGS
    public List<TrainingProgram> getUpcomingTrainings() {
        List<TrainingProgram> trainings = new ArrayList<>();

        String sql = """
            SELECT id, title, description, duration, startDate, endDate, type
            FROM TrainingProgram
            WHERE startDate > CURDATE()
            ORDER BY startDate ASC
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                TrainingProgram training = mapResultSetToTrainingProgram(rs);
                trainings.add(training);
            }

        } catch (SQLException e) {
            System.out.println("Erreur getUpcomingTrainings: " + e.getMessage());
        }

        return trainings;
    }

    // DISPLAY ALL
    public void displayAll() {
        List<TrainingProgram> trainings = getAll();

        if (trainings.isEmpty()) {
            System.out.println("\n📚 Aucune formation à afficher\n");
            return;
        }

        System.out.println("\n" + "=".repeat(120));
        System.out.println("📚 LISTE DES FORMATIONS (" + trainings.size() + ")");
        System.out.println("=".repeat(120));

        for (TrainingProgram training : trainings) {
            System.out.println(training);
        }

        System.out.println("=".repeat(120) + "\n");
    }

    // DISPLAY BY TYPE
    public void displayByType(String type) {
        List<TrainingProgram> trainings = getByType(type);

        if (trainings.isEmpty()) {
            System.out.println("\n📚 Aucune formation " + type + " à afficher\n");
            return;
        }

        System.out.println("\n" + "=".repeat(120));
        System.out.println("📚 FORMATIONS " + type.toUpperCase() + " (" + trainings.size() + ")");
        System.out.println("=".repeat(120));

        for (TrainingProgram training : trainings) {
            System.out.println(training);
        }

        System.out.println("=".repeat(120) + "\n");
    }

    // DISPLAY CURRENT TRAININGS
    public void displayCurrentTrainings() {
        List<TrainingProgram> trainings = getCurrentTrainings();

        if (trainings.isEmpty()) {
            System.out.println("\n📚 Aucune formation en cours\n");
            return;
        }

        System.out.println("\n" + "=".repeat(120));
        System.out.println("📚 FORMATIONS EN COURS (" + trainings.size() + ")");
        System.out.println("=".repeat(120));

        for (TrainingProgram training : trainings) {
            System.out.println(training);
        }

        System.out.println("=".repeat(120) + "\n");
    }

    // DISPLAY UPCOMING TRAININGS
    public void displayUpcomingTrainings() {
        List<TrainingProgram> trainings = getUpcomingTrainings();

        if (trainings.isEmpty()) {
            System.out.println("\n📚 Aucune formation à venir\n");
            return;
        }

        System.out.println("\n" + "=".repeat(120));
        System.out.println("📚 FORMATIONS À VENIR (" + trainings.size() + ")");
        System.out.println("=".repeat(120));

        for (TrainingProgram training : trainings) {
            System.out.println(training);
        }

        System.out.println("=".repeat(120) + "\n");
    }
}