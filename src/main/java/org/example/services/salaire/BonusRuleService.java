package org.example.services.salaire;


import org.example.model.salaire.BonusRule;
import org.example.util.DatabaseConnection;
import org.example.enums.BonusRuleStatus;
import org.example.interfaces.GlobalInterface;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BonusRuleService implements GlobalInterface<BonusRule> {

    private final Connection conn = DatabaseConnection.getInstance().getConnection();

    // fonction CREATE
    @Override
    public void create(BonusRule rule) {
        String sql = """
            INSERT INTO bonus_rule(salaryId, nomRegle, percentage, bonus, condition_text, status, createdAt, updatedAt)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, rule.getSalaryId());
            ps.setString(2, rule.getNomRegle());
            ps.setDouble(3, rule.getPercentage());
            ps.setDouble(4, rule.getBonus());
            ps.setString(5, rule.getCondition());
            ps.setString(6, rule.getStatus().name());
            ps.setTimestamp(7, Timestamp.valueOf(rule.getCreatedAt()));
            ps.setTimestamp(8, Timestamp.valueOf(rule.getUpdatedAt()));
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erreur create: " + e.getMessage());
        }
    }

    // fonction READ ALL
    @Override
    public List<BonusRule> getAll() {
        List<BonusRule> rules = new ArrayList<>();
        String sql = "SELECT * FROM bonus_rule";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                rules.add(mapResultSetToBonusRule(rs));
            }

        } catch (SQLException e) {
            System.out.println("Erreur getAll: " + e.getMessage());
        }

        return rules;
    }

    // fonction UPDATE
    @Override
    public void update(BonusRule rule) {
        String sql = """
            UPDATE bonus_rule
            SET nomRegle = ?, percentage = ?, condition_text = ?, bonus = ?, status = ?, updatedAt = ?
            WHERE id = ?
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rule.getNomRegle());
            ps.setDouble(2, rule.getPercentage());
            ps.setString(3, rule.getCondition());
            ps.setDouble(4, rule.getBonus());
            ps.setString(5, rule.getStatus().name());
            ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(7, rule.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erreur update: " + e.getMessage());
        }
    }

    // fonction DELETE
    @Override
    public void delete(int id) {
        String sql = "DELETE FROM bonus_rule WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erreur delete: " + e.getMessage());
        }
    }

    // MAP RESULTSET TO BONUSRULE
    private BonusRule mapResultSetToBonusRule(ResultSet rs) throws SQLException {
        BonusRule rule = new BonusRule();

        rule.setId(rs.getInt("id"));
        rule.setSalaryId(rs.getInt("salaryId"));
        rule.setNomRegle(rs.getString("nomRegle"));
        rule.setPercentage(rs.getDouble("percentage"));
        rule.setBonus(rs.getDouble("bonus"));
        rule.setCondition(rs.getString("condition_text"));
        rule.setStatus(BonusRuleStatus.valueOf(rs.getString("status")));
        rule.setCreatedAt(rs.getTimestamp("createdAt").toLocalDateTime());
        rule.setUpdatedAt(rs.getTimestamp("updatedAt").toLocalDateTime());

        return rule;
    }



    // fonction GET BY ID
    public BonusRule getById(int id) {
        String sql = "SELECT * FROM bonus_rule WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapResultSetToBonusRule(rs);
            }

        } catch (SQLException e) {
            System.out.println("Erreur getById: " + e.getMessage());
        }

        return null;
    }

    // fonction GET RULES BY SALAIRE ID
    public List<BonusRule> getRulesBySalaire(int salaireId) {
        List<BonusRule> rules = new ArrayList<>();
        String sql = "SELECT * FROM bonus_rule WHERE salaryId = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, salaireId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                rules.add(mapResultSetToBonusRule(rs));
            }

        } catch (SQLException e) {
            System.out.println("Erreur getRulesBySalaire: " + e.getMessage());
        }

        return rules;
    }
}

