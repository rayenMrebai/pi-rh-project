package Controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.model.recrutement.Candidat;
import org.example.model.recrutement.JobPosition;
import org.example.services.recrutement.CandidatService;
import org.example.services.email.ZeroBounceEmailService;

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
    private final ZeroBounceEmailService emailService = ZeroBounceEmailService.getInstance();
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

        // Désactiver le bouton de vérification au début
        if (btnVerifyEmail != null) {
            btnVerifyEmail.setDisable(true);
        }

        // Ajouter un listener sur le champ email
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
        emailVerified = true; // Pour l'édition, on considère que l'email est déjà vérifié

        if (lblJobInfo != null && selectedJob != null) {
            lblJobInfo.setText("Job: " + selectedJob.getTitle());
        }
    }

    @FXML
    private void onVerifyEmail() {
        String email = tfEmail.getText().trim();
        if (email.isEmpty()) {
            alert("Veuillez entrer un email à vérifier.");
            return;
        }

        // Désactiver le bouton pendant la vérification
        btnVerifyEmail.setDisable(true);
        btnVerifyEmail.setText("Vérification...");
        lblEmailStatus.setText("⏳ Vérification en cours...");
        lblEmailStatus.setStyle("-fx-text-fill: #f59e0b;");

        emailService.verifyEmail(email)
                .thenAccept(result -> {
                    Platform.runLater(() -> {
                        btnVerifyEmail.setDisable(false);
                        btnVerifyEmail.setText("Vérifier");

                        if (result.isValid()) {
                            emailVerified = true;
                            lblEmailStatus.setText("✅ " + result.getMessage());
                            lblEmailStatus.setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold;");

                            if (result.getDetails() != null) {
                                Tooltip tooltip = new Tooltip(result.getDetails());
                                Tooltip.install(lblEmailStatus, tooltip);
                            }
                        } else {
                            emailVerified = false;
                            lblEmailStatus.setText("❌ " + result.getMessage());
                            lblEmailStatus.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                        }
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        btnVerifyEmail.setDisable(false);
                        btnVerifyEmail.setText("Vérifier");
                        lblEmailStatus.setText("❌ Erreur: " + ex.getMessage());
                        lblEmailStatus.setStyle("-fx-text-fill: #ef4444;");
                    });
                    return null;
                });
    }

    @FXML
    private void onVerifyBasic() {
        String email = tfEmail.getText().trim();
        ZeroBounceEmailService.EmailVerificationResult result = emailService.verifyBasic(email);

        if (result.isValid()) {
            emailVerified = true;
            lblEmailStatus.setText("✅ " + result.getMessage());
            lblEmailStatus.setStyle("-fx-text-fill: #22c55e;");
        } else {
            emailVerified = false;
            lblEmailStatus.setText("❌ " + result.getMessage());
            lblEmailStatus.setStyle("-fx-text-fill: #ef4444;");
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

        // Vérifier l'email si ce n'est pas déjà fait (pour un nouveau candidat)
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
            // ADD - Nouveau candidat
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
            // EDIT - Mise à jour
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