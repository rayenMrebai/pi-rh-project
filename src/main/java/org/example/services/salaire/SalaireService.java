package org.example.services.salaire;

import org.example.enums.SalaireStatus;
import org.example.interfaces.GlobalInterface;
import org.example.model.salaire.Salaire;
import org.example.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SalaireService implements GlobalInterface<Salaire> {

    private final Connection conn = DatabaseConnection.getInstance().getConnection();

    // fonction CREATE
    @Override
    public void create(Salaire salaire) {
        String sql = """
            INSERT INTO salaire(user_id, base_amount, bonus_amount, total_amount, status, date_paiement)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, salaire.getUserId());
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

    // fonction READ ALL
    @Override
    public List<Salaire> getAll() {
        List<Salaire> salaires = new ArrayList<>();

        String sql = """
            SELECT 
                s.id, s.user_id, s.base_amount, s.bonus_amount, s.total_amount,
                s.status, s.date_paiement,
                u.name, u.email
            FROM salaire s
            JOIN useraccount u ON s.user_id = u.id
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

    // fonction UPDATE
    @Override
    public void update(Salaire salaire) {
        String sql = """
            UPDATE salaire SET status = ?, date_paiement = ? WHERE id = ?
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, salaire.getStatus().name());
            ps.setDate(2, Date.valueOf(salaire.getDatePaiement()));
            ps.setInt(3, salaire.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erreur update: " + e.getMessage());
        }
    }

    // fonction DELETE
    @Override
    public void delete(Salaire salaire) {
        String sql = "DELETE FROM salaire WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, salaire.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erreur delete: " + e.getMessage());
        }
    }

    // fonction MAP RESULTSET TO SALAIRE
    private Salaire mapResultSetToSalaire(ResultSet rs) throws SQLException {
        Salaire salaire = new Salaire();

        salaire.setId(rs.getInt("id"));
        salaire.setUserId(rs.getInt("user_id"));
        salaire.setBaseAmount(rs.getDouble("base_amount"));
        salaire.setBonusAmount(rs.getDouble("bonus_amount"));
        salaire.setTotalAmount(rs.getDouble("total_amount"));
        salaire.setStatus(SalaireStatus.valueOf(rs.getString("status")));
        salaire.setDatePaiement(rs.getDate("date_paiement").toLocalDate());

        // User info
        salaire.setUserName(rs.getString("name"));
        salaire.setUserEmail(rs.getString("email"));

        return salaire;
    }

    // fonction GET BY ID
    public Salaire getById(int id) {
        String sql = """
            SELECT 
                s.id, s.user_id, s.base_amount, s.bonus_amount, s.total_amount,
                s.status, s.date_paiement,
                u.name, u.email
            FROM salaire s
            JOIN useraccount u ON s.user_id = u.id
            WHERE s.id = ?
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapResultSetToSalaire(rs);
            }

        } catch (SQLException e) {
            System.out.println("Erreur getById: " + e.getMessage());
        }

        return null;
    }

}
