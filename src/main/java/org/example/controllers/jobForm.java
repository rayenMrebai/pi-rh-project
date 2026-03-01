package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.model.recrutement.JobPosition;
import org.example.services.recrutement.JobPositionService;

import java.time.LocalDate;

public class jobForm {

    @FXML private TextField tfTitle;
    @FXML private TextField tfDepartement;
    @FXML private TextField tfEmployeeType;
    @FXML private TextArea taDescription;
    @FXML private ComboBox<String> cbStatus;
    @FXML private DatePicker dpPostedAt;
    @FXML private Button btnSave;
    @FXML private Button btnCancel;

    private final JobPositionService jobService = new JobPositionService();
    private JobPosition jobToEdit;
    private Runnable onSaveCallback;

    @FXML
    public void initialize() {
        cbStatus.getItems().addAll("Open", "Closed", "Paused");
        cbStatus.getSelectionModel().select("Open");
        dpPostedAt.setValue(LocalDate.now());

        // Contrôles de saisie
        onlyLettersAndSpaces(tfTitle);
        onlyLettersAndSpaces(tfDepartement);
        onlyLettersSpacesAndDash(tfEmployeeType);
    }

    private void onlyLettersAndSpaces(TextField tf) {
        if (tf == null) return;
        tf.setTextFormatter(new TextFormatter<String>(change -> {
            String t = change.getControlNewText();
            return t.matches("[a-zA-ZÀ-ÿ ]*") ? change : null;
        }));
    }

    private void onlyLettersSpacesAndDash(TextField tf) {
        if (tf == null) return;
        tf.setTextFormatter(new TextFormatter<String>(change -> {
            String t = change.getControlNewText();
            return t.matches("[a-zA-ZÀ-ÿ \\-]*") ? change : null;
        }));
    }

    public void setJobToEdit(JobPosition job) {
        this.jobToEdit = job;

        tfTitle.setText(job.getTitle());
        tfDepartement.setText(job.getDepartement());
        tfEmployeeType.setText(job.getEmployeeType());
        taDescription.setText(job.getDescription());
        cbStatus.getSelectionModel().select(job.getStatus() != null ? job.getStatus() : "Open");
        dpPostedAt.setValue(job.getPostedAt() != null ? job.getPostedAt() : LocalDate.now());
    }

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    @FXML
    private void onSave() {
        if (tfTitle.getText().trim().isEmpty()
                || tfDepartement.getText().trim().isEmpty()
                || tfEmployeeType.getText().trim().isEmpty()) {

            Alert a = new Alert(Alert.AlertType.WARNING);
            a.setTitle("Required Fields");
            a.setHeaderText(null);
            a.setContentText("Title, Department, and Employee Type are required.");
            a.showAndWait();
            return;
        }

        try {
            if (jobToEdit == null) {
                // ADD
                JobPosition j = new JobPosition(
                        tfTitle.getText().trim(),
                        tfDepartement.getText().trim(),
                        tfEmployeeType.getText().trim(),
                        taDescription.getText() != null ? taDescription.getText().trim() : "",
                        cbStatus.getValue(),
                        dpPostedAt.getValue()
                );
                jobService.create(j);
            } else {
                // EDIT
                jobToEdit.setTitle(tfTitle.getText().trim());
                jobToEdit.setDepartement(tfDepartement.getText().trim());
                jobToEdit.setEmployeeType(tfEmployeeType.getText().trim());
                jobToEdit.setDescription(taDescription.getText() != null ? taDescription.getText().trim() : "");
                jobToEdit.setStatus(cbStatus.getValue());
                jobToEdit.setPostedAt(dpPostedAt.getValue());
                jobService.update(jobToEdit);
            }

            close();
            if (onSaveCallback != null) onSaveCallback.run();

        } catch (RuntimeException ex) {
            ex.printStackTrace();
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Database Error");
            a.setHeaderText("Operation failed");
            a.setContentText(ex.getMessage());
            a.showAndWait();
        }
    }

    @FXML
    private void onCancel() {
        close();
    }

    private void close() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }
}