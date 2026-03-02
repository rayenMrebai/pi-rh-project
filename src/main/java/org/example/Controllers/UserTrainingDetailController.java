package org.example.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.model.formation.Quiz;
import org.example.model.formation.TrainingProgram;
import org.example.services.formation.QuizService;
import org.example.services.formation.SkillService;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class UserTrainingDetailController implements Initializable {

    @FXML private Label titleLabel;
    @FXML private Label typeLabel;
    @FXML private Label statusLabel;
    @FXML private Label durationLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label skillLabel;
    @FXML private Label startDateLabel;
    @FXML private Label endDateLabel;
    @FXML private Label quizStatusLabel;
    @FXML private Button startQuizBtn;
    @FXML private Label resultScoreLabel;
    @FXML private Label resultPercentLabel;
    @FXML private Label resultPassedLabel;
    @FXML private Label resultDateLabel;
    @FXML private VBox resultPanel;
    @FXML private VBox quizPanel;

    private QuizService quizResultService;
    private SkillService skillService;
    private TrainingProgram training;

    // ✅ Stocker userId et nom sans dépendre de la classe User
    private int    currentUserId;
    private String currentUserName;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        quizResultService = new QuizService();
        skillService      = new SkillService();
    }

    // ✅ Méthode appelée avec userId/userName en attendant l'intégration
    public void setData(int userId, String userName, TrainingProgram training) {
        this.currentUserId   = userId;
        this.currentUserName = userName;
        this.training        = training;

        titleLabel.setText(training.getTitle());
        typeLabel.setText(training.getType().toUpperCase());
        durationLabel.setText(training.getDuration() + " semaines");
        descriptionLabel.setText(training.getDescription());

        String status = training.getStatus() != null ? training.getStatus() : "PROGRAMMÉ";
        statusLabel.setText(status);
        statusLabel.setStyle("-fx-text-fill: " + getStatusColor(status) +
                "; -fx-font-weight: bold; -fx-font-size: 14px;");

        if (training.getStartDate() != null)
            startDateLabel.setText(training.getStartDate().toString());
        if (training.getEndDate() != null)
            endDateLabel.setText(training.getEndDate().toString());

        var skills = skillService.getByTrainingProgramId(training.getId());
        skillLabel.setText(skills.isEmpty() ? "Aucune" :
                skills.stream().map(s -> s.getNom()).collect(Collectors.joining(", ")));

        checkQuizStatus();
    }

    private void checkQuizStatus() {
        boolean alreadyTaken = quizResultService.hasAlreadyTaken(
                currentUserId, training.getId());

        if (alreadyTaken) {
            Quiz result = quizResultService.getResult(currentUserId, training.getId());

            quizStatusLabel.setText("✅ Quiz complété — vous ne pouvez pas le repasser");
            quizStatusLabel.setStyle("-fx-text-fill: #43a047; -fx-font-weight: bold;");
            startQuizBtn.setDisable(true);
            startQuizBtn.setText("✅ Quiz déjà passé");
            startQuizBtn.setStyle(
                    "-fx-background-color: #9e9e9e; -fx-text-fill: white; " +
                            "-fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 6;");

            if (result != null) {
                resultPanel.setVisible(true);
                resultPanel.setManaged(true);
                resultScoreLabel.setText(result.getScore() + " / " + result.getTotalQuestions());
                resultPercentLabel.setText(String.format("%.1f%%", result.getPercentage()));
                resultPassedLabel.setText(result.isPassed() ? "✅ RÉUSSI" : "❌ ÉCHOUÉ");
                resultPassedLabel.setStyle(
                        "-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: " +
                                (result.isPassed() ? "#43a047" : "#e53935") + ";");
                if (result.getCompletedAt() != null)
                    resultDateLabel.setText("Passé le : " + result.getCompletedAt()
                            .format(java.time.format.DateTimeFormatter
                                    .ofPattern("dd/MM/yyyy HH:mm")));
            }
        } else {
            quizStatusLabel.setText("📝 Quiz disponible — testez vos connaissances !");
            quizStatusLabel.setStyle("-fx-text-fill: #1976d2; -fx-font-weight: bold;");
            startQuizBtn.setDisable(false);
            resultPanel.setVisible(false);
            resultPanel.setManaged(false);
        }
    }

    @FXML
    private void handleStartQuiz() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/QuizView.fxml"));
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(titleLabel.getScene().getWindow());
            stage.setTitle("📝 Quiz — " + training.getTitle());
            stage.setResizable(false);
            stage.setScene(new Scene(loader.load()));

            QuizController ctrl = loader.getController();
            // ✅ Passer userId directement
            ctrl.setData(currentUserId, currentUserName, training);

            stage.showAndWait();
            checkQuizStatus();
        } catch (Exception e) {
            showAlert("❌ Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClose() {
        ((Stage) titleLabel.getScene().getWindow()).close();
    }

    private String getStatusColor(String status) {
        switch (status) {
            case "EN COURS":  return "#43a047";
            case "PROGRAMMÉ": return "#1976d2";
            case "TERMINÉ":   return "#757575";
            case "ANNULÉ":    return "#e53935";
            case "SUSPENDU":  return "#f57c00";
            default:          return "#90a4ae";
        }
    }

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Info");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}