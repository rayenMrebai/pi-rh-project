package org.example.Controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.model.formation.TrainingProgram;
import org.example.model.user.UserAccount;
import org.example.services.TrainingProgramService;
import org.example.util.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class UserTrainingProgramListController implements Initializable {

    // ===================== FXML FIELDS =====================

    @FXML private Label welcomeLabel;
    @FXML private Label footerLabel;

    @FXML private TextField searchField;

    @FXML private TableView<TrainingProgram> trainingsTable;
    @FXML private TableColumn<TrainingProgram, String> colId;
    @FXML private TableColumn<TrainingProgram, String> colTitle;
    @FXML private TableColumn<TrainingProgram, String> colDuration;
    @FXML private TableColumn<TrainingProgram, String> colType;
    @FXML private TableColumn<TrainingProgram, String> colStatus;
    @FXML private TableColumn<TrainingProgram, String> colQuiz;

    // ===================== FIELDS =====================

    private TrainingProgramService trainingService;
    private ObservableList<TrainingProgram> allTrainings;
    private UserAccount loggedInUser;

    // ===================== SET USER =====================

    public void setLoggedInUser(UserAccount user) {
        this.loggedInUser = user;
        if (welcomeLabel != null && user != null) {
            welcomeLabel.setText("👤 Bienvenue, " + user.getUsername()
                    + " (" + user.getRole() + ")");
        }
        updateFooter();
    }

    // ===================== INITIALIZE =====================

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        trainingService = new TrainingProgramService();

        // Colonnes
        colId.setCellValueFactory(d -> new SimpleStringProperty(
                String.format("%04d", d.getValue().getId())));
        colTitle.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getTitle()));
        colDuration.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getDuration() + " sem."));
        colType.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getType().toUpperCase()));
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(
                getStatus(d.getValue())));
        colQuiz.setCellValueFactory(d -> new SimpleStringProperty(
                "📝 Quiz"));

        // Style colonnes Status et Quiz
        applyStatusCellStyle();
        applyQuizCellStyle();

        // Double-clic → détails
        trainingsTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                handleViewDetail();
            }
        });

        // Recherche
        searchField.textProperty().addListener((obs, old, nv) -> applyFilter(nv));

        loadTrainings();
        updateFooter();
    }

    // ===================== LOAD DATA =====================

    private void loadTrainings() {
        try {
            allTrainings = FXCollections.observableArrayList(trainingService.getAll());
            trainingsTable.setItems(allTrainings);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void applyFilter(String text) {
        if (text == null || text.isEmpty()) {
            trainingsTable.setItems(allTrainings);
            return;
        }
        String lower = text.toLowerCase();
        trainingsTable.setItems(FXCollections.observableArrayList(
                allTrainings.stream()
                        .filter(t -> t.getTitle().toLowerCase().contains(lower)
                                || t.getType().toLowerCase().contains(lower))
                        .collect(Collectors.toList())));
    }

    // ===================== ACTIONS =====================

    @FXML
    private void handleViewDetail() {
        TrainingProgram selected = trainingsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Attention",
                    "Veuillez sélectionner une formation.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/TrainingDetail.fxml"));
            Parent root = loader.load();

            // Si votre controller de détail existe, passer la formation :
            // TrainingDetailController ctrl = loader.getController();
            // ctrl.setTraining(selected);
            // ctrl.setLoggedInUser(loggedInUser);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Détail : " + selected.getTitle());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            // Si le fichier n'existe pas encore, afficher les infos en alerte
            showAlert(Alert.AlertType.INFORMATION, selected.getTitle(),
                    "Formation : " + selected.getTitle()
                            + "\nDurée : " + selected.getDuration() + " semaines"
                            + "\nType : " + selected.getType()
                            + "\nStatut : " + getStatus(selected)
                            + "\nDescription : " + selected.getDescription());
        }
    }

    @FXML
    private void handleGoToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Dashboard.fxml"));
            Parent root = loader.load();
            DashboardController ctrl = loader.getController();
            ctrl.setLoggedInUser(loggedInUser);

            Stage stage = (Stage) trainingsTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Mon Tableau de Bord");
            stage.setMaximized(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        SessionManager.clearSession();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Login.fxml"));
            Stage stage = (Stage) trainingsTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("INTEGRA – Connexion");
            stage.setResizable(false);
            stage.setMaximized(false);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ===================== HELPERS =====================

    private String getStatus(TrainingProgram t) {
        long now = System.currentTimeMillis();
        long start = t.getStartDate().getTime();
        long end = t.getEndDate().getTime();
        return now >= start && now <= end ? "EN COURS"
                : now < start ? "PROGRAMMÉ" : "TERMINÉ";
    }

    private void applyStatusCellStyle() {
        colStatus.setCellFactory(col -> new TableCell<TrainingProgram, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                String c = item.equals("EN COURS")  ? "#66bb6a"
                        : item.equals("PROGRAMMÉ") ? "#1e88e5"
                        : "#9e9e9e";
                setStyle("-fx-background-color:" + c
                        + "; -fx-text-fill:white; -fx-font-weight:bold; -fx-alignment:CENTER;");
            }
        });
    }

    private void applyQuizCellStyle() {
        colQuiz.setCellFactory(col -> new TableCell<TrainingProgram, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                setStyle("-fx-text-fill: #1976d2; -fx-font-weight: bold; "
                        + "-fx-alignment: CENTER; -fx-cursor: hand;");
            }
        });
    }

    private void updateFooter() {
        if (footerLabel == null) return;
        try {
            int count = trainingService.getAll().size();
            String user = (loggedInUser != null) ? loggedInUser.getUsername() : "—";
            footerLabel.setText("Connecté : " + user
                    + " • INTEGRA_DB • " + count + " formations disponibles");
        } catch (Exception e) {
            footerLabel.setText("Connecté • INTEGRA HR");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}