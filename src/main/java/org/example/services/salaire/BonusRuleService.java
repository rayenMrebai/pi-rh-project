package org.example.services.salaire;

import org.example.model.salaire.Salaire;
import org.example.model.salaire.BonusRule;
import org.example.model.user.UserAccount;
import org.example.util.DatabaseConnection;
import org.example.enums.BonusRuleStatus;
import org.example.interfaces.GlobalInterface;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BonusRuleService implements GlobalInterface<BonusRule> {

    private final Connection conn = DatabaseConnection.getInstance().getConnection();

    // ❌ NE PAS FAIRE : private final SalaireService salaireService = new SalaireService();

    @Override
    public void create(BonusRule rule) {
        String sql = """
            INSERT INTO bonus_rule(salaryId, nomRegle, percentage, bonus, condition_text, status, createdAt, updatedAt)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, rule.getSalaire().getId());
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

    private BonusRule mapResultSetToBonusRule(ResultSet rs) throws SQLException {
        BonusRule rule = new BonusRule();

        rule.setId(rs.getInt("id"));
        rule.setNomRegle(rs.getString("nomRegle"));
        rule.setPercentage(rs.getDouble("percentage"));
        rule.setBonus(rs.getDouble("bonus"));
        rule.setCondition(rs.getString("condition_text"));
        rule.setStatus(BonusRuleStatus.valueOf(rs.getString("status")));
        rule.setCreatedAt(rs.getTimestamp("createdAt").toLocalDateTime());
        rule.setUpdatedAt(rs.getTimestamp("updatedAt").toLocalDateTime());

        Salaire salaire = new Salaire();
        salaire.setId(rs.getInt("salaryId"));

        rule.setSalaire(salaire);

        return rule;
    }

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

    /**
     * ⭐ Charger les règles AVEC les informations du salaire
     */
    public List<BonusRule> getRulesBySalaire(int salaireId) {
        List<BonusRule> rules = new ArrayList<>();
        String sql = """
            SELECT 
                br.id, br.nomRegle, br.percentage, br.bonus, 
                br.condition_text, br.status, br.createdAt, br.updatedAt,
                s.id as salaryId, s.baseAmount, s.bonusAmount, s.totalAmount,
                u.id as userId, u.name, u.email
            FROM bonus_rule br
            JOIN salaire s ON br.salaryId = s.id
            JOIN useraccount u ON s.userId = u.id
            WHERE br.salaryId = ?
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, salaireId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                BonusRule rule = new BonusRule();

                rule.setId(rs.getInt("id"));
                rule.setNomRegle(rs.getString("nomRegle"));
                rule.setPercentage(rs.getDouble("percentage"));
                rule.setBonus(rs.getDouble("bonus"));
                rule.setCondition(rs.getString("condition_text"));
                rule.setStatus(BonusRuleStatus.valueOf(rs.getString("status")));
                rule.setCreatedAt(rs.getTimestamp("createdAt").toLocalDateTime());
                rule.setUpdatedAt(rs.getTimestamp("updatedAt").toLocalDateTime());

                // Créer le salaire complet
                UserAccount user = new UserAccount(
                        rs.getInt("userId"),
                        rs.getString("name"),
                        rs.getString("email")
                );

                Salaire salaire = new Salaire();
                salaire.setId(rs.getInt("salaryId"));
                salaire.setUser(user);
                salaire.setBaseAmount(rs.getDouble("baseAmount"));
                salaire.setBonusAmount(rs.getDouble("bonusAmount"));
                salaire.setTotalAmount(rs.getDouble("totalAmount"));

                rule.setSalaire(salaire);
                rules.add(rule);
            }

        } catch (SQLException e) {
            System.out.println("Erreur getRulesBySalaire: " + e.getMessage());
        }

        return rules;
    }
}