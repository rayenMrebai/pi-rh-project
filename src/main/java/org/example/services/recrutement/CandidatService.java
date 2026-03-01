package org.example.services.recrutement;

import org.example.interfaces.GlobalInterface;
import org.example.model.recrutement.Candidat;
import org.example.model.recrutement.JobPosition;
import org.example.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.example.util.StringSimilarity;
import java.util.ArrayList;
import java.util.List;
public class CandidatService implements GlobalInterface<Candidat> {

    private final Connection cnx;

    public CandidatService() {
        cnx = DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public void create(Candidat c) {
        String sql = "INSERT INTO candidat (firstName, lastName, email, phone, educationLevel, skills, status, idJob) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getFirstName());
            ps.setString(2, c.getLastName());
            ps.setString(3, c.getEmail());
            ps.setInt(4, c.getPhone());
            ps.setString(5, c.getEducationLevel());
            ps.setString(6, c.getSkills());
            ps.setString(7, c.getStatus());

            if (c.getJobPosition() != null && c.getJobPosition().getIdJob() > 0) {
                ps.setInt(8, c.getJobPosition().getIdJob());
            } else {
                ps.setNull(8, Types.INTEGER);
            }

            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating candidat failed, no rows affected.");
            }

            // Récupérer l'ID généré automatiquement
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    c.setId(generatedKeys.getInt(1));
                    System.out.println("ID généré pour le candidat: " + c.getId());
                } else {
                    throw new SQLException("Creating candidat failed, no ID obtained.");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Candidat> getAll() {
        List<Candidat> list = new ArrayList<>();
        String sql = "SELECT id, firstName, lastName, email, phone, educationLevel, skills, status, idJob FROM candidat";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {

                Integer idJob = (Integer) rs.getObject("idJob"); // peut être null

                JobPosition jp = null;
                if (idJob != null) {
                    jp = new JobPosition();
                    jp.setIdJob(idJob);
                }

                Candidat c = new Candidat(
                        rs.getString("firstName"),
                        rs.getString("lastName"),
                        rs.getString("email"),
                        rs.getInt("phone"),
                        rs.getString("educationLevel"),
                        rs.getString("skills"),
                        rs.getString("status"),
                        jp
                );

                // ✅ TRÈS IMPORTANT
                c.setId(rs.getInt("id"));

                list.add(c);
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }
    /**
     * Recherche les candidats existants similaires au candidat passé en paramètre.
     * Compare le prénom, nom et email avec un seuil de similarité.
     */
    public List<Candidat> findDuplicates(Candidat candidat) {
        List<Candidat> all = getAll(); // À optimiser si la base est volumineuse
        List<Candidat> duplicates = new ArrayList<>();
        double seuil = 0.8; // Ajustez ce seuil selon vos besoins (0.7-0.85)

        for (Candidat existing : all) {
            if (existing.getId() == candidat.getId()) continue; // même candidat (lors d'une modification)

            double score = calculateSimilarity(existing, candidat);
            if (score >= seuil) {
                duplicates.add(existing);
            }
        }
        return duplicates;
    }

    private double calculateSimilarity(Candidat c1, Candidat c2) {
        double total = 0.0;
        int count = 0;

        if (c1.getFirstName() != null && c2.getFirstName() != null) {
            total += StringSimilarity.compare(c1.getFirstName(), c2.getFirstName());
            count++;
        }
        if (c1.getLastName() != null && c2.getLastName() != null) {
            total += StringSimilarity.compare(c1.getLastName(), c2.getLastName());
            count++;
        }
        if (c1.getEmail() != null && c2.getEmail() != null) {
            total += StringSimilarity.compare(c1.getEmail(), c2.getEmail());
            count++;
        }
        // Optionnel : comparer le téléphone
        // if (c1.getPhone() != null && c2.getPhone() != null) {
        //     total += StringSimilarity.compare(c1.getPhone(), c2.getPhone());
        //     count++;
        // }

        return count == 0 ? 0.0 : total / count;
    }
    @Override
    public void update(Candidat c) {
        String sql = "UPDATE candidat SET firstName=?, lastName=?, email=?, phone=?, educationLevel=?, skills=?, status=?, idJob=? " +
                "WHERE id=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, c.getFirstName());
            ps.setString(2, c.getLastName());
            ps.setString(3, c.getEmail());
            ps.setInt(4, c.getPhone());
            ps.setString(5, c.getEducationLevel());
            ps.setString(6, c.getSkills());
            ps.setString(7, c.getStatus());

            if (c.getJobPosition() != null && c.getJobPosition().getIdJob() > 0) {
                ps.setInt(8, c.getJobPosition().getIdJob());
            } else {
                ps.setNull(8, Types.INTEGER);
            }

            ps.setInt(9, c.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM candidat WHERE id=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}