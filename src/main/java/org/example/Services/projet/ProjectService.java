package org.example.Services.projet;

import org.example.interfaces.GlobalInterface;
import org.example.model.projet.Project;
import org.example.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProjectService implements GlobalInterface<Project> {

    private final Connection conn;

    public ProjectService() {
        this.conn = DatabaseConnection.getInstance().getConnection();
    }

    // ===== CREATE =====
    @Override
    public void create(Project project) {
        String sql = "INSERT INTO project (name, description, startDate, endDate, status, budget) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, project.getName());
            ps.setString(2, project.getDescription());
            ps.setDate(3, project.getStartDate() != null ? Date.valueOf(project.getStartDate()) : null);
            ps.setDate(4, project.getEndDate() != null ? Date.valueOf(project.getEndDate()) : null);
            ps.setString(5, project.getStatus());
            ps.setDouble(6, project.getBudget());

            ps.executeUpdate();

            // récupérer id généré
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                project.setProjectId(rs.getInt(1));
            }

            System.out.println("✅ Project ajouté: " + project.getName());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ===== READ =====
    @Override
    public List<Project> getAll() {
        List<Project> list = new ArrayList<>();
        String sql = "SELECT * FROM project";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Project p = new Project();
                p.setProjectId(rs.getInt("projectId"));
                p.setName(rs.getString("name"));
                p.setDescription(rs.getString("description"));
                p.setStartDate(rs.getDate("startDate") != null ? rs.getDate("startDate").toLocalDate() : null);
                p.setEndDate(rs.getDate("endDate") != null ? rs.getDate("endDate").toLocalDate() : null);
                p.setStatus(rs.getString("status"));
                p.setBudget(rs.getDouble("budget"));

                list.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ===== UPDATE =====
    @Override
    public void update(Project project) {
        String sql = "UPDATE project SET name=?, description=?, startDate=?, endDate=?, status=?, budget=? WHERE projectId=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, project.getName());
            ps.setString(2, project.getDescription());
            ps.setDate(3, project.getStartDate() != null ? Date.valueOf(project.getStartDate()) : null);
            ps.setDate(4, project.getEndDate() != null ? Date.valueOf(project.getEndDate()) : null);
            ps.setString(5, project.getStatus());
            ps.setDouble(6, project.getBudget());
            ps.setInt(7, project.getProjectId());

            ps.executeUpdate();
            System.out.println("🔁 Project mis à jour: " + project.getName());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ===== DELETE =====
    @Override
    public void delete(int id) {
        String sql = "DELETE FROM project WHERE projectId=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("🗑️ Project supprimé, ID: " + id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
