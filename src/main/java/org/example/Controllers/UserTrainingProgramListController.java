package org.example.Controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.model.formation.TrainingProgram;
import org.example.services.QuizService;
import org.example.services.TrainingProgramService;
import org.example.util.Session;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class UserTrainingProgramListController implements Initializable {

    @FXML private Label welcomeLabel;
    @FXML private TableView<TrainingProgram> trainingsTable;
    @FXML private TableColumn<TrainingProgram, String> colId;
    @FXML private TableColumn<TrainingProgram, String> colTitle;
    @FXML private TableColumn<TrainingProgram, String> colDuration;
    @FXML private TableColumn<TrainingProgram, String> colType;
    @FXML private TableColumn<TrainingProgram, String> colStatus;
    @FXML private TableColumn<TrainingProgram, String> colQuiz;
    @FXML private TextField searchField;
    @FXML private Label footerLabel;

    private TrainingProgramService trainingService;
    private QuizService quizResultService;
    private List<TrainingProgram> allTrainings;

    private int    userId;
    private String userName;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        trainingService   = new TrainingProgramService();
        quizResultService = new QuizService();

        userId   = Session.getUserId();
        userName = Session.getUsername();
        welcomeLabel.setText("👤 Bienvenue, " + userName);

        colId.setCellValueFactory(d -> new SimpleStringProperty(
                String.format("%04d", d.getValue().getId())));
        colTitle.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getTitle()));
        colDuration.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getDuration() + " sem."));
        colType.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getType().toUpperCase()));
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getStatus() != null ? d.getValue().getStatus() : "PROGRAMMÉ"));

        // ✅ Colonne quiz — utilise TEMP_USER_ID
        colQuiz.setCellValueFactory(d -> {
            boolean taken = quizResultService.hasAlreadyTaken(userId, d.getValue().getId());
            return new SimpleStringProperty(taken ? "✅ Complété" : "📝 Disponible");
        });

        applyStyles();

        // ✅ Double-clic → ouvrir détails
        trainingsTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                TrainingProgram sel = trainingsTable.getSelectionModel().getSelectedItem();
                if (sel != null) openTrainingDetail(sel);
            }
        });

        searchField.textProperty().addListener((obs, old, nv) -> applySearch(nv));

        loadTrainings();
    }

    private void loadTrainings() {
        allTrainings = trainingService.getAll();
        trainingsTable.setItems(FXCollections.observableArrayList(allTrainings));
        footerLabel.setText(allTrainings.size() + " formations disponibles");
    }

    private void applySearch(String txt) {
        if (txt == null || txt.isEmpty()) {
            trainingsTable.setItems(FXCollections.observableArrayList(allTrainings));
            return;
        }
        String kw = txt.toLowerCase();
        trainingsTable.setItems(FXCollections.observableArrayList(
                allTrainings.stream()
                        .filter(t -> t.getTitle().toLowerCase().contains(kw) ||
                                t.getType().toLowerCase().contains(kw))
                        .collect(Collectors.toList())));
    }

    private void openTrainingDetail(TrainingProgram training) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/UserTrainingDetail.fxml"));
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(trainingsTable.getScene().getWindow());
            stage.setTitle("📚 " + training.getTitle());
            stage.setScene(new Scene(loader.load()));

            UserTrainingDetailController ctrl = loader.getController();
            // ✅ int + String au lieu de User
            ctrl.setData(userId, userName, training);

            stage.showAndWait();
            trainingsTable.refresh();
        } catch (Exception e) {
            showAlert("❌ Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleViewDetail() {
        TrainingProgram sel = trainingsTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showAlert("⚠️ Sélectionnez une formation"); return; }
        openTrainingDetail(sel);
    }

    @FXML
    private void handleLogout() {
        Session.clear();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Login.fxml"));
            Stage stage = (Stage) trainingsTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("INTEGRA – Connexion");
            stage.setResizable(false);
            stage.setMaximized(false);
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void applyStyles() {
        colStatus.setCellFactory(col -> new TableCell<TrainingProgram, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                String c;
                switch (item) {
                    case "EN COURS":  c = "#43a047"; break;
                    case "PROGRAMMÉ": c = "#1976d2"; break;
                    case "TERMINÉ":   c = "#757575"; break;
                    case "ANNULÉ":    c = "#e53935"; break;
                    case "SUSPENDU":  c = "#f57c00"; break;
                    default:          c = "#90a4ae"; break;
                }
                setStyle("-fx-background-color:" + c +
                        "; -fx-text-fill:white; -fx-font-weight:bold; -fx-alignment:CENTER;");
            }
        });

        colType.setCellFactory(col -> new TableCell<TrainingProgram, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                String c = item.contains("LIGNE")    ? "#2196f3" :
                        item.contains("HYBRIDE")  ? "#9c27b0" :
                                item.contains("WORKSHOP") ? "#ff9800" : "#f44336";
                setStyle("-fx-background-color:" + c +
                        "; -fx-text-fill:white; -fx-font-weight:bold; -fx-alignment:CENTER;");
            }
        });

        colQuiz.setCellFactory(col -> new TableCell<TrainingProgram, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                String c = item.contains("Complété") ? "#43a047" : "#1976d2";
                setStyle("-fx-background-color:" + c +
                        "; -fx-text-fill:white; -fx-font-weight:bold; -fx-alignment:CENTER;");
            }
        });
    }

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Information");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}