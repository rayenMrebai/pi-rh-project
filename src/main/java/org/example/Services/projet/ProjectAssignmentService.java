package org.example.Services.projet;

import org.example.interfaces.GlobalInterface;
import org.example.model.projet.Project;
import org.example.model.projet.ProjectAssignment;
import org.example.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ProjectAssignmentService implements GlobalInterface<ProjectAssignment> {

    private final Connection conn;

    public ProjectAssignmentService() {
        this.conn = DatabaseConnection.getInstance().getConnection();
    }

    // ===== CREATE =====
    @Override
    public void create(ProjectAssignment assignment) {
        String sql = "INSERT INTO projectassignment (projectId, employeeId, role, allocationRate, assignedFrom, assignedTo) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, assignment.getProject().getProjectId());
            ps.setInt(2, assignment.getEmployeeId());
            ps.setString(3, assignment.getRole());
            ps.setInt(4, assignment.getAllocationRate());
            ps.setDate(5, assignment.getAssignedFrom() != null ? Date.valueOf(assignment.getAssignedFrom()) : null);
            ps.setDate(6, assignment.getAssignedTo() != null ? Date.valueOf(assignment.getAssignedTo()) : null);

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                assignment.setIdAssignment(rs.getInt(1)); // Récupère le vrai ID
            }

            System.out.println("✅ Affectation ajoutée pour projet ID " + assignment.getProject().getProjectId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ===== READ =====
    @Override
    public List<ProjectAssignment> getAll() {
        List<ProjectAssignment> list = new ArrayList<>();
        String sql = "SELECT pa.*, p.name as projectName FROM projectassignment pa LEFT JOIN project p ON pa.projectId = p.projectId";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Project p = new Project();
                p.setProjectId(rs.getInt("projectId"));
                p.setName(rs.getString("projectName"));

                ProjectAssignment a = new ProjectAssignment();
                a.setIdAssignment(rs.getInt("idAssignment"));
                a.setProject(p);
                a.setEmployeeId(rs.getInt("employeeId"));
                a.setRole(rs.getString("role"));
                a.setAllocationRate(rs.getInt("allocationRate"));
                a.setAssignedFrom(rs.getDate("assignedFrom") != null ? rs.getDate("assignedFrom").toLocalDate() : null);
                a.setAssignedTo(rs.getDate("assignedTo") != null ? rs.getDate("assignedTo").toLocalDate() : null);

                list.add(a);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ===== UPDATE =====
    @Override
    public void update(ProjectAssignment assignment) {
        String sql = "UPDATE projectassignment SET role=?, allocationRate=?, assignedTo=? WHERE idAssignment=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, assignment.getRole());
            ps.setInt(2, assignment.getAllocationRate());
            ps.setDate(3, assignment.getAssignedTo() != null ? Date.valueOf(assignment.getAssignedTo()) : null);
            ps.setInt(4, assignment.getIdAssignment());

            ps.executeUpdate();
            System.out.println("🔁 Affectation mise à jour, ID: " + assignment.getIdAssignment());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ===== DELETE =====
    @Override
    public void delete(int id) {
        String sql = "DELETE FROM projectassignment WHERE idAssignment=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("🗑️ Affectation supprimée, ID: " + id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Inside ProjectAssignmentService.java
    public List<ProjectAssignment> getByProjectId(int projectId) {
        List<ProjectAssignment> list = new ArrayList<>();
        String sql = "SELECT pa.*, p.name as projectName FROM projectassignment pa " +
                "LEFT JOIN project p ON pa.projectId = p.projectId " +
                "WHERE pa.projectId = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, projectId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Project p = new Project();
                p.setProjectId(rs.getInt("projectId"));
                p.setName(rs.getString("projectName"));

                ProjectAssignment a = new ProjectAssignment();
                a.setIdAssignment(rs.getInt("idAssignment"));
                a.setProject(p);
                a.setEmployeeId(rs.getInt("employeeId"));
                a.setRole(rs.getString("role"));
                a.setAllocationRate(rs.getInt("allocationRate"));
                a.setAssignedFrom(rs.getDate("assignedFrom") != null ? rs.getDate("assignedFrom").toLocalDate() : null);
                a.setAssignedTo(rs.getDate("assignedTo") != null ? rs.getDate("assignedTo").toLocalDate() : null);

                list.add(a);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
