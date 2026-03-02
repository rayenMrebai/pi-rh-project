package org.example.Controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.model.formation.Skill;
import org.example.model.formation.TrainingProgram;
import org.example.services.formation.SkillService;
import org.example.services.formation.TrainingProgramService;

import java.net.URL;
import java.sql.Date;
import java.util.List;
import java.util.ResourceBundle;

public class UpdateFormTrainingProgramController implements Initializable {

    @FXML private TextField titleField;
    @FXML private TextArea  descriptionField;
    @FXML private Spinner<Integer> durationSpinner;
    @FXML private ComboBox<String> typeCombo;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<String> statusCombo; // ✅ NOUVEAU
    @FXML private ComboBox<Skill>  skillCombo;
    @FXML private Label errorLabel;

    private TrainingProgramService trainingService;
    private SkillService skillService;
    private TrainingProgram currentTraining;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        trainingService = new TrainingProgramService();
        skillService    = new SkillService();

        typeCombo.setItems(FXCollections.observableArrayList(
                "en ligne", "présentiel", "hybride", "workshop"));

        durationSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 52, 4));

        // ✅ Statuts disponibles
        statusCombo.setItems(FXCollections.observableArrayList(
                "PROGRAMMÉ", "EN COURS", "TERMINÉ", "ANNULÉ", "SUSPENDU"));

        // ✅ Couleur dynamique
        statusCombo.setOnAction(e -> updateStatusColor());

        loadSkills();
    }

    // ✅ Pré-remplir le formulaire avec les données existantes
    public void setTrainingProgram(TrainingProgram training) {
        this.currentTraining = training;

        titleField.setText(training.getTitle());
        descriptionField.setText(training.getDescription());
        durationSpinner.getValueFactory().setValue(training.getDuration());
        typeCombo.setValue(training.getType());

        // ✅ Pré-sélectionner le statut actuel
        String status = training.getStatus() != null ? training.getStatus() : "PROGRAMMÉ";
        statusCombo.setValue(status);
        updateStatusColor();

        // Dates
        // Dates — toLocalDate() fonctionne directement sur java.sql.Date
        if (training.getStartDate() != null) {
            startDatePicker.setValue(
                    new java.sql.Date(training.getStartDate().getTime()).toLocalDate());
        }
        if (training.getEndDate() != null) {
            endDatePicker.setValue(
                    new java.sql.Date(training.getEndDate().getTime()).toLocalDate());
        }

        // Skill associé
        if (training.getId() > 0) {
            List<Skill> associated = skillService.getByTrainingProgramId(training.getId());
            if (!associated.isEmpty()) {
                skillCombo.setValue(associated.get(0));
            }
        }
    }

    // ✅ Changer couleur du ComboBox selon statut
    private void updateStatusColor() {
        String status = statusCombo.getValue();
        if (status == null) return;
        String color;
        switch (status) {
            case "EN COURS":  color = "#43a047"; break;
            case "PROGRAMMÉ": color = "#1976d2"; break;
            case "TERMINÉ":   color = "#757575"; break;
            case "ANNULÉ":    color = "#e53935"; break;
            case "SUSPENDU":  color = "#f57c00"; break;
            default:          color = "#90a4ae"; break;
        }
        statusCombo.setStyle(
                "-fx-background-radius: 6; -fx-font-size: 13px; " +
                        "-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold;");
    }

    private void loadSkills() {
        List<Skill> skills = skillService.getAll();
        skillCombo.setCellFactory(lv -> new ListCell<Skill>() {
            @Override
            protected void updateItem(Skill item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null :
                        item.getNom() + " [" + item.getCategorie() + "]");
            }
        });
        skillCombo.setButtonCell(new ListCell<Skill>() {
            @Override
            protected void updateItem(Skill item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Sélectionner une compétence..." :
                        item.getNom() + " [" + item.getCategorie() + "]");
            }
        });
        skillCombo.setItems(FXCollections.observableArrayList(skills));
    }

    @FXML
    private void handleSave() {
        errorLabel.setText("");

        String title  = titleField.getText().trim();
        String desc   = descriptionField.getText().trim();
        String type   = typeCombo.getValue();
        String status = statusCombo.getValue();
        var start = startDatePicker.getValue();
        var end   = endDatePicker.getValue();

        if (title.isEmpty() || desc.isEmpty() || type == null || status == null
                || start == null || end == null) {
            errorLabel.setText("⚠️ Veuillez remplir tous les champs obligatoires.");
            return;
        }
        if (end.isBefore(start)) {
            errorLabel.setText("⚠️ La date de fin doit être après la date de début.");
            return;
        }

        try {
            currentTraining.setTitle(title);
            currentTraining.setDescription(desc);
            currentTraining.setDuration(durationSpinner.getValue());
            currentTraining.setType(type);
            currentTraining.setStatus(status); // ✅ statut mis à jour
            currentTraining.setStartDate(Date.valueOf(start));
            currentTraining.setEndDate(Date.valueOf(end));

            trainingService.update(currentTraining);

            Skill selectedSkill = skillCombo.getValue();
            if (selectedSkill != null) {
                skillService.assignToTraining(selectedSkill.getId(), currentTraining.getId());
            }

            new Alert(Alert.AlertType.INFORMATION,
                    "✅ Programme mis à jour !\n📌 Statut : " + status).showAndWait();

            ((Stage) titleField.getScene().getWindow()).close();

        } catch (Exception e) {
            errorLabel.setText("❌ Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        ((Stage) titleField.getScene().getWindow()).close();
    }
}