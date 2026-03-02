package org.example.model.projet;

public class MatchResult {
    private int employeeId;
    private String employeeName;
    private String role;
    private double similarityScore;   // 0.0 → 1.0
    private double allocationRate;    // % actuelle
    private double finalScore;        // score ajusté multi‑critère

    public MatchResult(int employeeId, String employeeName, String role,
                       double similarityScore, double allocationRate) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.role = role;
        this.similarityScore = similarityScore;
        this.allocationRate = allocationRate;
        // Score final = 80% similarité + 20% disponibilité
        this.finalScore = (similarityScore * 0.8) + ((1.0 - allocationRate / 100.0) * 0.2);
    }

    public int getEmployeeId() { return employeeId; }
    public String getEmployeeName() { return employeeName; }
    public String getRole() { return role; }
    public double getSimilarityScore() { return similarityScore; }
    public double getAllocationRate() { return allocationRate; }
    public double getFinalScore() { return finalScore; }

    public String getSuggestedAllocation() {
        if (finalScore >= 0.85) return "80%";
        else if (finalScore >= 0.70) return "60%";
        else return "40%";
    }

    public String getMedal() {
        return "";  // géré dans le contrôleur
    }
}