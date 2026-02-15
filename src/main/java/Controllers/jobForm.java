package Controllers;

import javafx.collections.FXCollections;
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

    private final JobPositionService jobService = new JobPositionService();

    @FXML
    public void initialize() {
        cbStatus.setItems(FXCollections.observableArrayList("Open", "On Hold", "Closed"));
        cbStatus.getSelectionModel().select("Open");
        dpPostedAt.setValue(LocalDate.now());
    }

    @FXML
    private void onSave() {
        if (tfTitle.getText().isBlank()) {
            showAlert("Title is required.");
            return;
        }

        JobPosition job = new JobPosition();
        job.setTitle(tfTitle.getText().trim());
        job.setDepartement(tfDepartement.getText().trim());
        job.setEmployeeType(tfEmployeeType.getText().trim());
        job.setDescription(taDescription.getText().trim());
        job.setStatus(cbStatus.getValue());
        job.setPostedAt(dpPostedAt.getValue());

        jobService.create(job);
        close();
    }

    @FXML
    private void onCancel() {
        close();
    }

    private void close() {
        Stage stage = (Stage) tfTitle.getScene().getWindow();
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
