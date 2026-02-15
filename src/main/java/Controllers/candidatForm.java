package Controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.model.recrutement.Candidat;
import org.example.model.recrutement.JobPosition;
import org.example.services.recrutement.CandidatService;
import org.example.services.recrutement.JobPositionService;

import java.util.List;

public class candidatForm {

    @FXML private TextField tfFirstName;
    @FXML private TextField tfLastName;
    @FXML private TextField tfEmail;
    @FXML private TextField tfPhone;
    @FXML private TextField tfEducation;
    @FXML private TextArea taSkills;
    @FXML private ComboBox<String> cbStatus;
    @FXML private ComboBox<JobPosition> cbJob;

    private final CandidatService candidatService = new CandidatService();
    private final JobPositionService jobService = new JobPositionService();

    @FXML
    public void initialize() {
        cbStatus.setItems(FXCollections.observableArrayList("New", "Applied", "Interview", "Accepted", "Rejected"));
        cbStatus.getSelectionModel().select("New");

        List<JobPosition> jobs = jobService.getAll();
        cbJob.setItems(FXCollections.observableArrayList(jobs));

        // affichage lisible dans la combobox
        cbJob.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(JobPosition item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getIdJob() + " - " + item.getTitle());
            }
        });
        cbJob.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(JobPosition item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getIdJob() + " - " + item.getTitle());
            }
        });
    }

    @FXML
    private void onSave() {
        if (tfFirstName.getText().isBlank() || tfLastName.getText().isBlank()) {
            showAlert("First name and last name are required.");
            return;
        }

        int phone;
        try {
            phone = Integer.parseInt(tfPhone.getText().trim());
        } catch (Exception e) {
            showAlert("Phone must be a number.");
            return;
        }

        Candidat c = new Candidat();
        c.setFirstName(tfFirstName.getText().trim());
        c.setLastName(tfLastName.getText().trim());
        c.setEmail(tfEmail.getText().trim());
        c.setPhone(phone);
        c.setEducationLevel(tfEducation.getText().trim());
        c.setSkills(taSkills.getText().trim());
        c.setStatus(cbStatus.getValue());

        JobPosition selectedJob = cbJob.getSelectionModel().getSelectedItem();
        c.setJobPosition(selectedJob); // objet avec idJob (pas de join)

        candidatService.create(c);
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

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle("Validation");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
