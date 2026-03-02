package org.example.services.chart;

public class SalaryStatistics {
    private int totalCount;
    private double averageSalary;
    private double totalAmount;
    private double maxSalary;
    private double minSalary;
    private int paidCount;
    private double paidPercentage;

    // Getters et Setters
    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public double getAverageSalary() {
        return averageSalary;
    }

    public void setAverageSalary(double averageSalary) {
        this.averageSalary = averageSalary;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public double getMaxSalary() {
        return maxSalary;
    }

    public void setMaxSalary(double maxSalary) {
        this.maxSalary = maxSalary;
    }

    public double getMinSalary() {
        return minSalary;
    }

    public void setMinSalary(double minSalary) {
        this.minSalary = minSalary;
    }

    public int getPaidCount() {
        return paidCount;
    }

    public void setPaidCount(int paidCount) {
        this.paidCount = paidCount;
    }

    public double getPaidPercentage() {
        return paidPercentage;
    }

    public void setPaidPercentage(double paidPercentage) {
        this.paidPercentage = paidPercentage;
    }
}