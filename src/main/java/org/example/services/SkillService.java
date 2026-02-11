package org.example.services;

import org.example.interfaces.GlobalInterface;
import org.example.model.formation.Skill;
import org.example.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SkillService implements GlobalInterface<Skill> {

    private final Connection conn = DatabaseConnection.getInstance().getConnection();

    // ========== CRUD OPERATIONS ==========

    // Creation
    @Override
    public void create(Skill skill) {
        String sql = """
            INSERT INTO Skill(nom, description, categorie, levelRequired)
            VALUES (?, ?, ?, ?)
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, skill.getNom());
            ps.setString(2, skill.getDescription());
            ps.setString(3, skill.getCategorie());
            ps.setInt(4, skill.getLevelRequired());

            ps.executeUpdate();

            // Récupérer l'ID généré
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                skill.setId(rs.getInt(1));
                System.out.println("✓ Compétence créée avec succès - ID: " + skill.getId());
            }

        } catch (SQLException e) {
            System.out.println("Erreur create: " + e.getMessage());
        }
    }

    // Lecture
    @Override
    public List<Skill> getAll() {
        List<Skill> skills = new ArrayList<>();

        String sql = """
            SELECT id, nom, description, categorie, levelRequired
            FROM Skill
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Skill skill = mapResultSetToSkill(rs);
                skills.add(skill);
            }

        } catch (SQLException e) {
            System.out.println("Erreur getAll: " + e.getMessage());
        }

        return skills;
    }

    // Mise à jour
    @Override
    public void update(Skill skill) {
        String sql = """
            UPDATE Skill 
            SET nom = ?, description = ?, categorie = ?, levelRequired = ?
            WHERE id = ?
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, skill.getNom());
            ps.setString(2, skill.getDescription());
            ps.setString(3, skill.getCategorie());
            ps.setInt(4, skill.getLevelRequired());
            ps.setInt(5, skill.getId());

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✓ Compétence mise à jour avec succès");
            } else {
                System.out.println("✗ Aucune compétence trouvée avec l'ID: " + skill.getId());
            }

        } catch (SQLException e) {
            System.out.println("Erreur update: " + e.getMessage());
        }
    }

    // Suppression
    @Override
    public void delete(int id) {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM Skill WHERE id = ?")) {
            ps.setInt(1, id);

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✓ Compétence supprimée avec succès");
            } else {
                System.out.println("✗ Aucune compétence trouvée avec l'ID: " + id);
            }

        } catch (SQLException e) {
            System.out.println("Erreur delete: " + e.getMessage());
        }
    }

    // ========== HELPER METHODS ==========

    // MAP RESULTSET TO SKILL
    private Skill mapResultSetToSkill(ResultSet rs) throws SQLException {
        Skill skill = new Skill();

        skill.setId(rs.getInt("id"));
        skill.setNom(rs.getString("nom"));
        skill.setDescription(rs.getString("description"));
        skill.setCategorie(rs.getString("categorie"));
        skill.setLevelRequired(rs.getInt("levelRequired"));

        return skill;
    }

    // ========== ADDITIONAL METHODS ==========

    // GET BY ID
    public Skill getById(int id) {
        String sql = """
            SELECT id, nom, description, categorie, levelRequired
            FROM Skill
            WHERE id = ?
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapResultSetToSkill(rs);
            }

        } catch (SQLException e) {
            System.out.println("Erreur getById: " + e.getMessage());
        }

        return null;
    }

    // GET BY CATEGORY
    public List<Skill> getByCategory(String category) {
        List<Skill> skills = new ArrayList<>();

        String sql = """
            SELECT id, nom, description, categorie, levelRequired
            FROM Skill
            WHERE categorie = ?
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, category);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Skill skill = mapResultSetToSkill(rs);
                skills.add(skill);
            }

        } catch (SQLException e) {
            System.out.println("Erreur getByCategory: " + e.getMessage());
        }

        return skills;
    }

    // SEARCH BY NAME
    public List<Skill> searchByName(String name) {
        List<Skill> skills = new ArrayList<>();

        String sql = """
            SELECT id, nom, description, categorie, levelRequired
            FROM Skill
            WHERE nom LIKE ?
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + name + "%");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Skill skill = mapResultSetToSkill(rs);
                skills.add(skill);
            }

        } catch (SQLException e) {
            System.out.println("Erreur searchByName: " + e.getMessage());
        }

        return skills;
    }

    // DISPLAY ALL
    public void displayAll() {
        List<Skill> skills = getAll();

        if (skills.isEmpty()) {
            System.out.println("\n📋 Aucune compétence à afficher\n");
            return;
        }

        System.out.println("\n" + "=".repeat(100));
        System.out.println("📋 LISTE DES COMPÉTENCES (" + skills.size() + ")");
        System.out.println("=".repeat(100));

        for (Skill skill : skills) {
            System.out.println(skill);
        }

        System.out.println("=".repeat(100) + "\n");
    }

    // DISPLAY BY CATEGORY
    public void displayByCategory(String category) {
        List<Skill> skills = getByCategory(category);

        if (skills.isEmpty()) {
            System.out.println("\n📋 Aucune compétence " + category + " à afficher\n");
            return;
        }

        System.out.println("\n" + "=".repeat(100));
        System.out.println("📋 COMPÉTENCES " + category.toUpperCase() + " (" + skills.size() + ")");
        System.out.println("=".repeat(100));

        for (Skill skill : skills) {
            System.out.println(skill);
        }

        System.out.println("=".repeat(100) + "\n");
    }
}