package org.example.services.prediction;

import org.example.model.salaire.Salaire;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class PredictionService {

    /**
     * Prédit les salaires moyens des N prochains mois
     * en utilisant une régression linéaire simple
     */
    public Map<String, Double> predictFutureSalaries(List<Salaire> historicalData, int monthsToPredict) {
        Map<String, Double> predictions = new LinkedHashMap<>();

        if (historicalData.isEmpty() || historicalData.size() < 3) {
            System.out.println("⚠️ Pas assez de données pour prédiction (minimum 3 mois)");
            return predictions;
        }

        // 1. Regrouper par mois et calculer les moyennes
        Map<LocalDate, Double> monthlyAverages = calculateMonthlyAverages(historicalData);

        if (monthlyAverages.size() < 3) {
            System.out.println("⚠️ Pas assez de mois distincts pour prédiction");
            return predictions;
        }

        // 2. Préparer les données pour la régression
        List<DataPoint> dataPoints = prepareDataPoints(monthlyAverages);

        // 3. Calculer la régression linéaire (y = ax + b)
        LinearRegression regression = calculateLinearRegression(dataPoints);

        // 4. Prédire les mois futurs
        LocalDate lastMonth = dataPoints.get(dataPoints.size() - 1).date;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");

        for (int i = 1; i <= monthsToPredict; i++) {
            LocalDate futureMonth = lastMonth.plusMonths(i);
            double predictedValue = regression.predict(dataPoints.size() - 1 + i);

            // S'assurer que la prédiction est positive
            predictedValue = Math.max(predictedValue, 0);

            String monthKey = futureMonth.format(formatter);
            predictions.put(monthKey, predictedValue);
        }

        return predictions;
    }

    /**
     * Calcule la moyenne des salaires par mois
     */
    private Map<LocalDate, Double> calculateMonthlyAverages(List<Salaire> salaires) {
        Map<LocalDate, List<Salaire>> groupedByMonth = salaires.stream()
                .collect(Collectors.groupingBy(s ->
                        LocalDate.of(s.getDatePaiement().getYear(), s.getDatePaiement().getMonth(), 1)
                ));

        Map<LocalDate, Double> averages = new TreeMap<>();

        for (Map.Entry<LocalDate, List<Salaire>> entry : groupedByMonth.entrySet()) {
            double average = entry.getValue().stream()
                    .mapToDouble(Salaire::getTotalAmount)
                    .average()
                    .orElse(0.0);

            averages.put(entry.getKey(), average);
        }

        return averages;
    }

    /**
     * Prépare les points de données pour la régression
     */
    private List<DataPoint> prepareDataPoints(Map<LocalDate, Double> monthlyData) {
        List<DataPoint> points = new ArrayList<>();
        int x = 0;

        for (Map.Entry<LocalDate, Double> entry : monthlyData.entrySet()) {
            points.add(new DataPoint(x++, entry.getValue(), entry.getKey()));
        }

        return points;
    }

    /**
     * Calcule la régression linéaire (y = ax + b)
     */
    private LinearRegression calculateLinearRegression(List<DataPoint> points) {
        int n = points.size();

        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;

        for (DataPoint point : points) {
            sumX += point.x;
            sumY += point.y;
            sumXY += point.x * point.y;
            sumX2 += point.x * point.x;
        }

        // Formule régression linéaire
        double a = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
        double b = (sumY - a * sumX) / n;

        return new LinearRegression(a, b);
    }

    /**
     * Calcule le coefficient de détermination R² (qualité de la prédiction)
     * Retourne un pourcentage entre 0 et 100
     */
    public double calculateR2(List<Salaire> historicalData) {
        if (historicalData.isEmpty() || historicalData.size() < 3) {
            return 0.0;
        }

        Map<LocalDate, Double> monthlyAverages = calculateMonthlyAverages(historicalData);

        if (monthlyAverages.size() < 3) {
            return 0.0;
        }

        List<DataPoint> dataPoints = prepareDataPoints(monthlyAverages);
        LinearRegression regression = calculateLinearRegression(dataPoints);

        double meanY = dataPoints.stream()
                .mapToDouble(p -> p.y)
                .average()
                .orElse(0.0);

        double totalSS = 0; // Total sum of squares
        double residualSS = 0; // Residual sum of squares

        for (int i = 0; i < dataPoints.size(); i++) {
            DataPoint point = dataPoints.get(i);
            double predicted = regression.predict(point.x);

            totalSS += Math.pow(point.y - meanY, 2);
            residualSS += Math.pow(point.y - predicted, 2);
        }

        double r2 = 1 - (residualSS / totalSS);

        // Convertir en pourcentage et limiter entre 0 et 100
        return Math.max(0, Math.min(100, r2 * 100));
    }

    /**
     * Retourne l'historique mensuel formaté pour affichage
     */
    public Map<String, Double> getHistoricalMonthlyAverages(List<Salaire> salaires) {
        Map<LocalDate, Double> monthlyAverages = calculateMonthlyAverages(salaires);
        Map<String, Double> formatted = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");

        for (Map.Entry<LocalDate, Double> entry : monthlyAverages.entrySet()) {
            formatted.put(entry.getKey().format(formatter), entry.getValue());
        }

        return formatted;
    }

    // ========== CLASSES INTERNES ==========

    private static class DataPoint {
        int x;
        double y;
        LocalDate date;

        DataPoint(int x, double y, LocalDate date) {
            this.x = x;
            this.y = y;
            this.date = date;
        }
    }

    private static class LinearRegression {
        double a; // Pente
        double b; // Ordonnée à l'origine

        LinearRegression(double a, double b) {
            this.a = a;
            this.b = b;
        }

        double predict(double x) {
            return a * x + b;
        }
    }
}