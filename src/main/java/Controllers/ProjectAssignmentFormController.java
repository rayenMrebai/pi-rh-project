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

        // Lier la ComboBox à la liste des employés (qui sera remplie plus tard par setEmployeeList)
        employeeCombo.setItems(employeeList);

        assignButton.setOnAction(e -> saveAssignment());
        cancelButton.setOnAction(e -> closeWindow());
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

    /**
     * Reçoit la liste des employés depuis le DashboardController
     */
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

            // Sélectionner l'employé correspondant dans la comboBox
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
        Project selectedProject = projectsTable.getSelectionModel().getSelectedItem();
        EmployesDTO selectedEmployee = employeeCombo.getValue();
        String role = roleField.getText().trim();
        Integer allocation = allocationSpinner.getValue();
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();

        // Validation
        if (selectedProject == null) {
            showAlert("No Project", "Please select a project.");
            return;
        }
        if (selectedEmployee == null) {
            showAlert("No Employee", "Please select an employee.");
            return;
        }
        if (role.isEmpty()) {
            showAlert("Role Required", "Please enter the role.");
            return;
        }
        if (allocation == null || allocation < 0 || allocation > 100) {
            showAlert("Invalid Allocation", "Allocation must be between 0 and 100.");
            return;
        }
        if (start == null || end == null) {
            showAlert("Dates Required", "Please enter both start and end dates.");
            return;
        }
        if (end.isBefore(start)) {
            showAlert("Invalid Dates", "End date cannot be before start date.");
            return;
        }

        if (assignmentToEdit == null) {
            // Création
            ProjectAssignment newAssignment = new ProjectAssignment();
            newAssignment.setProject(selectedProject);
            newAssignment.setEmployeeId(selectedEmployee.getUserId());
            newAssignment.setRole(role);
            newAssignment.setAllocationRate(allocation);
            newAssignment.setAssignedFrom(start);
            newAssignment.setAssignedTo(end);
            assignmentService.create(newAssignment);
        } else {
            // Mise à jour
            assignmentToEdit.setRole(role);
            assignmentToEdit.setAllocationRate(allocation);
            assignmentToEdit.setAssignedTo(end);
            assignmentService.update(assignmentToEdit);
        }

        if (onSaveCallback != null) onSaveCallback.run();
        closeWindow();
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