package org.example.Services.ai;

import org.example.Services.projet.ProjectAssignmentService;
import org.example.model.projet.EmployesDTO;
import org.example.model.projet.MatchResult;
import org.example.model.projet.Project;
import org.example.model.projet.ProjectAssignment;

import java.util.*;
import java.util.stream.Collectors;

public class EmployeeMatchingService {

    private final HuggingFaceService huggingFaceService = new HuggingFaceService();
    private final ProjectAssignmentService assignmentService = new ProjectAssignmentService();

    /**
     * Retourne la liste des employés triés par score décroissant pour un projet donné.
     */
    public List<MatchResult> rankEmployeesForProject(Project project,
                                                     List<EmployesDTO> employees) throws Exception {

        // 1. Profil texte du projet
        String projectProfile = buildProjectProfile(project);
        float[] projectVector = huggingFaceService.getEmbedding(projectProfile);

        List<MatchResult> results = new ArrayList<>();

        for (EmployesDTO emp : employees) {

            // Récupérer l'historique de l'employé
            List<ProjectAssignment> history = assignmentService.getByEmployeeId(emp.getUserId());

            // Allocation actuelle moyenne (si historique non vide)
            double currentAllocation = history.stream()
                    .mapToDouble(ProjectAssignment::getAllocationRate)
                    .average()
                    .orElse(0.0);

            // Exclure les employés surchargés (> 90%)
            if (currentAllocation > 90.0) continue;

            // Profil texte de l'employé
            String employeeProfile = buildEmployeeProfile(emp, history);
            float[] employeeVector = huggingFaceService.getEmbedding(employeeProfile);

            double similarity = huggingFaceService.cosineSimilarity(projectVector, employeeVector);
            String dominantRole = getDominantRole(history);

            results.add(new MatchResult(
                    emp.getUserId(),
                    emp.getUsername(),
                    dominantRole,
                    similarity,
                    currentAllocation
            ));
        }

        // Trier par score final décroissant
        results.sort(Comparator.comparingDouble(MatchResult::getFinalScore).reversed());
        return results;
    }

    private String buildProjectProfile(Project project) {
        StringBuilder sb = new StringBuilder();
        sb.append("Project: ").append(project.getName()).append(". ");
        if (project.getDescription() != null && !project.getDescription().isBlank()) {
            sb.append("Description: ").append(project.getDescription()).append(". ");
        }
        sb.append("Status: ").append(project.getStatus()).append(". ");
        return sb.toString();
    }

    private String buildEmployeeProfile(EmployesDTO emp, List<ProjectAssignment> history) {
        StringBuilder sb = new StringBuilder();
        sb.append("Employee: ").append(emp.getUsername()).append(". ");

        if (!history.isEmpty()) {
            // Rôles distincts
            String roles = history.stream()
                    .map(ProjectAssignment::getRole)
                    .filter(r -> r != null && !r.isBlank())
                    .distinct()
                    .collect(Collectors.joining(", "));
            if (!roles.isBlank()) {
                sb.append("Roles: ").append(roles).append(". ");
            }

            double avgAlloc = history.stream()
                    .mapToDouble(ProjectAssignment::getAllocationRate)
                    .average().orElse(0);
            sb.append("Average allocation: ").append(String.format("%.0f", avgAlloc)).append("%. ");
            sb.append("Number of projects: ").append(history.size()).append(". ");
        }
        return sb.toString();
    }

    private String getDominantRole(List<ProjectAssignment> history) {
        if (history == null || history.isEmpty()) return "Not assigned";
        return history.stream()
                .map(ProjectAssignment::getRole)
                .filter(r -> r != null && !r.isBlank())
                .collect(Collectors.groupingBy(r -> r, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Not assigned");
    }
}