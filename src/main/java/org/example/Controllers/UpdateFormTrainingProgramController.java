package org.example.Controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.model.formation.TrainingProgram;
import org.example.services.TrainingProgramService;

import java.net.URL;
import java.time.ZoneId;
import java.util.ResourceBundle;

public class UpdateFormTrainingProgramController implements Initializable {

    @FXML private Label idLabel;
    @FXML private TextField titleField;
    @FXML private TextArea descriptionField;
    @FXML private Spinner<Integer> durationSpinner;
    @FXML private ComboBox<String> typeCombo;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Label errorLabel;

    private TrainingProgramService trainingService;
    private TrainingProgram currentTraining;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        trainingService = new TrainingProgramService();
        typeCombo.setItems(FXCollections.observableArrayList("en ligne", "présentiel", "hybride", "workshop"));
        durationSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 52, 4));
    }

    public void setTrainingProgram(TrainingProgram t) {
        this.currentTraining = t;
        idLabel.setText(String.format("%04d", t.getId()));
        titleField.setText(t.getTitle());
        descriptionField.setText(t.getDescription());
        typeCombo.setValue(t.getType());
        durationSpinner.getValueFactory().setValue(t.getDuration());

        // Conversion java.util.Date -> LocalDate
        if (t.getStartDate() != null) {
            startDatePicker.setValue(
                    t.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            );
        }
        if (t.getEndDate() != null) {
            endDatePicker.setValue(
                    t.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            );
        }
    }

    @FXML
    private void handleUpdate() {
        errorLabel.setText("");
        String title = titleField.getText().trim();
        String desc  = descriptionField.getText().trim();
        String type  = typeCombo.getValue();
        var start = startDatePicker.getValue();
        var end   = endDatePicker.getValue();

        if (title.isEmpty() || desc.isEmpty() || type == null || start == null || end == null) {
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
            currentTraining.setStartDate(java.sql.Date.valueOf(start));
            currentTraining.setEndDate(java.sql.Date.valueOf(end));

            trainingService.update(currentTraining);
            new Alert(Alert.AlertType.INFORMATION, "✅ Programme mis à jour !").showAndWait();
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