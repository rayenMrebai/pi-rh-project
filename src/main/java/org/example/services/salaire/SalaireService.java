package org.example.services.salaire;

import org.example.model.user.UserAccount;
import org.example.enums.SalaireStatus;
import org.example.interfaces.GlobalInterface;
import org.example.model.salaire.BonusRule;
import org.example.model.salaire.Salaire;
import org.example.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SalaireService implements GlobalInterface<Salaire> {

    private final Connection conn = DatabaseConnection.getInstance().getConnection();


    @Override
    public void create(Salaire salaire) {
        String sql = """
            INSERT INTO salaire(userId, baseAmount, bonusAmount, totalAmount, status, datePaiement)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, salaire.getUser().getId());
            ps.setDouble(2, salaire.getBaseAmount());
            ps.setDouble(3, salaire.getBonusAmount());
            ps.setDouble(4, salaire.getTotalAmount());
            ps.setString(5, salaire.getStatus().name());
            ps.setDate(6, Date.valueOf(salaire.getDatePaiement()));
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erreur create: " + e.getMessage());
        }
    }

    @Override
    public List<Salaire> getAll() {
        List<Salaire> salaires = new ArrayList<>();

        String sql = """
                    SELECT 
                        s.id, s.userId, s.baseAmount, s.bonusAmount, s.totalAmount,
                        s.status, s.datePaiement,
                        s.createdAt, s.updatedAt,
                        u.name, u.email
                    FROM salaire s
                    JOIN useraccount u ON s.userId = u.id
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Salaire salaire = mapResultSetToSalaire(rs);
                salaires.add(salaire);
            }

        } catch (SQLException e) {
            System.out.println("Erreur getAll: " + e.getMessage());
        }

        return salaires;
    }

    @Override
    public void update(Salaire salaire) {
        String sql = """
            UPDATE salaire 
            SET status = ?, datePaiement = ?, updatedAt = ?
            WHERE id = ?
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, salaire.getStatus().name());
            ps.setDate(2, Date.valueOf(salaire.getDatePaiement()));
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(4, salaire.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erreur update: " + e.getMessage());
        }
    }

    public void updateBonusAndTotal(Salaire salaire) {
        String sql = """
            UPDATE salaire 
            SET bonusAmount = ?, totalAmount = ?, updatedAt = ?
            WHERE id = ?
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, salaire.getBonusAmount());
            ps.setDouble(2, salaire.getTotalAmount());
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(4, salaire.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erreur updateBonusAndTotal: " + e.getMessage());
        }
    }

    @Override
    public void delete(int id) {
        try (PreparedStatement ps =
                     conn.prepareStatement("DELETE FROM salaire WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private Salaire mapResultSetToSalaire(ResultSet rs) throws SQLException {

        UserAccount user = new UserAccount(
                rs.getInt("userId"),
                rs.getString("name"),
                rs.getString("email")
        );

        return new Salaire(
                rs.getInt("id"),
                user,
                rs.getDouble("baseAmount"),
                rs.getDouble("bonusAmount"),
                rs.getDouble("totalAmount"),
                SalaireStatus.valueOf(rs.getString("status")),
                rs.getDate("datePaiement").toLocalDate(),
                rs.getTimestamp("createdAt").toLocalDateTime(),
                rs.getTimestamp("updatedAt").toLocalDateTime()
        );
    }


    public Salaire getById(int id) {
        String sql = """
                    SELECT 
                        s.id, s.baseAmount, s.bonusAmount, s.totalAmount,
                        s.status, s.datePaiement,
                        s.createdAt, s.updatedAt,
                        u.id as userId, u.name, u.email
                    FROM salaire s
                    JOIN useraccount u ON s.userId = u.id
                    WHERE s.id = ?
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Salaire salaire = mapResultSetToSalaire(rs);

                // Créer le service
                BonusRuleService bonusRuleService = new BonusRuleService();
                List<BonusRule> rules = bonusRuleService.getRulesBySalaire(id);
                salaire.setBonusRules(rules);

                return salaire;
            }

        } catch (SQLException e) {
            System.out.println("Erreur getById: " + e.getMessage());
        }

        return null;
    }
}