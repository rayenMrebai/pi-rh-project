package org.example.services;

import org.example.interfaces.GlobalInterface;
import org.example.model.formation.Skill;
import org.example.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SkillService implements GlobalInterface<Skill> {

    private final Connection conn = DatabaseConnection.getInstance().getConnection();

    // ========== CREATE ==========
    public void create(Skill skill) {
        String sql = """
            INSERT INTO skill (nom, description, categorie, level_required, trainingprogram_id)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, skill.getNom());
            stmt.setString(2, skill.getDescription());
            stmt.setString(3, skill.getCategorie());
            stmt.setInt(4, skill.getLevelRequired());

            if (skill.getTrainingProgramId() != null) {
                stmt.setInt(5, skill.getTrainingProgramId());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }

            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                skill.setId(rs.getInt(1));
            }

            System.out.println("✓ Compétence créée - ID: " + skill.getId());
        } catch (SQLException e) {
            System.err.println("Erreur création : " + e.getMessage());
        }
    }

    // ========== READ ==========
    public List<Skill> getAll() {
        List<Skill> skills = new ArrayList<>();
        String sql = "SELECT * FROM skill";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                skills.add(mapResultSetToSkill(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur getAll : " + e.getMessage());
        }

        return skills;
    }

    public Skill getById(int id) {
        String sql = "SELECT * FROM skill WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToSkill(rs);
            }
        } catch (SQLException e) {
            System.err.println("Erreur getById : " + e.getMessage());
        }

        return null;
    }

    // ========== GET BY TRAINING PROGRAM ID ==========
    public List<Skill> getByTrainingProgramId(int trainingProgramId) {
        List<Skill> skills = new ArrayList<>();
        String sql = "SELECT * FROM skill WHERE trainingprogram_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, trainingProgramId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                skills.add(mapResultSetToSkill(rs));
            }
            System.out.println("📊 " + skills.size() + " skill(s) pour training ID=" + trainingProgramId);
        } catch (SQLException e) {
            System.err.println("Erreur getByTrainingProgramId : " + e.getMessage());
        }

        return skills;
    }

    // ========== GET UNASSIGNED ==========
    public List<Skill> getUnassignedSkills() {
        List<Skill> skills = new ArrayList<>();
        String sql = "SELECT * FROM skill WHERE trainingprogram_id IS NULL";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                skills.add(mapResultSetToSkill(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur getUnassignedSkills : " + e.getMessage());
        }

        return skills;
    }

    // ========== UPDATE ==========
    public void update(Skill skill) {
        String sql = """
            UPDATE skill
            SET nom = ?, description = ?, categorie = ?, level_required = ?, trainingprogram_id = ?
            WHERE id = ?
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, skill.getNom());
            stmt.setString(2, skill.getDescription());
            stmt.setString(3, skill.getCategorie());
            stmt.setInt(4, skill.getLevelRequired());

            if (skill.getTrainingProgramId() != null) {
                stmt.setInt(5, skill.getTrainingProgramId());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }

            stmt.setInt(6, skill.getId());
            stmt.executeUpdate();

            System.out.println("✓ Compétence mise à jour");
        } catch (SQLException e) {
            System.err.println("Erreur update : " + e.getMessage());
        }
    }

    // ========== ASSIGN TO TRAINING ==========
    public void assignToTraining(int skillId, int trainingProgramId) {
        Skill existing = getById(skillId);
        if (existing == null) {
            System.err.println("❌ Skill introuvable : ID=" + skillId);
            return;
        }

        if (existing.getTrainingProgramId() != null
                && existing.getTrainingProgramId() == trainingProgramId) {
            System.out.println("⚠️ Déjà assigné à cette formation.");
            return;
        }

        String sql = "UPDATE skill SET trainingprogram_id = ? WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, trainingProgramId);
            stmt.setInt(2, skillId);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ Skill #" + skillId + " → Training #" + trainingProgramId);
            }
        } catch (SQLException e) {
            System.err.println("Erreur assignToTraining : " + e.getMessage());
        }
    }

    // ========== REMOVE FROM TRAINING ==========
    public void removeFromTraining(int skillId) {
        String sql = "UPDATE skill SET trainingprogram_id = NULL WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, skillId);
            stmt.executeUpdate();
            System.out.println("✓ Compétence retirée de la formation");
        } catch (SQLException e) {
            System.err.println("Erreur removeFromTraining : " + e.getMessage());
        }
    }

    // ========== DELETE ==========
    public void delete(int id) {
        String sql = "DELETE FROM skill WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            System.out.println("✓ Compétence supprimée");
        } catch (SQLException e) {
            System.err.println("Erreur delete : " + e.getMessage());
        }
    }

    // ========== AUTRES ==========
    public List<Skill> getByCategory(String categorie) {
        List<Skill> skills = new ArrayList<>();
        String sql = "SELECT * FROM skill WHERE categorie = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, categorie);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                skills.add(mapResultSetToSkill(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur getByCategory : " + e.getMessage());
        }

        return skills;
    }

    public List<Skill> searchByName(String name) {
        List<Skill> skills = new ArrayList<>();
        String sql = "SELECT * FROM skill WHERE nom LIKE ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + name + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                skills.add(mapResultSetToSkill(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur searchByName : " + e.getMessage());
        }

        return skills;
    }

    public void displayAll() {
        List<Skill> skills = getAll();
        System.out.println("\n==============================================");
        System.out.println("📋 LISTE DES COMPÉTENCES (" + skills.size() + ")");
        System.out.println("==============================================");
        for (Skill skill : skills) System.out.println(skill);
        System.out.println("==============================================\n");
    }

    public void displayByTraining() {
        // ✅ Corrigé : nom de table training_program
        String sql = """
            SELECT s.*, t.title as training_title
            FROM skill s
            LEFT JOIN training_program t ON s.trainingprogram_id = t.id
            ORDER BY t.title, s.nom
            """;

        System.out.println("\n==============================================");
        System.out.println("📊 COMPÉTENCES PAR FORMATION");
        System.out.println("==============================================");

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            String currentTraining = "";
            while (rs.next()) {
                String trainingTitle = rs.getString("training_title");
                if (trainingTitle == null) trainingTitle = "❌ Non assignée";

                if (!trainingTitle.equals(currentTraining)) {
                    System.out.println("🎓 " + trainingTitle);
                    currentTraining = trainingTitle;
                }

                System.out.println("   └─ " + rs.getString("nom") +
                        " [" + rs.getString("categorie") +
                        ", niveau " + rs.getInt("level_required") + "]");
            }
        } catch (SQLException e) {
            System.err.println("Erreur displayByTraining : " + e.getMessage());
        }

        System.out.println("==============================================\n");
    }

    // ========== MAPPING ==========
    private Skill mapResultSetToSkill(ResultSet rs) throws SQLException {
        Skill skill = new Skill();
        skill.setId(rs.getInt("id"));
        skill.setNom(rs.getString("nom"));
        skill.setDescription(rs.getString("description"));
        skill.setCategorie(rs.getString("categorie"));

        // ✅ Corrigé : niveau avec le bon nom de colonne
        // On essaie les deux noms possibles
        try {
            skill.setLevelRequired(rs.getInt("level_required"));
        } catch (SQLException e) {
            try {
                skill.setLevelRequired(rs.getInt("levelRequired"));
            } catch (SQLException ex) {
                skill.setLevelRequired(1); // valeur par défaut
            }
        }

        int trainingId = rs.getInt("trainingprogram_id");
        if (!rs.wasNull()) {
            skill.setTrainingProgramId(trainingId);
        }

        return skill;
    }
}