package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.example.enums.UserRole;
import org.example.model.salaire.Salaire;
import org.example.model.user.UserAccount;
import org.example.services.chart.ChartService;
import org.example.services.chart.SalaryStatistics;
import org.example.services.prediction.PredictionService;
import org.example.services.salaire.SalaireService;
import org.example.util.SessionManager;

import java.io.File;
import java.util.List;
import java.util.Map;

public class StatisticsController {

    // Navbar
    @FXML private Button btnNavSalaires;
    @FXML private Button btnNavStatistics;

    // Statistiques
    @FXML private Label lblTotalCount;
    @FXML private Label lblAverage;
    @FXML private Label lblTotal;
    @FXML private Label lblPaidPercentage;

    // Graphiques
    @FXML private ImageView imgBarChart;
    @FXML private ImageView imgPieChart;
    @FXML private ImageView imgPredictionChart;

    // Prédictions
    @FXML private Label lblPrediction1;
    @FXML private Label lblPrediction2;
    @FXML private Label lblPrediction3;
    @FXML private Label lblConfidence;

    @FXML private Label lblCurrentUser;
    @FXML private Label lblCurrentRole;
    @FXML private Label lblModuleTitle;

    @FXML private Button btnRefresh;
    @FXML private ProgressIndicator progressIndicator;

    private SalaireService salaireService;
    private ChartService chartService;
    private PredictionService predictionService;


    @FXML
    public void initialize() {
        salaireService = new SalaireService();
        chartService = new ChartService();
        predictionService = new PredictionService();

        progressIndicator.setVisible(false);
        displayCurrentUserInfo();
        loadStatistics();
    }



    private void displayCurrentUserInfo() {
        // ✅ FIX : Vérifier que les labels sont injectés ET que la session est active
        if (lblCurrentUser == null || lblCurrentRole == null || lblModuleTitle == null) {
            System.out.println("⚠️ Labels non injectés (vérifier fx:id dans FXML)");
            return;
        }

        if (SessionManager.isLoggedIn()) {
            UserAccount currentUser = SessionManager.getCurrentUser();
            UserRole currentRole = SessionManager.getCurrentRole();

            lblCurrentUser.setText(currentUser.getUsername());
            lblCurrentRole.setText(currentRole.toString());

            if (SessionManager.isAdmin()) {
                lblModuleTitle.setText("Administrator / RH Module");
            } else if (SessionManager.isManager()) {
                lblModuleTitle.setText("Manager / Consultation Salaires");
            } else {
                lblModuleTitle.setText("Consultation de mes salaires");
            }
        } else {
            lblCurrentUser.setText("Non connecté");
            lblCurrentRole.setText("-");
            lblModuleTitle.setText("Mode Test");
        }
    }

    /**
     * ⭐ NOUVEAU : Navigation vers Gestion Salaires
     */
    @FXML
    private void handleNavSalaires() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/SalaireManagement.fxml"));
            Parent root = loader.load();

            Stage currentStage = (Stage) btnNavSalaires.getScene().getWindow();
            Scene scene = new Scene(root);
            currentStage.setScene(scene);
            currentStage.setTitle("INTEGRA - Gestion Salaires");

            System.out.println("✅ Navigation vers Gestion Salaires");

        } catch (Exception e) {
            System.err.println("❌ Erreur navigation : " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la gestion des salaires", Alert.AlertType.ERROR);
        }
    }

    /**
     * ⭐ NOUVEAU : Navigation vers Statistiques (déjà là, on reste)
     */
    @FXML
    private void handleNavStatistics() {
        System.out.println("Déjà sur Statistiques & IA");
    }

    @FXML
    private void handleRefresh() {
        loadStatistics();
    }

    private void loadStatistics() {
        btnRefresh.setDisable(true);
        progressIndicator.setVisible(true);

        new Thread(() -> {
            try {
                List<Salaire> salaires = salaireService.getAll();

                if (salaires.isEmpty()) {
                    javafx.application.Platform.runLater(() -> {
                        showAlert("Info", "Aucune donnée. Créez d'abord des salaires.", Alert.AlertType.INFORMATION);
                        progressIndicator.setVisible(false);
                        btnRefresh.setDisable(false);
                    });
                    return;
                }

                SalaryStatistics stats = chartService.calculateStatistics(salaires);

                javafx.application.Platform.runLater(() -> {
                    lblTotalCount.setText(String.valueOf(stats.getTotalCount()));
                    lblAverage.setText(String.format("%.0f TND", stats.getAverageSalary()));
                    lblTotal.setText(String.format("%.0f TND", stats.getTotalAmount()));
                    lblPaidPercentage.setText(String.format("%.0f%%", stats.getPaidPercentage()));
                });

                String tempDir = System.getProperty("java.io.tmpdir");

                String barChartPath = chartService.generateSalaryBarChart(salaires, tempDir);
                String pieChartPath = chartService.generateStatusPieChart(salaires, tempDir);
                String predictionChartPath = chartService.generatePredictionChart(salaires, tempDir);

                javafx.application.Platform.runLater(() -> {
                    if (barChartPath != null) {
                        imgBarChart.setImage(new Image(new File(barChartPath).toURI().toString()));
                    }
                    if (pieChartPath != null) {
                        imgPieChart.setImage(new Image(new File(pieChartPath).toURI().toString()));
                    }
                    if (predictionChartPath != null) {
                        imgPredictionChart.setImage(new Image(new File(predictionChartPath).toURI().toString()));
                    }
                });

                Map<String, Double> predictions = predictionService.predictFutureSalaries(salaires, 3);
                double r2 = predictionService.calculateR2(salaires);

                javafx.application.Platform.runLater(() -> {
                    if (predictions.size() >= 3) {
                        List<Map.Entry<String, Double>> predList = new java.util.ArrayList<>(predictions.entrySet());

                        lblPrediction1.setText(predList.get(0).getKey() + " : ~" +
                                String.format("%.0f TND", predList.get(0).getValue()));
                        lblPrediction2.setText(predList.get(1).getKey() + " : ~" +
                                String.format("%.0f TND", predList.get(1).getValue()));
                        lblPrediction3.setText(predList.get(2).getKey() + " : ~" +
                                String.format("%.0f TND", predList.get(2).getValue()));

                        lblConfidence.setText(String.format("Fiabilité : %.0f%%", r2));

                        if (r2 >= 70) {
                            lblConfidence.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
                        } else if (r2 >= 50) {
                            lblConfidence.setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
                        } else {
                            lblConfidence.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                        }
                    } else {
                        lblPrediction1.setText("Données insuffisantes");
                        lblPrediction2.setText("");
                        lblPrediction3.setText("");
                        lblConfidence.setText("Minimum 3 mois requis");
                    }

                    progressIndicator.setVisible(false);
                    btnRefresh.setDisable(false);
                });

            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    showAlert("Erreur", "Erreur : " + e.getMessage(), Alert.AlertType.ERROR);
                    progressIndicator.setVisible(false);
                    btnRefresh.setDisable(false);
                });
            }
        }).start();
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}