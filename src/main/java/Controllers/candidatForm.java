package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.model.recrutement.Candidat;
import org.example.services.recrutement.CandidatService;

public class candidatForm {

    @FXML private TextField tfFirstName;
    @FXML private TextField tfLastName;
    @FXML private TextField tfEmail;
    @FXML private TextField tfPhone;
    @FXML private TextField tfEducation;
    @FXML private TextArea taSkills;
    @FXML private ComboBox<String> cbStatus;

    private final CandidatService service = new CandidatService();
    private Candidat candidatToEdit = null; // null => ADD

    @FXML
    public void initialize() {
        cbStatus.getItems().addAll("NEW", "IN_REVIEW", "ACCEPTED", "REJECTED");
        cbStatus.getSelectionModel().selectFirst();
    }

    // appelé depuis manageRecruitment
    public void setCandidatToEdit(Candidat c) {
        this.candidatToEdit = c;

        tfFirstName.setText(c.getFirstName());
        tfLastName.setText(c.getLastName());
        tfEmail.setText(c.getEmail());
        tfPhone.setText(String.valueOf(c.getPhone()));
        tfEducation.setText(c.getEducationLevel());
        taSkills.setText(c.getSkills());
        cbStatus.setValue(c.getStatus());
    }

    @FXML
    private void onSave() {
        if (tfFirstName.getText().isEmpty() || tfLastName.getText().isEmpty()) {
            alert("First name and Last name are required!");
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
            // ADD
            Candidat c = new Candidat();
            c.setFirstName(tfFirstName.getText());
            c.setLastName(tfLastName.getText());
            c.setEmail(tfEmail.getText());
            c.setPhone(phone);
            c.setEducationLevel(tfEducation.getText());
            c.setSkills(taSkills.getText());
            c.setStatus(cbStatus.getValue());

            service.create(c);
        } else {
            // EDIT
            candidatToEdit.setFirstName(tfFirstName.getText());
            candidatToEdit.setLastName(tfLastName.getText());
            candidatToEdit.setEmail(tfEmail.getText());
            candidatToEdit.setPhone(phone);
            candidatToEdit.setEducationLevel(tfEducation.getText());
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
