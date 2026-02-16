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

public class UpdateFormTrainingProgramController implements Initializable {

    @FXML private TextField idField;
    @FXML private TextField titleField;
    @FXML private TextArea descriptionField;
    @FXML private Spinner<Integer> durationSpinner;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<String> typeCombo;
    @FXML private Button modifierBtn;
    @FXML private Button annulerBtn;
    @FXML private Label statusLabel;

    private TrainingProgramService trainingService;
    private TrainingProgram trainingToUpdate;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        trainingService = new TrainingProgramService();

        // Configurer le ComboBox
        typeCombo.setItems(FXCollections.observableArrayList("en ligne", "présentiel"));

        // Configurer le Spinner
        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 500, 40);
        durationSpinner.setValueFactory(valueFactory);

        System.out.println("✅ Formulaire de modification de formation initialisé");
    }

    /**
     * MÉTHODE CORRIGÉE : Pré-remplir le formulaire avec les données du training program
     */
    public void setTrainingProgram(TrainingProgram training) {
        if (training == null) {
            System.err.println("❌ ERREUR : Le training program passé est null !");
            return;
        }

        this.trainingToUpdate = training;

        System.out.println("📝 Pré-remplissage du formulaire avec : " + training);

        // Remplir chaque champ
        idField.setText(String.valueOf(training.getId()));
        titleField.setText(training.getTitle());
        descriptionField.setText(training.getDescription());
        durationSpinner.getValueFactory().setValue(training.getDuration());

        // ✅ CORRECTION : Convertir java.util.Date en LocalDate correctement
        LocalDate startDate = convertToLocalDate(training.getStartDate());
        LocalDate endDate = convertToLocalDate(training.getEndDate());

        startDatePicker.setValue(startDate);
        endDatePicker.setValue(endDate);
        typeCombo.setValue(training.getType());

        System.out.println("✅ Formulaire pré-rempli avec succès :");
        System.out.println("   - ID: " + training.getId());
        System.out.println("   - Titre: " + training.getTitle());
        System.out.println("   - Description: " + training.getDescription());
        System.out.println("   - Durée: " + training.getDuration() + "h");
        System.out.println("   - Date début: " + startDate);
        System.out.println("   - Date fin: " + endDate);
        System.out.println("   - Type: " + training.getType());
    }

    /**
     * Méthode utilitaire pour convertir java.util.Date en LocalDate
     * Compatible avec java.sql.Date qui ne supporte pas toInstant()
     */
    private LocalDate convertToLocalDate(Date date) {
        if (date == null) {
            return LocalDate.now();
        }

        // Convertir via Calendar (compatible avec java.sql.Date)
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTime(date);

        return LocalDate.of(
                calendar.get(java.util.Calendar.YEAR),
                calendar.get(java.util.Calendar.MONTH) + 1,  // Les mois commencent à 0
                calendar.get(java.util.Calendar.DAY_OF_MONTH)
        );
    }

    @FXML
    private void handleModifier() {
        try {
            System.out.println("🔄 Début de la modification...");

            // Validation
            if (!validerChamps()) {
                return;
            }

            // Afficher les modifications
            System.out.println("📊 Modifications :");
            System.out.println("   Ancien titre: " + trainingToUpdate.getTitle() + " → Nouveau: " + titleField.getText().trim());

            // Convertir LocalDate en Date
            Date startDate = Date.from(startDatePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date endDate = Date.from(endDatePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());

            // Mettre à jour les données
            trainingToUpdate.setTitle(titleField.getText().trim());
            trainingToUpdate.setDescription(descriptionField.getText().trim());
            trainingToUpdate.setDuration(durationSpinner.getValue());
            trainingToUpdate.setStartDate(startDate);
            trainingToUpdate.setEndDate(endDate);
            trainingToUpdate.setType(typeCombo.getValue());

            // Enregistrer en base
            trainingService.update(trainingToUpdate);

            // Message de succès
            afficherMessage("✅ Formation modifiée avec succès ! Redirection...", "success");

            // Redirection après 1.5 seconde
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
            afficherMessage("❌ Erreur lors de la modification : " + e.getMessage(), "error");
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

    /**
     * Ajouter des contrôles de saisie en temps réel
     */
    private void ajouterControlesEnTempsReel() {
        // Limiter le titre à 200 caractères
        titleField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 200) {
                titleField.setText(oldValue);
            }
        });

        // Limiter la description à 1000 caractères
        descriptionField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 1000) {
                descriptionField.setText(oldValue);
            }
        });
    }

    /**
     * VALIDATION COMPLÈTE DES CHAMPS
     */
    private boolean validerChamps() {
        // 1. Validation du titre
        if (titleField.getText() == null || titleField.getText().trim().isEmpty()) {
            afficherMessage("⚠️ Le titre de la formation est obligatoire", "warning");
            titleField.requestFocus();
            titleField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
            return false;
        }
        titleField.setStyle("");

        if (titleField.getText().trim().length() < 5) {
            afficherMessage("⚠️ Le titre doit contenir au moins 5 caractères", "warning");
            titleField.requestFocus();
            titleField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
            return false;
        }
        titleField.setStyle("");

        if (titleField.getText().trim().length() > 200) {
            afficherMessage("⚠️ Le titre ne doit pas dépasser 200 caractères", "warning");
            titleField.requestFocus();
            titleField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
            return false;
        }
        titleField.setStyle("");

        // 2. Validation de la description
        if (descriptionField.getText() == null || descriptionField.getText().trim().isEmpty()) {
            afficherMessage("⚠️ La description est obligatoire", "warning");
            descriptionField.requestFocus();
            descriptionField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
            return false;
        }
        descriptionField.setStyle("");

        if (descriptionField.getText().trim().length() < 20) {
            afficherMessage("⚠️ La description doit contenir au moins 20 caractères", "warning");
            descriptionField.requestFocus();
            descriptionField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
            return false;
        }
        descriptionField.setStyle("");

        if (descriptionField.getText().trim().length() > 1000) {
            afficherMessage("⚠️ La description ne doit pas dépasser 1000 caractères", "warning");
            descriptionField.requestFocus();
            descriptionField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
            return false;
        }
        descriptionField.setStyle("");

        // 3. Validation de la durée
        if (durationSpinner.getValue() == null) {
            afficherMessage("⚠️ Veuillez définir une durée", "warning");
            durationSpinner.requestFocus();
            durationSpinner.setStyle("-fx-border-color: red; -fx-border-width: 2;");
            return false;
        }
        durationSpinner.setStyle("");

        if (durationSpinner.getValue() < 1 || durationSpinner.getValue() > 500) {
            afficherMessage("⚠️ La durée doit être entre 1 et 500 heures", "warning");
            durationSpinner.requestFocus();
            durationSpinner.setStyle("-fx-border-color: red; -fx-border-width: 2;");
            return false;
        }
        durationSpinner.setStyle("");

        // 4. Validation de la date de début
        if (startDatePicker.getValue() == null) {
            afficherMessage("⚠️ Veuillez sélectionner une date de début", "warning");
            startDatePicker.requestFocus();
            startDatePicker.setStyle("-fx-border-color: red; -fx-border-width: 2;");
            return false;
        }
        startDatePicker.setStyle("");

        // Vérifier que la date de début n'est pas dans le passé (optionnel)
        // if (startDatePicker.getValue().isBefore(LocalDate.now())) {
        //     afficherMessage("⚠️ La date de début ne peut pas être dans le passé", "warning");
        //     startDatePicker.requestFocus();
        //     startDatePicker.setStyle("-fx-border-color: red; -fx-border-width: 2;");
        //     return false;
        // }
        // startDatePicker.setStyle("");

        // 5. Validation de la date de fin
        if (endDatePicker.getValue() == null) {
            afficherMessage("⚠️ Veuillez sélectionner une date de fin", "warning");
            endDatePicker.requestFocus();
            endDatePicker.setStyle("-fx-border-color: red; -fx-border-width: 2;");
            return false;
        }
        endDatePicker.setStyle("");

        // Vérifier que la date de fin est après la date de début
        if (endDatePicker.getValue().isBefore(startDatePicker.getValue())) {
            afficherMessage("⚠️ La date de fin doit être après la date de début", "warning");
            endDatePicker.requestFocus();
            endDatePicker.setStyle("-fx-border-color: red; -fx-border-width: 2;");
            return false;
        }
        endDatePicker.setStyle("");

        // Vérifier que la date de fin n'est pas la même que la date de début
        if (endDatePicker.getValue().isEqual(startDatePicker.getValue())) {
            afficherMessage("⚠️ La date de fin doit être différente de la date de début", "warning");
            endDatePicker.requestFocus();
            endDatePicker.setStyle("-fx-border-color: red; -fx-border-width: 2;");
            return false;
        }
        endDatePicker.setStyle("");

        // Vérifier que la durée n'est pas trop longue (ex: max 1 an)
        if (java.time.temporal.ChronoUnit.DAYS.between(startDatePicker.getValue(), endDatePicker.getValue()) > 365) {
            afficherMessage("⚠️ La formation ne peut pas durer plus d'un an", "warning");
            endDatePicker.requestFocus();
            endDatePicker.setStyle("-fx-border-color: red; -fx-border-width: 2;");
            return false;
        }
        endDatePicker.setStyle("");

        // 6. Validation du type
        if (typeCombo.getValue() == null) {
            afficherMessage("⚠️ Veuillez sélectionner un type de formation", "warning");
            typeCombo.requestFocus();
            typeCombo.setStyle("-fx-border-color: red; -fx-border-width: 2;");
            return false;
        }
        typeCombo.setStyle("");

        if (!typeCombo.getValue().equals("en ligne") && !typeCombo.getValue().equals("présentiel")) {
            afficherMessage("⚠️ Type invalide. Choisissez 'en ligne' ou 'présentiel'", "warning");
            typeCombo.requestFocus();
            typeCombo.setStyle("-fx-border-color: red; -fx-border-width: 2;");
            return false;
        }
        typeCombo.setStyle("");

        // Toutes les validations passées
        return true;
    }

    private void naviguerVersListe() throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/ListTrainingPrograms.fxml"));
        Stage stage = (Stage) modifierBtn.getScene().getWindow();
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