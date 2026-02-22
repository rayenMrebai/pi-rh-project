package org.example.Controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.model.formation.TrainingProgram;
import org.example.services.TrainingProgramService;

import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

public class AddFormTrainingProgramController implements Initializable {

    @FXML private TextField titleField;
    @FXML private TextArea descriptionField;
    @FXML private Spinner<Integer> durationSpinner;
    @FXML private ComboBox<String> typeCombo;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Label errorLabel;

    private TrainingProgramService trainingService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        trainingService = new TrainingProgramService();
        typeCombo.setItems(FXCollections.observableArrayList("en ligne", "présentiel", "hybride", "workshop"));
        durationSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 52, 4));
    }

    @FXML
    private void handleSave() {
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
            TrainingProgram p = new TrainingProgram();
            p.setTitle(title);
            p.setDescription(desc);
            p.setDuration(durationSpinner.getValue());
            p.setType(type);
            // Conversion LocalDate -> java.util.Date
            p.setStartDate(java.sql.Date.valueOf(start));
            p.setEndDate(java.sql.Date.valueOf(end));

            trainingService.create(p);
            new Alert(Alert.AlertType.INFORMATION, "✅ Programme '" + title + "' ajouté !").showAndWait();
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