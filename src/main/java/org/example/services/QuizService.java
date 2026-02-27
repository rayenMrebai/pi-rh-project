package org.example.services;

import org.example.model.formation.Quiz;
import org.example.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;

public class QuizService {

    private final Connection conn = DatabaseConnection.getInstance().getConnection();

    // ✅ Sauvegarder résultat
    public void save(Quiz result) {
        String sql = """
            INSERT INTO quiz_result (user_id, training_id, score, total_questions, percentage, passed)
            VALUES (?, ?, ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, result.getUserId());
            ps.setInt(2, result.getTrainingId());
            ps.setInt(3, result.getScore());
            ps.setInt(4, result.getTotalQuestions());
            ps.setDouble(5, result.getPercentage());
            ps.setBoolean(6, result.isPassed());
            ps.executeUpdate();
            System.out.println("✅ Résultat quiz sauvegardé");
        } catch (SQLException e) {
            System.out.println("Erreur save quiz result: " + e.getMessage());
        }
    }

    // ✅ Vérifier si le user a déjà passé ce quiz
    public boolean hasAlreadyTaken(int userId, int trainingId) {
        String sql = "SELECT COUNT(*) FROM quiz_result WHERE user_id = ? AND training_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, trainingId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.out.println("Erreur hasAlreadyTaken: " + e.getMessage());
        }
        return false;
    }

    // ✅ Récupérer le résultat existant
    public Quiz getResult(int userId, int trainingId) {
        String sql = "SELECT * FROM quiz_result WHERE user_id = ? AND training_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, trainingId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Quiz r = new Quiz();
                r.setId(rs.getInt("id"));
                r.setUserId(rs.getInt("user_id"));
                r.setTrainingId(rs.getInt("training_id"));
                r.setScore(rs.getInt("score"));
                r.setTotalQuestions(rs.getInt("total_questions"));
                r.setPercentage(rs.getDouble("percentage"));
                r.setPassed(rs.getBoolean("passed"));
                Timestamp ts = rs.getTimestamp("completed_at");
                if (ts != null) r.setCompletedAt(ts.toLocalDateTime());
                return r;
            }
        } catch (SQLException e) {
            System.out.println("Erreur getResult: " + e.getMessage());
        }
        return null;
    }
}