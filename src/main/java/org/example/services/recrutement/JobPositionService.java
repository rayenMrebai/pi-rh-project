package org.example.services.recrutement;

import org.example.interfaces.GlobalInterface;
import org.example.model.recrutement.JobPosition;
import org.example.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class JobPositionService implements GlobalInterface<JobPosition> {

    private final Connection cnx;

    public JobPositionService() {
        cnx = DatabaseConnection.getInstance().getConnection();
    }



    @Override
    public List<JobPosition> getAll() {
        List<JobPosition> list = new ArrayList<>();
        String sql = "SELECT * FROM jobposition";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Date d = rs.getDate("postedAt");
                LocalDate postedAt = (d != null) ? d.toLocalDate() : null;

                JobPosition j = new JobPosition(
                        rs.getString("title"),
                        rs.getString("departement"),
                        rs.getString("employeeType"),
                        rs.getString("description"),
                        rs.getString("status"),
                        postedAt
                );
                j.setIdJob(rs.getInt("idJob"));

                list.add(j);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    @Override
    public void create(JobPosition j) {
        String sql = "INSERT INTO jobposition (title, departement, employeeType, description, status, postedAt) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, j.getTitle());
            ps.setString(2, j.getDepartement());
            ps.setString(3, j.getEmployeeType());
            ps.setString(4, j.getDescription());
            ps.setString(5, j.getStatus());

            if (j.getPostedAt() != null) {
                ps.setDate(6, Date.valueOf(j.getPostedAt()));
            } else {
                ps.setDate(6, Date.valueOf(LocalDate.now()));
            }

            int rows = ps.executeUpdate();
            System.out.println("INSERT jobposition rows=" + rows);

            // Récupérer l'ID généré automatiquement
            if (rows > 0) {
                ResultSet generatedKeys = ps.getGeneratedKeys();
                if (generatedKeys.next()) {
                    j.setIdJob(generatedKeys.getInt(1));
                    System.out.println("ID généré: " + j.getIdJob());
                }
            }

        } catch (SQLException e) {
            System.out.println("ERROR INSERT jobposition: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    @Override
    public void update(JobPosition j) {
        String sql = "UPDATE jobposition SET title=?, departement=?, employeeType=?, description=?, status=?, postedAt=? WHERE idJob=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, j.getTitle());
            ps.setString(2, j.getDepartement());
            ps.setString(3, j.getEmployeeType());
            ps.setString(4, j.getDescription());
            ps.setString(5, j.getStatus());

            if (j.getPostedAt() != null) {
                ps.setDate(6, Date.valueOf(j.getPostedAt()));
            } else {
                ps.setDate(6, Date.valueOf(java.time.LocalDate.now()));
            }

            ps.setInt(7, j.getIdJob());

            int rows = ps.executeUpdate();
            System.out.println("UPDATE jobposition rows=" + rows);

        } catch (SQLException e) {
            System.out.println("ERROR UPDATE jobposition: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


    @Override
    public void delete(int id) {
        String sql = "DELETE FROM jobposition WHERE idJob=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ✅ Optionnel : récupérer un job par id (sans jointure)
    public JobPosition findById(int idJob) {
        String sql = "SELECT * FROM jobposition WHERE idJob=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idJob);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Date d = rs.getDate("postedAt");
                    LocalDate postedAt = (d != null) ? d.toLocalDate() : null;

                    JobPosition j = new JobPosition(
                            rs.getString("title"),
                            rs.getString("departement"),
                            rs.getString("employeeType"),
                            rs.getString("description"),
                            rs.getString("status"),
                            postedAt
                    );
                    j.setIdJob(rs.getInt("idJob"));

                    return j;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}
