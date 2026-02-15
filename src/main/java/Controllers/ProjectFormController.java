package Controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.Services.projet.ProjectService;
import org.example.model.projet.Project;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class ProjectFormController implements Initializable {

    @FXML private TextField idField;
    @FXML private TextField nameField;
    @FXML private TextArea descriptionField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<String> statusCombo;
    @FXML private TextField budgetField;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private Project projectToEdit;
    private Runnable onSaveCallback;

    private final ProjectService projectService = new ProjectService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        statusCombo.setItems(FXCollections.observableArrayList(
                "PLANNING", "IN PROGRESS", "ON HOLD", "COMPLETED"
        ));

        saveButton.setOnAction(e -> saveProject());
        cancelButton.setOnAction(e -> closeWindow());

        // Demander le focus après que la scène soit affichée
        Platform.runLater(() -> nameField.requestFocus());
    }

    public void setProjectToEdit(Project project) {
        this.projectToEdit = project;
        if (project != null) {
            idField.setText(String.valueOf(project.getProjectId()));
            nameField.setText(project.getName());
            descriptionField.setText(project.getDescription());
            startDatePicker.setValue(project.getStartDate());
            endDatePicker.setValue(project.getEndDate());
            statusCombo.setValue(project.getStatus());
            budgetField.setText(String.valueOf(project.getBudget()));
        } else {
            idField.clear();
            nameField.clear();
            descriptionField.clear();
            startDatePicker.setValue(null);
            endDatePicker.setValue(null);
            statusCombo.setValue(null);
            budgetField.clear();
        }
        // Focus sur le champ nom
        nameField.requestFocus();
    }

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    private void saveProject() {
        if (!validateInputs()) return;

        Project project = projectToEdit != null ? projectToEdit : new Project();
        project.setName(nameField.getText().trim());
        project.setDescription(descriptionField.getText().trim());
        project.setStartDate(startDatePicker.getValue());
        project.setEndDate(endDatePicker.getValue());
        project.setStatus(statusCombo.getValue());
        try {
            project.setBudget(Double.parseDouble(budgetField.getText().trim()));
        } catch (NumberFormatException e) {
            showAlert("Invalid Budget", "Budget must be a number.");
            return;
        }

        if (projectToEdit == null) {
            projectService.create(project);
        } else {
            projectService.update(project);
        }

        if (onSaveCallback != null) onSaveCallback.run();
        closeWindow();
    }

    private boolean validateInputs() {
        String error = "";
        if (nameField.getText().trim().isEmpty()) error += "Project Name is required.\n";
        if (startDatePicker.getValue() == null) error += "Start Date is required.\n";
        if (endDatePicker.getValue() == null) error += "End Date is required.\n";
        if (statusCombo.getValue() == null) error += "Status is required.\n";
        if (budgetField.getText().trim().isEmpty()) error += "Budget is required.\n";
        else {
            try {
                Double.parseDouble(budgetField.getText().trim());
            } catch (NumberFormatException e) {
                error += "Budget must be a valid number.\n";
            }
        }
        if (!error.isEmpty()) {
            showAlert("Validation Error", error);
            return false;
        }
        return true;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}