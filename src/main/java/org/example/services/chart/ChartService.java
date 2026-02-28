package org.example.services.chart;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.example.enums.SalaireStatus;
import org.example.model.salaire.Salaire;
import org.example.services.prediction.PredictionService;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class ChartService {

    private static final String API_URL = "https://quickchart.io/chart";
    private HttpClient httpClient;
    private Gson gson;

    public ChartService() {
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new Gson();
    }

    /**
     * Génère un graphique des salaires par employé (barres)
     */
    public String generateSalaryBarChart(List<Salaire> salaires, String savePath) {
        try {
            // Limiter à 10 employés pour lisibilité
            List<Salaire> limited = salaires.stream()
                    .limit(10)
                    .collect(Collectors.toList());

            List<String> labels = limited.stream()
                    .map(s -> s.getUser().getUsername())
                    .collect(Collectors.toList());

            List<Double> data = limited.stream()
                    .map(Salaire::getTotalAmount)
                    .collect(Collectors.toList());

            // Configuration Chart.js
            JsonObject config = new JsonObject();
            config.addProperty("type", "bar");

            JsonObject dataObj = new JsonObject();
            dataObj.add("labels", gson.toJsonTree(labels));

            JsonObject dataset = new JsonObject();
            dataset.addProperty("label", "Salaire Total (TND)");
            dataset.add("data", gson.toJsonTree(data));
            dataset.addProperty("backgroundColor", "rgba(54, 162, 235, 0.8)");
            dataset.addProperty("borderColor", "rgba(54, 162, 235, 1)");
            dataset.addProperty("borderWidth", 2);

            JsonArray datasets = new JsonArray();
            datasets.add(dataset);
            dataObj.add("datasets", datasets);
            config.add("data", dataObj);

            // Options
            JsonObject options = new JsonObject();
            JsonObject scales = new JsonObject();
            JsonObject yAxis = new JsonObject();
            yAxis.addProperty("beginAtZero", true);
            scales.add("y", yAxis);
            options.add("scales", scales);

            JsonObject title = new JsonObject();
            title.addProperty("display", true);
            title.addProperty("text", "Salaires par Employé");
            title.addProperty("fontSize", 16);
            options.add("title", title);

            config.add("options", options);

            // Générer l'URL
            String chartConfig = gson.toJson(config);
            String encodedConfig = URLEncoder.encode(chartConfig, StandardCharsets.UTF_8);
            String chartUrl = API_URL + "?c=" + encodedConfig + "&width=700&height=400&backgroundColor=white";

            // Télécharger l'image
            String filename = savePath + "/chart_salaries_bar.png";
            downloadImage(chartUrl, filename);

            System.out.println("✅ Graphique barres généré : " + filename);
            return filename;

        } catch (Exception e) {
            System.err.println("❌ Erreur génération graphique barres : " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Génère un graphique de répartition par statut (camembert)
     */
    public String generateStatusPieChart(List<Salaire> salaires, String savePath) {
        try {
            Map<SalaireStatus, Long> countByStatus = salaires.stream()
                    .collect(Collectors.groupingBy(Salaire::getStatus, Collectors.counting()));

            List<String> labels = countByStatus.keySet().stream()
                    .map(SalaireStatus::name)
                    .collect(Collectors.toList());

            List<Long> data = new ArrayList<>(countByStatus.values());

            JsonObject config = new JsonObject();
            config.addProperty("type", "pie");

            JsonObject dataObj = new JsonObject();
            dataObj.add("labels", gson.toJsonTree(labels));

            JsonObject dataset = new JsonObject();
            dataset.add("data", gson.toJsonTree(data));

            List<String> colors = Arrays.asList(
                    "rgba(255, 159, 64, 0.8)",   // Orange pour CRÉÉ
                    "rgba(54, 162, 235, 0.8)",   // Bleu pour EN_COURS
                    "rgba(75, 192, 192, 0.8)"    // Vert pour PAYÉ
            );
            dataset.add("backgroundColor", gson.toJsonTree(colors));

            JsonArray datasets = new JsonArray();
            datasets.add(dataset);
            dataObj.add("datasets", datasets);
            config.add("data", dataObj);

            JsonObject options = new JsonObject();
            JsonObject title = new JsonObject();
            title.addProperty("display", true);
            title.addProperty("text", "Répartition par Statut");
            title.addProperty("fontSize", 16);
            options.add("title", title);
            config.add("options", options);

            String chartConfig = gson.toJson(config);
            String encodedConfig = URLEncoder.encode(chartConfig, StandardCharsets.UTF_8);
            String chartUrl = API_URL + "?c=" + encodedConfig + "&width=450&height=400&backgroundColor=white";

            String filename = savePath + "/chart_status_pie.png";
            downloadImage(chartUrl, filename);

            System.out.println("✅ Graphique camembert généré : " + filename);
            return filename;

        } catch (Exception e) {
            System.err.println("❌ Erreur génération graphique camembert : " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Génère un graphique de prédiction IA (ligne)
     */
    public String generatePredictionChart(List<Salaire> salaires, String savePath) {
        try {
            PredictionService predictionService = new PredictionService();

            // Historique
            Map<String, Double> historical = predictionService.getHistoricalMonthlyAverages(salaires);

            // Prédictions
            Map<String, Double> predictions = predictionService.predictFutureSalaries(salaires, 3);

            // R²
            double r2 = predictionService.calculateR2(salaires);

            // Préparer les labels
            List<String> allLabels = new ArrayList<>();
            allLabels.addAll(historical.keySet());
            allLabels.addAll(predictions.keySet());

            // Préparer les données historiques
            List<Double> historicalData = new ArrayList<>(historical.values());

            // Préparer les données prédictions (avec nulls au début)
            List<Object> predictionData = new ArrayList<>();
            for (int i = 0; i < historical.size() - 1; i++) {
                predictionData.add(null);
            }
            // Ajouter le dernier point historique pour continuité
            if (!historical.isEmpty()) {
                predictionData.add(new ArrayList<>(historical.values()).get(historical.size() - 1));
            }
            // Ajouter les prédictions
            predictionData.addAll(predictions.values());

            // Configuration
            JsonObject config = new JsonObject();
            config.addProperty("type", "line");

            JsonObject dataObj = new JsonObject();
            dataObj.add("labels", gson.toJsonTree(allLabels));

            // Dataset 1 : Historique
            JsonObject dataset1 = new JsonObject();
            dataset1.addProperty("label", "Historique");
            dataset1.add("data", gson.toJsonTree(historicalData));
            dataset1.addProperty("borderColor", "rgba(54, 162, 235, 1)");
            dataset1.addProperty("backgroundColor", "rgba(54, 162, 235, 0.1)");
            dataset1.addProperty("borderWidth", 3);
            dataset1.addProperty("pointRadius", 5);
            dataset1.addProperty("fill", false);

            // Dataset 2 : Prédiction
            JsonObject dataset2 = new JsonObject();
            dataset2.addProperty("label", "Prédiction IA");
            dataset2.add("data", gson.toJsonTree(predictionData));
            dataset2.addProperty("borderColor", "rgba(255, 99, 132, 1)");
            dataset2.addProperty("backgroundColor", "rgba(255, 99, 132, 0.1)");
            dataset2.addProperty("borderWidth", 3);
            dataset2.addProperty("borderDash", "[10, 5]");
            dataset2.addProperty("pointRadius", 5);
            dataset2.addProperty("pointStyle", "triangle");
            dataset2.addProperty("fill", false);

            JsonArray datasets = new JsonArray();
            datasets.add(dataset1);
            datasets.add(dataset2);
            dataObj.add("datasets", datasets);
            config.add("data", dataObj);

            // Options
            JsonObject options = new JsonObject();
            JsonObject title = new JsonObject();
            title.addProperty("display", true);
            title.addProperty("text", "Prédiction IA des Salaires (Fiabilité: " + String.format("%.0f%%", r2) + ")");
            title.addProperty("fontSize", 16);
            options.add("title", title);

            JsonObject scales = new JsonObject();
            JsonObject yAxis = new JsonObject();
            yAxis.addProperty("beginAtZero", false);
            scales.add("y", yAxis);
            options.add("scales", scales);

            config.add("options", options);

            String chartConfig = gson.toJson(config);
            String encodedConfig = URLEncoder.encode(chartConfig, StandardCharsets.UTF_8);
            String chartUrl = API_URL + "?c=" + encodedConfig + "&width=900&height=450&backgroundColor=white";

            String filename = savePath + "/chart_prediction_ia.png";
            downloadImage(chartUrl, filename);

            System.out.println("✅ Graphique prédiction IA généré : " + filename);
            return filename;

        } catch (Exception e) {
            System.err.println("❌ Erreur génération prédiction : " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Télécharge une image depuis une URL
     */
    private void downloadImage(String imageUrl, String savePath) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(imageUrl))
                .GET()
                .build();

        HttpResponse<InputStream> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofInputStream());

        if (response.statusCode() == 200) {
            Path path = Paths.get(savePath);
            Files.createDirectories(path.getParent());

            try (FileOutputStream fos = new FileOutputStream(savePath)) {
                response.body().transferTo(fos);
            }
        } else {
            throw new IOException("Erreur API QuickChart : " + response.statusCode());
        }
    }

    /**
     * Calcule les statistiques globales
     */
    public SalaryStatistics calculateStatistics(List<Salaire> salaires) {
        SalaryStatistics stats = new SalaryStatistics();

        if (salaires.isEmpty()) {
            return stats;
        }

        stats.setTotalCount(salaires.size());

        stats.setAverageSalary(salaires.stream()
                .mapToDouble(Salaire::getTotalAmount)
                .average()
                .orElse(0.0));

        stats.setTotalAmount(salaires.stream()
                .mapToDouble(Salaire::getTotalAmount)
                .sum());

        stats.setMaxSalary(salaires.stream()
                .mapToDouble(Salaire::getTotalAmount)
                .max()
                .orElse(0.0));

        stats.setMinSalary(salaires.stream()
                .mapToDouble(Salaire::getTotalAmount)
                .min()
                .orElse(0.0));

        long paidCount = salaires.stream()
                .filter(s -> s.getStatus() == SalaireStatus.PAYÉ)
                .count();
        stats.setPaidCount((int) paidCount);
        stats.setPaidPercentage((paidCount * 100.0) / salaires.size());

        return stats;
    }
}