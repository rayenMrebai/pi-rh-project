package Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.Services.projet.ProjectAssignmentService;
import org.example.Services.projet.ProjectService;
import org.example.model.projet.EmployesDTO;
import org.example.model.projet.Project;
import org.example.model.projet.ProjectAssignment;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Predicate;

public class DashboardController implements Initializable {

    // ListViews
    @FXML private ListView<Project> projectsListView;
    @FXML private ListView<ProjectAssignment> assignmentsListView;

    // Search fields
    @FXML private TextField searchProjectField;
    @FXML private TextField searchAssignmentField;

    // Other FXML elements
    @FXML private Label selectedProjectLabel;
    @FXML private Label detailName;
    @FXML private Label detailDescription;
    @FXML private Label detailStart;
    @FXML private Label detailEnd;
    @FXML private Label detailStatus;
    @FXML private Label detailBudget;
    @FXML private Label statusLabel;

    @FXML private Button addProjectButton;
    @FXML private Button editProjectButton;
    @FXML private Button deleteProjectButton;
    @FXML private Button assignEmployeeButton;
    @FXML private Button updateAssignmentButton;
    @FXML private Button removeAssignmentButton;

    private final ProjectService projectService = new ProjectService();
    private final ProjectAssignmentService assignmentService = new ProjectAssignmentService();
    private final ObservableList<EmployesDTO> employeeList = FXCollections.observableArrayList();

    // Données brutes
    private final ObservableList<Project> projectData = FXCollections.observableArrayList();
    private final ObservableList<ProjectAssignment> assignmentData = FXCollections.observableArrayList();

    // Listes filtrées pour la recherche
    private FilteredList<Project> filteredProjects;
    private FilteredList<ProjectAssignment> filteredAssignments;

    // Map pour obtenir le nom de l'employé à partir de son ID
    private Map<Integer, String> employeeNameMap = new HashMap<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Charger les employés et construire la map
        loadSampleEmployees();
        buildEmployeeNameMap();

        // Configurer l'affichage des ListView
        setupProjectsListView();
        setupAssignmentsListView();

        // Configurer la recherche
        setupSearch();

        // Installer les écouteurs
        setupListeners();

        // Charger les projets
        loadProjects();

        updateStatus("Connected - Database: INTEGRA_DB");
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

    private void buildEmployeeNameMap() {
        for (EmployesDTO emp : employeeList) {
            employeeNameMap.put(emp.getUserId(), emp.getUsername());
        }
    }

    private void setupProjectsListView() {
        projectsListView.setCellFactory(lv -> new ListCell<Project>() {
            @Override
            protected void updateItem(Project project, boolean empty) {
                super.updateItem(project, empty);
                if (empty || project == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox hbox = new HBox(10);
                    hbox.setAlignment(Pos.CENTER_LEFT);
                    hbox.setPadding(new Insets(5, 10, 5, 10));

                    Label idLabel = new Label(String.valueOf(project.getProjectId()));
                    idLabel.setPrefWidth(100);
                    idLabel.setStyle("-fx-font-weight: bold;");

                    Label nameLabel = new Label(project.getName());
                    nameLabel.setPrefWidth(250);

                    Label startLabel = new Label(project.getStartDate() != null ? project.getStartDate().toString() : "");
                    startLabel.setPrefWidth(120);

                    Label endLabel = new Label(project.getEndDate() != null ? project.getEndDate().toString() : "");
                    endLabel.setPrefWidth(120);

                    Label statusLabel = new Label(project.getStatus());
                    statusLabel.setPrefWidth(140);
                    // Coloration du statut
                    switch (project.getStatus()) {
                        case "IN PROGRESS":
                            statusLabel.setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
                            break;
                        case "PLANNING":
                            statusLabel.setStyle("-fx-text-fill: #2563eb; -fx-font-weight: bold;");
                            break;
                        case "COMPLETED":
                            statusLabel.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
                            break;
                        case "ON HOLD":
                            statusLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                            break;
                        default:
                            statusLabel.setStyle("-fx-text-fill: #6b7280;");
                    }

                    hbox.getChildren().addAll(idLabel, nameLabel, startLabel, endLabel, statusLabel);
                    setGraphic(hbox);
                }
            }
        });
    }

    private void setupAssignmentsListView() {
        assignmentsListView.setCellFactory(lv -> new ListCell<ProjectAssignment>() {
            @Override
            protected void updateItem(ProjectAssignment assignment, boolean empty) {
                super.updateItem(assignment, empty);
                if (empty || assignment == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox hbox = new HBox(10);
                    hbox.setAlignment(Pos.CENTER_LEFT);
                    hbox.setPadding(new Insets(5, 10, 5, 10));

                    Label idLabel = new Label(String.valueOf(assignment.getIdAssignment()));
                    idLabel.setPrefWidth(110);
                    idLabel.setStyle("-fx-font-weight: bold;");

                    // Récupérer le nom de l'employé depuis la map
                    String empName = employeeNameMap.getOrDefault(assignment.getEmployeeId(), "Emp " + assignment.getEmployeeId());
                    Label empLabel = new Label(empName);
                    empLabel.setPrefWidth(160);

                    Label roleLabel = new Label(assignment.getRole());
                    roleLabel.setPrefWidth(170);

                    Label allocLabel = new Label(assignment.getAllocationRate() + "%");
                    allocLabel.setPrefWidth(100);

                    Label startLabel = new Label(assignment.getAssignedFrom() != null ? assignment.getAssignedFrom().toString() : "");
                    startLabel.setPrefWidth(130);

                    Label endLabel = new Label(assignment.getAssignedTo() != null ? assignment.getAssignedTo().toString() : "");
                    endLabel.setPrefWidth(130);

                    hbox.getChildren().addAll(idLabel, empLabel, roleLabel, allocLabel, startLabel, endLabel);
                    setGraphic(hbox);
                }
            }
        });
    }

