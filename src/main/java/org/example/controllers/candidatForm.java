package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.model.recrutement.Candidat;
import org.example.model.recrutement.JobPosition;
import org.example.services.recrutement.CandidatService;

import java.io.IOException;

public class candidatForm {

    @FXML private TextField tfFirstName;
    @FXML private TextField tfLastName;
    @FXML private TextField tfEmail;
    @FXML private TextField tfPhone;
    @FXML private TextField tfEducationLevel;
    @FXML private TextArea taSkills;
    @FXML private ComboBox<String> cbStatus;
    @FXML private Label lblJobInfo;
    @FXML private Button btnVerifyEmail;
    @FXML private Label lblEmailStatus;

    private final CandidatService service = new CandidatService();

    private Candidat candidatToEdit = null;
    private JobPosition selectedJob = null;
    private boolean emailVerified = false;

    @FXML
    public void initialize() {
        cbStatus.getItems().addAll("NEW", "IN_REVIEW", "ACCEPTED", "REJECTED");
        cbStatus.getSelectionModel().selectFirst();

        // Validations de saisie
        tfPhone.setTextFormatter(new TextFormatter<String>(change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d*")) return change;
            return null;
        }));

        onlyLetters(tfFirstName);
        onlyLetters(tfLastName);

        tfEducationLevel.setTextFormatter(new TextFormatter<String>(change -> {
            String t = change.getControlNewText();
            if (t.matches("[a-zA-ZÀ-ÿ0-9 \\-+/]*")) return change;
            return null;
        }));

        if (btnVerifyEmail != null) {
            btnVerifyEmail.setDisable(true);
        }

        tfEmail.textProperty().addListener((obs, oldVal, newVal) -> {
            emailVerified = false;
            if (btnVerifyEmail != null) {
                btnVerifyEmail.setDisable(newVal == null || newVal.trim().isEmpty());
            }
            if (lblEmailStatus != null) {
                lblEmailStatus.setText("");
            }
        });
    }

    private void onlyLetters(TextField tf) {
        tf.setTextFormatter(new TextFormatter<String>(change -> {
            String t = change.getControlNewText();
            if (t.matches("[a-zA-ZÀ-ÿ ]*")) return change;
            return null;
        }));
    }

    public void setSelectedJob(JobPosition job) {
        this.selectedJob = job;
        if (lblJobInfo != null && job != null) {
            lblJobInfo.setText("Applying for: " + job.getTitle());
        }
    }

    @FXML
    private void onUploadCV() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/upload_cv.fxml"));
            Parent root = loader.load();

            UploadCVController controller = loader.getController();
            controller.setSelectedJob(selectedJob);
            controller.setOnCvExtractedListener(extractedData -> {
                tfFirstName.setText(extractedData.getFirstName());
                tfLastName.setText(extractedData.getLastName());
                tfEmail.setText(extractedData.getEmail());
                tfPhone.setText(extractedData.getPhone());
                tfEducationLevel.setText(extractedData.getEducationLevel());
                taSkills.setText(extractedData.getSkills());
                cbStatus.setValue(extractedData.getStatus() != null ? extractedData.getStatus() : "NEW");
            });

            Stage stage = new Stage();
            stage.setTitle("Uploader un CV");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            alert("Erreur lors de l'ouverture de la fenêtre d'upload : " + e.getMessage());
        }
    }

    public void setCandidatToEdit(Candidat c) {
        this.candidatToEdit = c;
        this.selectedJob = c.getJobPosition();

        tfFirstName.setText(c.getFirstName());
        tfLastName.setText(c.getLastName());
        tfEmail.setText(c.getEmail());
        tfPhone.setText(String.valueOf(c.getPhone()));
        tfEducationLevel.setText(c.getEducationLevel());
        taSkills.setText(c.getSkills());
        cbStatus.setValue(c.getStatus());
        emailVerified = true;

        if (lblJobInfo != null && selectedJob != null) {
            lblJobInfo.setText("Job: " + selectedJob.getTitle());
        }
    }

    @FXML
    private void onSave() {
        if (tfFirstName.getText().isEmpty() || tfLastName.getText().isEmpty()) {
            alert("First name and Last name are required!");
            return;
        }

        if (selectedJob == null && candidatToEdit == null) {
            alert("No job selected for this candidate!");
            return;
        }

        if (!emailVerified && candidatToEdit == null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Email non vérifié");
            confirm.setHeaderText("L'email n'a pas été vérifié");
            confirm.setContentText("Voulez-vous quand même continuer ?");
            if (confirm.showAndWait().get() != ButtonType.OK) {
                return;
            }
        }

        int phone;
        try {
            phone = Integer.parseInt(tfPhone.getText().trim());
        } catch (Exception e) {
            alert("Phone must be a number!");
            return;
        }

        if (candidatToEdit == null) {
            Candidat c = new Candidat();
            c.setFirstName(tfFirstName.getText());
            c.setLastName(tfLastName.getText());
            c.setEmail(tfEmail.getText());
            c.setPhone(phone);
            c.setEducationLevel(tfEducationLevel.getText());
            c.setSkills(taSkills.getText());
            c.setStatus(cbStatus.getValue());
            c.setJobPosition(selectedJob);
            service.create(c);
        } else {
            candidatToEdit.setFirstName(tfFirstName.getText());
            candidatToEdit.setLastName(tfLastName.getText());
            candidatToEdit.setEmail(tfEmail.getText());
            candidatToEdit.setPhone(phone);
            candidatToEdit.setEducationLevel(tfEducationLevel.getText());
            candidatToEdit.setSkills(taSkills.getText());
            candidatToEdit.setStatus(cbStatus.getValue());
            service.update(candidatToEdit);
        }

        close();
    }

    @FXML
    private void onCancel() {
        close();
    }

    private void close() {
        Stage stage = (Stage) tfFirstName.getScene().getWindow();
        stage.close();
    }

    private void alert(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}