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
                "PLANNING", "IN PROGRESS", "ACTIVE", "ON HOLD", "COMPLETED"
        ));

        saveButton.setOnAction(e -> saveProject());
        cancelButton.setOnAction(e -> closeWindow());

        setupValidationListeners();
        Platform.runLater(() -> nameField.requestFocus());
    }

    private void setupValidationListeners() {
        nameField.textProperty().addListener((obs, old, newVal) -> nameField.setStyle(""));
        budgetField.textProperty().addListener((obs, old, newVal) -> budgetField.setStyle(""));
        startDatePicker.valueProperty().addListener((obs, old, newVal) -> startDatePicker.setStyle(""));
        endDatePicker.valueProperty().addListener((obs, old, newVal) -> endDatePicker.setStyle(""));
        statusCombo.valueProperty().addListener((obs, old, newVal) -> statusCombo.setStyle(""));
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
        nameField.requestFocus();
    }

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    private void saveProject() {
        resetFieldStyles();
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
            budgetField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
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
        boolean valid = true;
        StringBuilder errorMsg = new StringBuilder();

        if (nameField.getText().trim().isEmpty()) {
            errorMsg.append("• Project Name is required.\n");
            nameField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
            valid = false;
        }

        if (startDatePicker.getValue() == null) {
            errorMsg.append("• Start Date is required.\n");
            startDatePicker.setStyle("-fx-border-color: red; -fx-border-width: 2;");
            valid = false;
        }

        if (endDatePicker.getValue() == null) {
            errorMsg.append("• End Date is required.\n");
            endDatePicker.setStyle("-fx-border-color: red; -fx-border-width: 2;");
            valid = false;
        } else if (startDatePicker.getValue() != null && endDatePicker.getValue().isBefore(startDatePicker.getValue())) {
            errorMsg.append("• End Date cannot be before Start Date.\n");
            endDatePicker.setStyle("-fx-border-color: red; -fx-border-width: 2;");
            valid = false;
        }

        if (statusCombo.getValue() == null) {
            errorMsg.append("• Status is required.\n");
            statusCombo.setStyle("-fx-border-color: red; -fx-border-width: 2;");
            valid = false;
        }

        if (budgetField.getText().trim().isEmpty()) {
            errorMsg.append("• Budget is required.\n");
            budgetField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
            valid = false;
        } else {
            try {
                double budget = Double.parseDouble(budgetField.getText().trim());
                if (budget < 0) {
                    errorMsg.append("• Budget cannot be negative.\n");
                    budgetField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
                    valid = false;
                }
            } catch (NumberFormatException e) {
                errorMsg.append("• Budget must be a valid number.\n");
                budgetField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
                valid = false;
            }
        }

        if (!valid) {
            showAlert("Validation Error", errorMsg.toString());
            return false;
        }
        return true;
    }

    private void resetFieldStyles() {
        nameField.setStyle("");
        startDatePicker.setStyle("");
        endDatePicker.setStyle("");
        statusCombo.setStyle("");
        budgetField.setStyle("");
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