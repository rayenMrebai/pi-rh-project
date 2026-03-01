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
import java.util.List;

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
        // Validation des champs obligatoires
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

        // Construction de l'objet candidat (pour la vérification des doublons)
        Candidat candidat;
        if (candidatToEdit == null) {
            candidat = new Candidat();
        } else {
            candidat = candidatToEdit;
        }
        candidat.setFirstName(tfFirstName.getText());
        candidat.setLastName(tfLastName.getText());
        candidat.setEmail(tfEmail.getText());
        candidat.setPhone(phone);
        candidat.setEducationLevel(tfEducationLevel.getText());
        candidat.setSkills(taSkills.getText());
        candidat.setStatus(cbStatus.getValue());
        // Pour un nouveau candidat, on affecte le job sélectionné
        if (candidatToEdit == null) {
            candidat.setJobPosition(selectedJob);
        }

        // Vérification des doublons
        List<Candidat> duplicates = service.findDuplicates(candidat);
        if (!duplicates.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Doublon potentiel");
            alert.setHeaderText("Des candidats similaires existent déjà :");
            StringBuilder sb = new StringBuilder();
            for (Candidat d : duplicates) {
                sb.append("• ").append(d.getFirstName()).append(" ").append(d.getLastName())
                        .append(" (").append(d.getEmail()).append(")\n");
            }
            sb.append("\nVoulez-vous quand même enregistrer ce candidat ?");
            alert.setContentText(sb.toString());
            alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

            if (alert.showAndWait().get() == ButtonType.NO) {
                return; // Annuler la sauvegarde
            }
        }

        // Sauvegarde
        if (candidatToEdit == null) {
            service.create(candidat);   // ou service.add(candidat) selon votre implémentation
        } else {
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