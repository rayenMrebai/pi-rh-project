package org.example.Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.example.model.formation.TrainingProgram;
import org.example.services.TrainingProgramService;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ListTrainingProgramsController implements Initializable {

    @FXML private ListView<TrainingProgram> trainingListView;
    @FXML private Label countLabel;
    @FXML private Label statusLabel;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterType;

    private TrainingProgramService trainingService;
    private ObservableList<TrainingProgram> trainingList;
    private ObservableList<TrainingProgram> allTrainings;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        trainingService = new TrainingProgramService();

        // Configurer le filtre
        filterType.setItems(FXCollections.observableArrayList("Toutes", "en ligne", "présentiel"));
        filterType.setValue("Toutes");
        filterType.setOnAction(e -> appliquerFiltres());

        // Recherche en temps réel
        searchField.textProperty().addListener((observable, oldValue, newValue) -> appliquerFiltres());

        // Configurer le rendu personnalisé
        trainingListView.setCellFactory(param -> new TrainingListCell());

        // Charger les données
        chargerTrainings();
    }
    //Go to home
    @FXML
    private void handleGoToHome() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Home.fxml"));
            Stage stage = (Stage) trainingListView.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Accueil - Système de Gestion RH");
            stage.setMaximized(false);
        } catch (Exception e) {
            System.err.println("Erreur lors du retour à l'accueil : " + e.getMessage());
            e.printStackTrace();
        }
    }
    @FXML
    private void handleGoToAdd() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AddFormTrainingProgram.fxml"));
            Stage stage = (Stage) trainingListView.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajouter une Formation");
        } catch (Exception e) {
            afficherMessage("❌ Erreur lors de l'ouverture du formulaire", "error");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRafraichir() {
        chargerTrainings();
        afficherMessage("🔄 Liste rafraîchie avec succès", "info");
    }
    @FXML
    private void handleModifier() {
        TrainingProgram selectedTraining = trainingListView.getSelectionModel().getSelectedItem();

        if (selectedTraining == null) {
            afficherMessage("⚠️ Veuillez sélectionner une formation à modifier", "warning");
            return;
        }

        try {
            // ⚠️ VÉRIFIEZ LE NOM DU FICHIER ICI
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UpdateFormTrainingProgram.fxml"));
            Parent root = loader.load();

            UpdateFormTrainingProgramController controller = loader.getController();
            controller.setTrainingProgram(selectedTraining);

            Stage stage = (Stage) trainingListView.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier - " + selectedTraining.getTitle());

        } catch (Exception e) {
            afficherMessage("❌ Erreur lors de l'ouverture du formulaire de modification", "error");
            e.printStackTrace();  // ← Regardez les détails dans la console
        }
    }

    @FXML
    private void handleSupprimer() {
        TrainingProgram selectedTraining = trainingListView.getSelectionModel().getSelectedItem();

        if (selectedTraining == null) {
            afficherMessage("⚠️ Veuillez sélectionner une formation à supprimer", "warning");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer la formation : " + selectedTraining.getTitle());
        alert.setContentText("Cette action est irréversible. Voulez-vous continuer ?");

        if (alert.showAndWait().get() == ButtonType.OK) {
            trainingService.delete(selectedTraining.getId());
            afficherMessage("✅ Formation supprimée avec succès", "success");
            chargerTrainings();
        }
    }

    private void chargerTrainings() {
        try {
            List<TrainingProgram> trainings = trainingService.getAll();
            allTrainings = FXCollections.observableArrayList(trainings);
            trainingList = FXCollections.observableArrayList(trainings);
            trainingListView.setItems(trainingList);
            countLabel.setText(trainings.size() + " formation(s)");

            searchField.clear();
            filterType.setValue("Toutes");
        } catch (Exception e) {
            afficherMessage("❌ Erreur lors du chargement des données", "error");
            e.printStackTrace();
        }
    }

    private void appliquerFiltres() {
        String searchText = searchField.getText().toLowerCase();
        String type = filterType.getValue();

        List<TrainingProgram> filtered = allTrainings.stream()
                .filter(training -> {
                    boolean matchSearch = training.getTitle().toLowerCase().contains(searchText) ||
                            training.getDescription().toLowerCase().contains(searchText);
                    boolean matchType = type.equals("Toutes") || training.getType().equals(type);
                    return matchSearch && matchType;
                })
                .collect(Collectors.toList());

        trainingList = FXCollections.observableArrayList(filtered);
        trainingListView.setItems(trainingList);
        countLabel.setText(filtered.size() + " formation(s)");
    }

    private void afficherMessage(String message, String type) {
        statusLabel.setText(message);

        switch (type) {
            case "success": statusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;"); break;
            case "error": statusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;"); break;
            case "warning": statusLabel.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;"); break;
            case "info": statusLabel.setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;"); break;
        }

        new Thread(() -> {
            try {
                Thread.sleep(4000);
                javafx.application.Platform.runLater(() -> statusLabel.setText(""));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // ========== CLASSE INTERNE POUR PERSONNALISER L'AFFICHAGE ==========
    static class TrainingListCell extends ListCell<TrainingProgram> {
        private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        @Override
        protected void updateItem(TrainingProgram training, boolean empty) {
            super.updateItem(training, empty);

            if (empty || training == null) {
                setText(null);
                setGraphic(null);
            } else {
                VBox container = new VBox(8);
                container.setPadding(new Insets(12, 15, 12, 15));
                container.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");

                // Ligne 1 : ID et Titre
                HBox ligne1 = new HBox(10);
                ligne1.setAlignment(Pos.CENTER_LEFT);

                Label idLabel = new Label("#" + training.getId());
                idLabel.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-padding: 3 8; -fx-background-radius: 3; -fx-font-size: 11px; -fx-font-weight: bold;");

                Label titleLabel = new Label(training.getTitle());
                titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
                titleLabel.setStyle("-fx-text-fill: #2c3e50;");

                ligne1.getChildren().addAll(idLabel, titleLabel);

                // Ligne 2 : Description
                Label descriptionLabel = new Label(training.getDescription());
                descriptionLabel.setWrapText(true);
                descriptionLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 13px;");
                descriptionLabel.setMaxWidth(1000);

                // Ligne 3 : Infos (Type, Durée, Dates)
                HBox ligne3 = new HBox(20);
                ligne3.setAlignment(Pos.CENTER_LEFT);

                // Type
                Label typeLabel = new Label(training.getType());
                if (training.getType().equals("en ligne")) {
                    typeLabel.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 4 10; -fx-background-radius: 3; -fx-font-size: 12px; -fx-font-weight: bold;");
                } else {
                    typeLabel.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 4 10; -fx-background-radius: 3; -fx-font-size: 12px; -fx-font-weight: bold;");
                }

                // Durée
                Label durationLabel = new Label("⏱ " + training.getDuration() + "h");
                durationLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 12px; -fx-font-weight: bold;");

                // Dates
                String startDateStr = dateFormat.format(training.getStartDate());
                String endDateStr = dateFormat.format(training.getEndDate());
                Label datesLabel = new Label("📅 Du " + startDateStr + " au " + endDateStr);
                datesLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 12px; -fx-font-weight: bold;");

                ligne3.getChildren().addAll(typeLabel, durationLabel, datesLabel);

                container.getChildren().addAll(ligne1, descriptionLabel, ligne3);

                setGraphic(container);
            }
        }
    }
}
