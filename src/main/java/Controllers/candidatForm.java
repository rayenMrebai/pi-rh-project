package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.model.recrutement.Candidat;
import org.example.services.recrutement.CandidatService;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

public class candidatForm {

    @FXML private TextField tfFirstName;
    @FXML private TextField tfLastName;
    @FXML private TextField tfEmail;
    @FXML private TextField tfPhone;
    @FXML private TextField tfEducationLevel;
    @FXML private TextArea taSkills;
    @FXML private ComboBox<String> cbStatus;

    private final CandidatService service = new CandidatService();
    private Candidat candidatToEdit = null; // null => ADD

    @FXML
    public void initialize() {
        cbStatus.getItems().addAll("NEW", "IN_REVIEW", "ACCEPTED", "REJECTED");
        cbStatus.getSelectionModel().selectFirst();
        // 1) Phone : chiffres uniquement
        tfPhone.setTextFormatter(new TextFormatter<String>(change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d*")) return change;
            return null;
        }));

        // 2) First/Last name : lettres + espaces uniquement
        onlyLetters(tfFirstName);
        onlyLetters(tfLastName);

        // 3) Education : lettres + chiffres + espaces + - + / (ex: "3ème année", "Bac+3")
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

    // appelé depuis manageRecruitment
    public void setCandidatToEdit(Candidat c) {
        this.candidatToEdit = c;

        tfFirstName.setText(c.getFirstName());
        tfLastName.setText(c.getLastName());
        tfEmail.setText(c.getEmail());
        tfPhone.setText(String.valueOf(c.getPhone()));
        tfEducationLevel.setText(c.getEducationLevel());
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
            c.setEducationLevel(tfEducationLevel.getText());
            c.setSkills(taSkills.getText());
            c.setStatus(cbStatus.getValue());

            service.create(c);
        } else {
            // EDIT
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
