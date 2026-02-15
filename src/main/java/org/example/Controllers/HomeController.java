package org.example.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.example.services.SkillService;
import org.example.services.TrainingProgramService;

import java.net.URL;
import java.util.ResourceBundle;

public class HomeController implements Initializable {

    @FXML private Label skillCountLabel;
    @FXML private Label trainingCountLabel;

    private SkillService skillService;
    private TrainingProgramService trainingService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        skillService = new SkillService();
        trainingService = new TrainingProgramService();

        // Charger les statistiques
        chargerStatistiques();
    }

    /**
     * Charger les statistiques (nombre de compétences et formations)
     */
    private void chargerStatistiques() {
        try {
            // Compter les compétences
            int skillCount = skillService.getAll().size();
            skillCountLabel.setText(String.valueOf(skillCount));

            // Compter les formations
            int trainingCount = trainingService.getAll().size();
            trainingCountLabel.setText(String.valueOf(trainingCount));

        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des statistiques : " + e.getMessage());
            skillCountLabel.setText("0");
            trainingCountLabel.setText("0");
        }
    }

    /**
     * Naviguer vers la liste des compétences
     */
    @FXML
    private void handleGoToSkills() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/ListSkills.fxml"));
            Stage stage = (Stage) skillCountLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Gestion des Compétences");
            stage.setMaximized(true);
        } catch (Exception e) {
            System.err.println("Erreur lors de la navigation vers les compétences : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Naviguer vers la liste des formations
     */
    @FXML
    private void handleGoToTrainings() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/ListTrainingPrograms.fxml"));
            Stage stage = (Stage) trainingCountLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Gestion des Formations");
            stage.setMaximized(true);
        } catch (Exception e) {
            System.err.println("Erreur lors de la navigation vers les formations : " + e.getMessage());
            e.printStackTrace();
        }
    }
}