package org.example.Controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.model.formation.TrainingProgram;
import org.example.services.TrainingProgramService;

import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.ResourceBundle;

public class AddFormTrainingProgramController implements Initializable {

    @FXML private TextField titleField;
    @FXML private TextArea descriptionField;
    @FXML private Spinner<Integer> durationSpinner;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<String> typeCombo;
    @FXML private Button ajouterBtn;
    @FXML private Label statusLabel;

    private TrainingProgramService trainingService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        trainingService = new TrainingProgramService();

        // Configurer le ComboBox des types
        typeCombo.setItems(FXCollections.observableArrayList("en ligne", "présentiel"));

        // Configurer le Spinner pour la durée (1 à 500 heures)
        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 500, 40);
        durationSpinner.setValueFactory(valueFactory);

        // Configurer les DatePickers
        startDatePicker.setValue(LocalDate.now());
        endDatePicker.setValue(LocalDate.now().plusDays(30));
    }

    @FXML
    private void handleAjouter() {
        try {
            // Validation
            if (!validerChamps()) {
                return;
            }

            // Convertir LocalDate en Date
            Date startDate = Date.from(startDatePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date endDate = Date.from(endDatePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());

            // Créer le TrainingProgram
            TrainingProgram newTraining = new TrainingProgram(
                    titleField.getText().trim(),
                    descriptionField.getText().trim(),
                    durationSpinner.getValue(),
                    startDate,
                    endDate,
                    typeCombo.getValue()
            );

            // Ajouter en base
            trainingService.create(newTraining);

            // Message de succès
            afficherMessage("✅ Formation ajoutée avec succès ! Redirection...", "success");

            // Rediriger vers la liste après 1.5 seconde
            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                    javafx.application.Platform.runLater(() -> {
                        try {
                            naviguerVersListe();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (Exception e) {
            afficherMessage("❌ Erreur lors de l'ajout : " + e.getMessage(), "error");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRetourListe() {
        try {
            naviguerVersListe();
        } catch (Exception e) {
            afficherMessage("❌ Erreur de navigation", "error");
            e.printStackTrace();
        }
    }

    private boolean validerChamps() {
        // Validation du titre
        if (titleField.getText().trim().isEmpty()) {
            afficherMessage("⚠️ Le titre de la formation est obligatoire", "warning");
            titleField.requestFocus();
            return false;
        }

        // Validation de la description
        if (descriptionField.getText().trim().isEmpty()) {
            afficherMessage("⚠️ La description est obligatoire", "warning");
            descriptionField.requestFocus();
            return false;
        }

        // Validation de la date de début
        if (startDatePicker.getValue() == null) {
            afficherMessage("⚠️ Veuillez sélectionner une date de début", "warning");
            startDatePicker.requestFocus();
            return false;
        }

        // Validation de la date de fin
        if (endDatePicker.getValue() == null) {
            afficherMessage("⚠️ Veuillez sélectionner une date de fin", "warning");
            endDatePicker.requestFocus();
            return false;
        }

        // Vérifier que la date de fin est après la date de début
        if (endDatePicker.getValue().isBefore(startDatePicker.getValue())) {
            afficherMessage("⚠️ La date de fin doit être après la date de début", "warning");
            endDatePicker.requestFocus();
            return false;
        }

        // Validation du type
        if (typeCombo.getValue() == null) {
            afficherMessage("⚠️ Veuillez sélectionner un type de formation", "warning");
            typeCombo.requestFocus();
            return false;
        }

        return true;
    }

    private void naviguerVersListe() throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/ListTrainingPrograms.fxml"));
        Stage stage = (Stage) ajouterBtn.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Liste des Formations");
        stage.setMaximized(true);
    }

    private void afficherMessage(String message, String type) {
        statusLabel.setText(message);

        switch (type) {
            case "success":
                statusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-size: 14px;");
                break;
            case "error":
                statusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-font-size: 14px;");
                break;
            case "warning":
                statusLabel.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold; -fx-font-size: 14px;");
                break;
        }
    }
}