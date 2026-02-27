package org.example.Controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.model.formation.Quiz;
import org.example.model.formation.TrainingProgram;
import org.example.services.QuizApiService;
import org.example.services.QuizApiService.QuizQuestion;
import org.example.services.QuizService;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class QuizController implements Initializable {

    @FXML private Label       trainingTitleLabel;
    @FXML private Label       questionCounterLabel;
    @FXML private ProgressBar progressBar;
    @FXML private Label       difficultyLabel;
    @FXML private Label       questionLabel;
    @FXML private Label       categoryLabel;
    @FXML private VBox        answersBox;
    @FXML private Button      nextBtn;
    @FXML private Button      prevBtn;
    @FXML private Label       feedbackLabel;
    @FXML private VBox        quizPanel;
    @FXML private VBox        resultPanel;
    @FXML private Label       finalScoreLabel;
    @FXML private Label       finalPercentLabel;
    @FXML private Label       finalPassedLabel;
    @FXML private Label       finalMessageLabel;
    @FXML private ProgressBar finalProgressBar;

    private QuizApiService    quizApiService;
    private QuizService quizResultService;
    private TrainingProgram   training;

    // ✅ userId/userName sans dépendre de User.java
    private int    currentUserId;
    private String currentUserName;

    private List<QuizQuestion> questions;
    private int      currentIndex = 0;
    private String[] userAnswers;
    private ToggleGroup toggleGroup;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        quizApiService    = new QuizApiService();
        quizResultService = new QuizService();
    }

    // ✅ Méthode appelée avec userId/userName directement
    public void setData(int userId, String userName, TrainingProgram training) {
        this.currentUserId   = userId;
        this.currentUserName = userName;
        this.training        = training;

        trainingTitleLabel.setText("📝 Quiz : " + training.getTitle());

        String difficulty = training.getDuration() > 8 ? "hard" :
                training.getDuration() > 4 ? "medium" : "easy";
        difficultyLabel.setText("Niveau : " + difficulty.toUpperCase());

        questionLabel.setText("⏳ Chargement des questions...");

        new Thread(() -> {
            questions   = quizApiService.fetchQuestions(10, difficulty);
            userAnswers = new String[questions.size()];
            Platform.runLater(() -> {
                if (!questions.isEmpty()) showQuestion(0);
                else questionLabel.setText("❌ Impossible de charger les questions.");
            });
        }).start();
    }

    private void showQuestion(int index) {
        if (questions == null || questions.isEmpty()) return;

        QuizQuestion q = questions.get(index);
        currentIndex = index;

        questionCounterLabel.setText("Question " + (index + 1) + " / " + questions.size());
        progressBar.setProgress((double)(index + 1) / questions.size());
        categoryLabel.setText("📂 " + q.getCategory());
        questionLabel.setText(q.getQuestion());

        answersBox.getChildren().clear();
        toggleGroup  = new ToggleGroup();
        feedbackLabel.setText("");

        for (String answer : q.getAllAnswers()) {
            RadioButton rb = new RadioButton(answer);
            rb.setToggleGroup(toggleGroup);
            rb.setWrapText(true);
            rb.setStyle("-fx-font-size: 13px; -fx-padding: 8; -fx-cursor: hand;");
            if (answer.equals(userAnswers[index])) rb.setSelected(true);
            answersBox.getChildren().add(rb);
        }

        prevBtn.setDisable(index == 0);
        nextBtn.setText(index == questions.size() - 1 ? "Terminer ✅" : "Suivant →");
    }

    @FXML
    private void handleNext() {
        if (toggleGroup.getSelectedToggle() == null) {
            feedbackLabel.setText("⚠️ Veuillez sélectionner une réponse.");
            feedbackLabel.setStyle("-fx-text-fill: #f57c00; -fx-font-weight: bold;");
            return;
        }

        RadioButton selected = (RadioButton) toggleGroup.getSelectedToggle();
        userAnswers[currentIndex] = selected.getText();
        feedbackLabel.setText("");

        if (currentIndex == questions.size() - 1) {
            calculateAndShowResult();
        } else {
            showQuestion(currentIndex + 1);
        }
    }

    @FXML
    private void handlePrev() {
        if (toggleGroup.getSelectedToggle() != null) {
            userAnswers[currentIndex] =
                    ((RadioButton) toggleGroup.getSelectedToggle()).getText();
        }
        if (currentIndex > 0) showQuestion(currentIndex - 1);
    }

    private void calculateAndShowResult() {
        int score = 0;
        for (int i = 0; i < questions.size(); i++) {
            if (questions.get(i).getCorrectAnswer().equals(userAnswers[i])) score++;
        }

        double  percentage = (double) score / questions.size() * 100;
        boolean passed     = percentage >= 60.0;

        // ✅ Sauvegarder avec currentUserId
        Quiz result = new Quiz();
        result.setUserId(currentUserId);
        result.setTrainingId(training.getId());
        result.setScore(score);
        result.setTotalQuestions(questions.size());
        result.setPercentage(percentage);
        result.setPassed(passed);
        quizResultService.save(result);

        // ✅ Afficher résultat
        quizPanel.setVisible(false);
        quizPanel.setManaged(false);
        resultPanel.setVisible(true);
        resultPanel.setManaged(true);

        finalScoreLabel.setText(score + " / " + questions.size());
        finalPercentLabel.setText(String.format("%.1f%%", percentage));
        finalProgressBar.setProgress(percentage / 100);
        finalProgressBar.setStyle(passed ? "-fx-accent: #43a047;" : "-fx-accent: #e53935;");
        finalPassedLabel.setText(passed ? "✅ RÉUSSI !" : "❌ ÉCHOUÉ");
        finalPassedLabel.setStyle(
                "-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " +
                        (passed ? "#43a047" : "#e53935") + ";");
        finalMessageLabel.setText(passed
                ? "🎉 Félicitations " + currentUserName + " ! Vous avez maîtrisé cette formation."
                : "📚 Dommage " + currentUserName + ". Continuez à étudier !");
    }

    @FXML
    private void handleClose() {
        ((Stage) trainingTitleLabel.getScene().getWindow()).close();
    }
}