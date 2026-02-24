package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.model.recrutement.Candidat;
import org.example.model.recrutement.JobPosition;
import org.example.services.recrutement.CandidatService;

public class candidatForm {

    @FXML private TextField tfFirstName;
    @FXML private TextField tfLastName;
    @FXML private TextField tfEmail;
    @FXML private TextField tfPhone;
    @FXML private TextField tfEducationLevel;
    @FXML private TextArea taSkills;
    @FXML private ComboBox<String> cbStatus;
    @FXML private Label lblJobInfo; // Nouveau label pour afficher le job

    private final CandidatService service = new CandidatService();
    private Candidat candidatToEdit = null;
    private JobPosition selectedJob = null;

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
    }

    private void onlyLetters(TextField tf) {
        tf.setTextFormatter(new TextFormatter<String>(change -> {
            String t = change.getControlNewText();
            if (t.matches("[a-zA-ZÀ-ÿ ]*")) return change;
            return null;
        }));
    }

    // Nouvelle méthode pour définir le job sélectionné (pour l'ajout)
    public void setSelectedJob(JobPosition job) {
        this.selectedJob = job;
        if (lblJobInfo != null && job != null) {
            lblJobInfo.setText("Applying for: " + job.getTitle());
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

        int phone;
        try {
            phone = Integer.parseInt(tfPhone.getText().trim());
        } catch (Exception e) {
            alert("Phone must be a number!");
            return;
        }

        if (candidatToEdit == null) {
            // ADD - Nouveau candidat
            Candidat c = new Candidat();
            c.setFirstName(tfFirstName.getText());
            c.setLastName(tfLastName.getText());
            c.setEmail(tfEmail.getText());
            c.setPhone(phone);
            c.setEducationLevel(tfEducationLevel.getText());
            c.setSkills(taSkills.getText());
            c.setStatus(cbStatus.getValue());
            c.setJobPosition(selectedJob); // Lier au job sélectionné

            service.create(c);
        } else {
            // EDIT - Mise à jour
            candidatToEdit.setFirstName(tfFirstName.getText());
            candidatToEdit.setLastName(tfLastName.getText());
            candidatToEdit.setEmail(tfEmail.getText());
            candidatToEdit.setPhone(phone);
            candidatToEdit.setEducationLevel(tfEducationLevel.getText());
            candidatToEdit.setSkills(taSkills.getText());
            candidatToEdit.setStatus(cbStatus.getValue());
            // Ne pas changer le job en édition

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