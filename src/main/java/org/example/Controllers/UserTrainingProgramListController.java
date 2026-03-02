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
import org.example.model.user.UserAccount;
import org.example.services.formation.QuizService;
import org.example.services.formation.TrainingProgramService;
import org.example.util.SessionManager;

import java.io.IOException;
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

    // ✅ Utilisateur connecté
    private UserAccount loggedInUser;

    // ===================== SET USER =====================

    public void setLoggedInUser(UserAccount user) {
        this.loggedInUser = user;
        if (user != null) {
            welcomeLabel.setText("👤 Bienvenue, " + user.getUsername()
                    + " (" + user.getRole() + ")");
            // Recharger avec le vrai userId pour la colonne quiz
            loadTrainings();
        }
    }

    // ===================== INITIALIZE =====================

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        trainingService   = new TrainingProgramService();
        quizResultService = new QuizService();

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

        // ✅ Colonne quiz avec le vrai userId
        colQuiz.setCellValueFactory(d -> {
            int userId = (loggedInUser != null) ? loggedInUser.getUserId() : 0;
            boolean taken = quizResultService.hasAlreadyTaken(userId, d.getValue().getId());
            return new SimpleStringProperty(taken ? "✅ Complété" : "📝 Disponible");
        });

        applyStyles();

        // Double-clic → ouvrir détails
        trainingsTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                TrainingProgram sel = trainingsTable.getSelectionModel().getSelectedItem();
                if (sel != null) openTrainingDetail(sel);
            }
        });

        searchField.textProperty().addListener((obs, old, nv) -> applySearch(nv));

        loadTrainings();
    }

    // ===================== LOAD DATA =====================

    private void loadTrainings() {
        allTrainings = trainingService.getAll();
        trainingsTable.setItems(FXCollections.observableArrayList(allTrainings));
        updateFooter();
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

    // ===================== ACTIONS =====================

    private void openTrainingDetail(TrainingProgram training) {
        // ✅ Vérifier si le FXML existe avant de le charger
        URL fxmlUrl = getClass().getResource("/UserTrainingDetail.fxml");
        if (fxmlUrl != null) {
            try {
                FXMLLoader loader = new FXMLLoader(fxmlUrl);
                Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.initOwner(trainingsTable.getScene().getWindow());
                stage.setTitle("📚 " + training.getTitle());
                stage.setScene(new Scene(loader.load()));

                UserTrainingDetailController ctrl = loader.getController();
                int userId = (loggedInUser != null) ? loggedInUser.getUserId() : 0;
                String userName = (loggedInUser != null) ? loggedInUser.getUsername() : "Utilisateur";
                ctrl.setData(userId, userName, training);

                stage.showAndWait();
                trainingsTable.refresh();
            } catch (IOException e) {
                showAlert("❌ Erreur chargement : " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            // ✅ FXML absent → afficher les détails dans une Alert
            showAlert(Alert.AlertType.INFORMATION, training.getTitle(),
                    "📚 Formation  : " + training.getTitle()
                            + "\n⏱ Durée      : " + training.getDuration() + " semaines"
                            + "\n🎓 Type       : " + training.getType()
                            + "\n📊 Statut     : " + (training.getStatus() != null ? training.getStatus() : "PROGRAMMÉ")
                            + "\n📅 Début      : " + training.getStartDate()
                            + "\n📅 Fin        : " + training.getEndDate()
                            + "\n📝 Description: " + training.getDescription());
        }
    }

    @FXML
    private void handleViewDetail() {
        TrainingProgram sel = trainingsTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner une formation.");
            return;
        }
        openTrainingDetail(sel);
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
        SessionManager.logout();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Login.fxml"));
            Stage stage = (Stage) trainingsTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("INTEGRA – Connexion");
            stage.setMaximized(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ===================== HELPERS =====================

    private void updateFooter() {
        if (footerLabel == null) return;
        String user = (loggedInUser != null) ? loggedInUser.getUsername() : "—";
        int count = (allTrainings != null) ? allTrainings.size() : 0;
        footerLabel.setText("Connecté : " + user + " • " + count + " formations disponibles");
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
        showAlert(Alert.AlertType.INFORMATION, "Information", msg);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }
}
