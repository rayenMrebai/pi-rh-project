package Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.example.Services.projet.ProjectAssignmentService;
import org.example.Services.projet.ProjectService;
import org.example.model.projet.EmployesDTO;
import org.example.model.projet.Project;
import org.example.model.projet.ProjectAssignment;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class ProjectAssignmentFormController implements Initializable {

    @FXML private TableView<Project> projectsTable;
    @FXML private TableColumn<Project, Integer> projIdCol;
    @FXML private TableColumn<Project, String> projNameCol;
    @FXML private TableColumn<Project, String> projStatusCol;

    @FXML private ComboBox<EmployesDTO> employeeCombo;
    @FXML private TextField roleField;
    @FXML private Spinner<Integer> allocationSpinner;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Button assignButton;
    @FXML private Button cancelButton;

    private Project preselectedProject;
    private ProjectAssignment assignmentToEdit;
    private Runnable onSaveCallback;

    private final ProjectService projectService = new ProjectService();
    private final ProjectAssignmentService assignmentService = new ProjectAssignmentService();

    private ObservableList<Project> projectList = FXCollections.observableArrayList();
    private ObservableList<EmployesDTO> employeeList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupProjectsTable();
        loadProjects();

        // Configuration du Spinner
        SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 100);
        allocationSpinner.setValueFactory(valueFactory);
        allocationSpinner.setEditable(true);

        employeeCombo.setItems(employeeList);

        // Listeners pour effacer les styles d'erreur
        setupValidationListeners();

        assignButton.setOnAction(e -> saveAssignment());
        cancelButton.setOnAction(e -> closeWindow());
    }

    private void setupValidationListeners() {
        employeeCombo.valueProperty().addListener((obs, old, newVal) -> employeeCombo.setStyle(""));
        roleField.textProperty().addListener((obs, old, newVal) -> roleField.setStyle(""));
        startDatePicker.valueProperty().addListener((obs, old, newVal) -> startDatePicker.setStyle(""));
        endDatePicker.valueProperty().addListener((obs, old, newVal) -> endDatePicker.setStyle(""));
        allocationSpinner.valueProperty().addListener((obs, old, newVal) -> allocationSpinner.setStyle(""));
        // Note: le Spinner n'a pas de méthode setStyle directe sur sa valeur, on peut agir sur l'éditeur
        // mais pour simplifier, on remettra le style à zéro lors de la validation.
    }

    private void setupProjectsTable() {
        projIdCol.setCellValueFactory(new PropertyValueFactory<>("projectId"));
        projNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        projStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void loadProjects() {
        projectList.setAll(projectService.getAll());
        projectsTable.setItems(projectList);
    }

    public void setEmployeeList(ObservableList<EmployesDTO> list) {
        employeeList.setAll(list);
    }

    public void setProject(Project project) {
        this.preselectedProject = project;
        if (project != null) {
            projectsTable.getSelectionModel().select(project);
        }
    }

    public void setAssignmentToEdit(ProjectAssignment assignment) {
        this.assignmentToEdit = assignment;
        if (assignment != null) {
            roleField.setText(assignment.getRole());
            allocationSpinner.getValueFactory().setValue(assignment.getAllocationRate());
            startDatePicker.setValue(assignment.getAssignedFrom());
            endDatePicker.setValue(assignment.getAssignedTo());

            employeeList.stream()
                    .filter(e -> e.getUserId() == assignment.getEmployeeId())
                    .findFirst()
                    .ifPresent(emp -> employeeCombo.setValue(emp));

            if (preselectedProject == null) {
                setProject(assignment.getProject());
            }
            assignButton.setText("Update");
        } else {
            assignButton.setText("Assign");
        }
    }

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    private void saveAssignment() {
        // Réinitialiser les styles
        resetFieldStyles();

        Project selectedProject = projectsTable.getSelectionModel().getSelectedItem();
        EmployesDTO selectedEmployee = employeeCombo.getValue();
        String role = roleField.getText().trim();
        Integer allocation = allocationSpinner.getValue();
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();

        StringBuilder errorMsg = new StringBuilder();
        boolean valid = true;

        if (selectedProject == null) {
            errorMsg.append("• Please select a project.\n");
            // Pas de champ spécifique à surligner, on pourrait surligner le tableau
            // Pour simplifier, on se contente de l'alerte.
            valid = false;
        }

        if (selectedEmployee == null) {
            errorMsg.append("• Please select an employee.\n");
            employeeCombo.setStyle("-fx-border-color: red; -fx-border-width: 2;");
            valid = false;
        }

        if (role.isEmpty()) {
            errorMsg.append("• Role is required.\n");
            roleField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
            valid = false;
        }

        if (allocation == null || allocation < 0 || allocation > 100) {
            errorMsg.append("• Allocation must be between 0 and 100.\n");
            allocationSpinner.setStyle("-fx-border-color: red; -fx-border-width: 2;");
            valid = false;
        }

        if (start == null) {
            errorMsg.append("• Start date is required.\n");
            startDatePicker.setStyle("-fx-border-color: red; -fx-border-width: 2;");
            valid = false;
        }

        if (end == null) {
            errorMsg.append("• End date is required.\n");
            endDatePicker.setStyle("-fx-border-color: red; -fx-border-width: 2;");
            valid = false;
        }

        if (start != null && end != null && end.isBefore(start)) {
            errorMsg.append("• End date cannot be before start date.\n");
            endDatePicker.setStyle("-fx-border-color: red; -fx-border-width: 2;");
            valid = false;
        }

        if (!valid) {
            showAlert("Validation Error", errorMsg.toString());
            return;
        }

        if (assignmentToEdit == null) {
            ProjectAssignment newAssignment = new ProjectAssignment();
            newAssignment.setProject(selectedProject);
            newAssignment.setEmployeeId(selectedEmployee.getUserId());
            newAssignment.setRole(role);
            newAssignment.setAllocationRate(allocation);
            newAssignment.setAssignedFrom(start);
            newAssignment.setAssignedTo(end);
            assignmentService.create(newAssignment);
        } else {
            assignmentToEdit.setRole(role);
            assignmentToEdit.setAllocationRate(allocation);
            assignmentToEdit.setAssignedTo(end);
            assignmentService.update(assignmentToEdit);
        }

        if (onSaveCallback != null) onSaveCallback.run();
        closeWindow();
    }

    private void resetFieldStyles() {
        employeeCombo.setStyle("");
        roleField.setStyle("");
        allocationSpinner.setStyle("");
        startDatePicker.setStyle("");
        endDatePicker.setStyle("");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
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