    private void setupSearch() {
        // Projets : filtrer à partir de projectData
        filteredProjects = new FilteredList<>(projectData, p -> true);
        projectsListView.setItems(filteredProjects);

        searchProjectField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredProjects.setPredicate(createProjectPredicate(newVal));
        });

        // Affectations : filtrer à partir de assignmentData
        filteredAssignments = new FilteredList<>(assignmentData, a -> true);
        assignmentsListView.setItems(filteredAssignments);

        searchAssignmentField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredAssignments.setPredicate(createAssignmentPredicate(newVal));
        });
    }

    private Predicate<Project> createProjectPredicate(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            return p -> true;
        }
        String lowerCaseFilter = searchText.toLowerCase().trim();
        return project -> {
            if (String.valueOf(project.getProjectId()).contains(lowerCaseFilter)) return true;
            if (project.getName().toLowerCase().contains(lowerCaseFilter)) return true;
            return false;
        };
    }

    private Predicate<ProjectAssignment> createAssignmentPredicate(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            return a -> true;
        }
        String lowerCaseFilter = searchText.toLowerCase().trim();
        return assignment -> {
            if (String.valueOf(assignment.getIdAssignment()).contains(lowerCaseFilter)) return true;
            if (assignment.getRole().toLowerCase().contains(lowerCaseFilter)) return true;
            String empName = employeeNameMap.getOrDefault(assignment.getEmployeeId(), "").toLowerCase();
            if (empName.contains(lowerCaseFilter)) return true;
            return false;
        };
    }

    private void loadProjects() {
        List<Project> projects = projectService.getAll();
        projectData.setAll(projects); // ← mise à jour directe, filteredProjects réagit automatiquement
        if (!projectData.isEmpty()) {
            projectsListView.getSelectionModel().selectFirst();
        }
    }

    private void loadAssignmentsForProject(int projectId) {
        List<ProjectAssignment> assignments = assignmentService.getByProjectId(projectId);
        assignmentData.setAll(assignments); // ← mise à jour directe
        // Optionnel : réinitialiser le champ de recherche si tu veux
        // searchAssignmentField.clear();
    }

    private void setupListeners() {
        projectsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                showProjectDetails(newVal);
                loadAssignmentsForProject(newVal.getProjectId());
                selectedProjectLabel.setText("Employees assigned to: " + newVal.getName());
            }
        });

        addProjectButton.setOnAction(e -> openProjectForm(null));
        editProjectButton.setOnAction(e -> {
            Project selected = projectsListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                openProjectForm(selected);
            } else {
                showAlert("No selection", "Please select a project to edit.");
            }
        });
        deleteProjectButton.setOnAction(e -> deleteSelectedProject());

        assignEmployeeButton.setOnAction(e -> openAssignmentForm(projectsListView.getSelectionModel().getSelectedItem(), null));
        updateAssignmentButton.setOnAction(e -> {
            ProjectAssignment selected = assignmentsListView.getSelectionModel().getSelectedItem();
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
        ProjectAssignment selected = assignmentsListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete this assignment?", ButtonType.YES, ButtonType.NO);
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    assignmentService.delete(selected.getIdAssignment());
                    loadAssignmentsForProject(projectsListView.getSelectionModel().getSelectedItem().getProjectId());
                    updateStatus("Assignment deleted.");
                }
            });
        } else {
            showAlert("No selection", "Please select an assignment to remove.");
        }
    }

    private void deleteSelectedProject() {
        Project selected = projectsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No selection", "Please select a project to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete project '" + selected.getName() + "'?\nThis will also delete all its assignments.",
                ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                List<ProjectAssignment> assignments = assignmentService.getByProjectId(selected.getProjectId());
                for (ProjectAssignment a : assignments) {
                    assignmentService.delete(a.getIdAssignment());
                }
                projectService.delete(selected.getProjectId());
                refreshAfterSave();
                updateStatus("Project and its assignments deleted.");
            }
        });
    }

    private void openProjectForm(Project project) {
        try {
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ProjectAssignmentForm.fxml"));
            Parent root = loader.load();
            ProjectAssignmentFormController controller = loader.getController();
            controller.setProject(project);
            controller.setAssignmentToEdit(assignment);
            controller.setEmployeeList(employeeList);
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
        Project selected = projectsListView.getSelectionModel().getSelectedItem();
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