package Controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.Services.projet.ProjectAssignmentService;
import org.example.Services.projet.ProjectService;
import org.example.model.projet.EmployesDTO;
import org.example.model.projet.Project;
import org.example.model.projet.ProjectAssignment;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML private TableView<Project> projectsTable;
    @FXML private TableColumn<Project, Integer> projectIdCol;
    @FXML private TableColumn<Project, String> projectNameCol;
    @FXML private TableColumn<Project, java.time.LocalDate> projectStartCol;
    @FXML private TableColumn<Project, java.time.LocalDate> projectEndCol;
    @FXML private TableColumn<Project, String> projectStatusCol;

    @FXML private TableView<ProjectAssignment> assignmentsTable;
    @FXML private TableColumn<ProjectAssignment, Integer> assignmentIdCol;
    @FXML private TableColumn<ProjectAssignment, String> employeeNameCol;
    @FXML private TableColumn<ProjectAssignment, String> roleCol;
    @FXML private TableColumn<ProjectAssignment, Integer> allocationCol;
    @FXML private TableColumn<ProjectAssignment, java.time.LocalDate> assignmentStartCol;
    @FXML private TableColumn<ProjectAssignment, java.time.LocalDate> assignmentEndCol;

    @FXML private Label selectedProjectLabel;
    @FXML private Label detailName;
    @FXML private Label detailDescription;
    @FXML private Label detailStart;
    @FXML private Label detailEnd;
    @FXML private Label detailStatus;
    @FXML private Label detailBudget;
    @FXML private Label statusLabel;

    @FXML private Button addProjectButton;
    @FXML private Button editProjectButton;          // NOUVEAU
    @FXML private Button deleteProjectButton;        // NOUVEAU
    @FXML private Button assignEmployeeButton;
    @FXML private Button updateAssignmentButton;
    @FXML private Button removeAssignmentButton;

    private final ProjectService projectService = new ProjectService();
    private final ProjectAssignmentService assignmentService = new ProjectAssignmentService();
    private final ObservableList<EmployesDTO> employeeList = FXCollections.observableArrayList();

    private ObservableList<Project> projectData = FXCollections.observableArrayList();
    private ObservableList<ProjectAssignment> assignmentData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupProjectTable();
        setupAssignmentTable();

        // Les écouteurs doivent être installés avant le chargement des données
        setupListeners();

        // Charger la liste fictive des employés
        loadSampleEmployees();

        // Charger les projets depuis la base
        loadProjects();

        updateStatus("Connected - Database: INTEGRA_DB");
    }

    private void setupProjectTable() {
        projectIdCol.setCellValueFactory(new PropertyValueFactory<>("projectId"));
        projectNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        projectStartCol.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        projectEndCol.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        projectStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void setupAssignmentTable() {
        assignmentIdCol.setCellValueFactory(new PropertyValueFactory<>("idAssignment"));
        // Pour l'instant on affiche juste "Emp " + employeeId (à améliorer plus tard)
        employeeNameCol.setCellValueFactory(cellData ->
                new SimpleStringProperty("Emp " + cellData.getValue().getEmployeeId()));
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        allocationCol.setCellValueFactory(new PropertyValueFactory<>("allocationRate"));
        assignmentStartCol.setCellValueFactory(new PropertyValueFactory<>("assignedFrom"));
        assignmentEndCol.setCellValueFactory(new PropertyValueFactory<>("assignedTo"));
    }

    private void loadSampleEmployees() {
        employeeList.addAll(
                new EmployesDTO(101, "Sarah Johnson"),
                new EmployesDTO(102, "Michael Chen"),
                new EmployesDTO(103, "Emily Rodriguez"),
                new EmployesDTO(104, "David Thompson"),
                new EmployesDTO(105, "Jessica Martinez")
        );
    }

    private void loadProjects() {
        List<Project> projects = projectService.getAll();
        projectData.setAll(projects);
        projectsTable.setItems(projectData);
        if (!projects.isEmpty()) {
            projectsTable.getSelectionModel().selectFirst(); // déclenche l'affichage des détails et des affectations
        }
    }

    private void loadAssignmentsForProject(int projectId) {
        List<ProjectAssignment> assignments = assignmentService.getByProjectId(projectId);
        assignmentData.setAll(assignments);
        assignmentsTable.setItems(assignmentData);
    }

    private void setupListeners() {
        projectsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                showProjectDetails(newVal);
                loadAssignmentsForProject(newVal.getProjectId());
                selectedProjectLabel.setText("Employees assigned to: " + newVal.getName());
            }
        });

        addProjectButton.setOnAction(e -> openProjectForm(null));
        editProjectButton.setOnAction(e -> {
            Project selected = projectsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                openProjectForm(selected);   // Ouvre le formulaire pré-rempli
            } else {
                showAlert("No selection", "Please select a project to edit.");
            }
        });
        deleteProjectButton.setOnAction(e -> deleteSelectedProject());

        assignEmployeeButton.setOnAction(e -> openAssignmentForm(projectsTable.getSelectionModel().getSelectedItem(), null));
        updateAssignmentButton.setOnAction(e -> {
            ProjectAssignment selected = assignmentsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                openAssignmentForm(selected.getProject(), selected);
            } else {
                showAlert("No selection", "Please select an assignment to update.");
            }
        });
        removeAssignmentButton.setOnAction(e -> deleteSelectedAssignment());
    }

    private void showProjectDetails(Project p) {
        detailName.setText(p.getName());
        detailDescription.setText(p.getDescription());
        detailStart.setText(p.getStartDate() != null ? p.getStartDate().toString() : "");
        detailEnd.setText(p.getEndDate() != null ? p.getEndDate().toString() : "");
        detailStatus.setText(p.getStatus());
        detailBudget.setText(String.format("%.2f", p.getBudget()));
    }

    private void deleteSelectedAssignment() {
        ProjectAssignment selected = assignmentsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete this assignment?", ButtonType.YES, ButtonType.NO);
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    assignmentService.delete(selected.getIdAssignment());
                    loadAssignmentsForProject(projectsTable.getSelectionModel().getSelectedItem().getProjectId());
                    updateStatus("Assignment deleted.");
                }
            });
        } else {
            showAlert("No selection", "Please select an assignment to remove.");
        }
    }

    // Nouvelle méthode : suppression d'un projet avec ses affectations
    private void deleteSelectedProject() {
        Project selected = projectsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No selection", "Please select a project to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete project '" + selected.getName() + "'?\nThis will also delete all its assignments.",
                ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                // Supprimer toutes les affectations de ce projet
                List<ProjectAssignment> assignments = assignmentService.getByProjectId(selected.getProjectId());
                for (ProjectAssignment a : assignments) {
                    assignmentService.delete(a.getIdAssignment());
                }
                // Supprimer le projet
                projectService.delete(selected.getProjectId());

                // Rafraîchir la vue
                refreshAfterSave();
                updateStatus("Project and its assignments deleted.");
            }
        });
    }

    private void openProjectForm(Project project) {
        try {
            // Attention : le nom du fichier doit correspondre exactement (avec majuscule)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ProjectForm.fxml"));
            Parent root = loader.load();
            ProjectFormController controller = loader.getController();
            controller.setProjectToEdit(project);
            controller.setOnSaveCallback(this::refreshAfterSave);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(project == null ? "Add Project" : "Edit Project");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not open form: " + e.getMessage());
        }
    }

    private void openAssignmentForm(Project project, ProjectAssignment assignment) {
        try {
            // Attention : le nom du fichier doit correspondre exactement (avec majuscule)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ProjectAssignmentForm.fxml"));
            Parent root = loader.load();
            ProjectAssignmentFormController controller = loader.getController();
            controller.setProject(project);
            controller.setAssignmentToEdit(assignment);
            controller.setEmployeeList(employeeList); // on passe la liste fictive
            controller.setOnSaveCallback(() -> {
                if (project != null) {
                    loadAssignmentsForProject(project.getProjectId());
                }
                updateStatus("Assignment saved.");
            });

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(assignment == null ? "Assign Employee" : "Update Assignment");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not open form: " + e.getMessage());
        }
    }

    private void refreshAfterSave() {
        loadProjects();
        Project selected = projectsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            loadAssignmentsForProject(selected.getProjectId());
        }
        updateStatus("Data refreshed.");
    }

    private void updateStatus(String message) {
        statusLabel.setText("Connected - Database: INTEGRA_DB | " + message);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}