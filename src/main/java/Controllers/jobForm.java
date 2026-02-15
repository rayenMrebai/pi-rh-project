package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.model.recrutement.JobPosition;
import org.example.services.recrutement.JobPositionService;
import javafx.scene.control.TextFormatter;


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

    // si != null => EDIT
    private JobPosition jobToEdit;
    private Runnable onSaveCallback;

    @FXML
    public void initialize() {
        cbStatus.getItems().addAll("Open", "Closed", "Paused");
        cbStatus.getSelectionModel().select("Open");

        // date par défaut
        dpPostedAt.setValue(LocalDate.now());

            cbStatus.getItems().addAll("Open", "Closed", "Paused");
            cbStatus.getSelectionModel().select("Open");
            dpPostedAt.setValue(LocalDate.now());

            // ====== CONTROLES SAISIE ======
            onlyLettersAndSpaces(tfTitle);
            onlyLettersAndSpaces(tfDepartement);
            onlyLettersSpacesAndDash(tfEmployeeType); // ex: "Full-time", "Part time"
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


    // Appelé depuis manageRecruitment quand on veut modifier
    public void setJobToEdit(JobPosition job) {
        this.jobToEdit = job;

        tfTitle.setText(job.getTitle());
        tfDepartement.setText(job.getDepartement());
        tfEmployeeType.setText(job.getEmployeeType());
        taDescription.setText(job.getDescription());
        cbStatus.getSelectionModel().select(job.getStatus() != null ? job.getStatus() : "Open");
        dpPostedAt.setValue(job.getPostedAt() != null ? job.getPostedAt() : LocalDate.now());
    }

    @FXML
    private void onSave() {
        System.out.println("=== onSave called ===");
        System.out.println("jobToEdit is null? " + (jobToEdit == null));

        // petites validations
        if (tfTitle.getText().trim().isEmpty()
                || tfDepartement.getText().trim().isEmpty()
                || tfEmployeeType.getText().trim().isEmpty()) {

            Alert a = new Alert(Alert.AlertType.WARNING);
            a.setTitle("Champs obligatoires");
            a.setHeaderText(null);
            a.setContentText("Title / Departement / EmployeeType sont obligatoires.");
            a.showAndWait();
            return;
        }

        try {
            if (jobToEdit == null) {
                // ADD
                System.out.println("Mode ADD - Création d'un nouveau job");
                JobPosition j = new JobPosition(
                        tfTitle.getText().trim(),
                        tfDepartement.getText().trim(),
                        tfEmployeeType.getText().trim(),
                        taDescription.getText() != null ? taDescription.getText().trim() : "",
                        cbStatus.getValue(),
                        dpPostedAt.getValue()
                );

                System.out.println("Job avant création: " + j);
                jobService.create(j);
                System.out.println("Job après création, ID: " + j.getIdJob());

            } else {
                // EDIT
                System.out.println("Mode EDIT - Mise à jour du job ID: " + jobToEdit.getIdJob());
                jobToEdit.setTitle(tfTitle.getText().trim());
                jobToEdit.setDepartement(tfDepartement.getText().trim());
                jobToEdit.setEmployeeType(tfEmployeeType.getText().trim());
                jobToEdit.setDescription(taDescription.getText() != null ? taDescription.getText().trim() : "");
                jobToEdit.setStatus(cbStatus.getValue());
                jobToEdit.setPostedAt(dpPostedAt.getValue());

                jobService.update(jobToEdit);
                System.out.println("Job mis à jour: " + jobToEdit);
            }

            close();
            if (onSaveCallback != null) onSaveCallback.run();

        } catch (RuntimeException ex) {
            System.out.println("EXCEPTION: " + ex.getMessage());
            ex.printStackTrace();
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Erreur SQL");
            a.setHeaderText("Insertion / Modification impossible");
            a.setContentText(ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage());
            a.showAndWait();
        }
    }


    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
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








