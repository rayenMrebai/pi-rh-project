package Controllers;

import javafx.application.Platform;
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
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.Services.currency.CurrencyService;
import org.example.Services.excel.ExcelExportService;
import org.example.Services.pdf.PdfExportService;
import org.example.Services.projet.ProjectAssignmentService;
import org.example.Services.projet.ProjectService;
import org.example.model.projet.EmployesDTO;
import org.example.model.projet.Project;
import org.example.model.projet.ProjectAssignment;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public class DashboardController implements Initializable {

    // ListViews
    @FXML private ListView<Project> projectsListView;
    @FXML private ListView<ProjectAssignment> assignmentsListView;

    // Search fields
    @FXML private TextField searchProjectField;
    @FXML private TextField searchAssignmentField;
    @FXML private ComboBox<String> statusFilterCombo;

    // Other FXML elements
    @FXML private Label selectedProjectLabel;
    @FXML private Label detailName;
    @FXML private Label detailDescription;
    @FXML private Label detailStart;
    @FXML private Label detailEnd;
    @FXML private Label detailStatus;
    @FXML private Label detailBudget;
    @FXML private Label detailBudgetCurrency;
    @FXML private Button budgetToggleButton;
    @FXML private Label statusLabel;
    @FXML private Label rateDisplayLabel;

    @FXML private Button addProjectButton;
    @FXML private Button editProjectButton;
    @FXML private Button deleteProjectButton;
    @FXML private Button assignEmployeeButton;
    @FXML private Button updateAssignmentButton;
    @FXML private Button removeAssignmentButton;
    @FXML private Button exportAllPdfButton;
    @FXML private Button exportProjectPdfButton;
    @FXML private Button exportAllExcelButton;
    @FXML private Button exportProjectExcelButton;
    @FXML private Button aiAssistantButton;         // Ollama
    @FXML private Button aiRecommendButton;        // Nouveau bouton pour la recommandation
    @FXML private Label usdRateLabel;
    @FXML private Label eurRateLabel;

    private final ProjectService projectService = new ProjectService();
    private final ProjectAssignmentService assignmentService = new ProjectAssignmentService();
    private final CurrencyService currencyService = new CurrencyService();
    private final ObservableList<EmployesDTO> employeeList = FXCollections.observableArrayList();

    private final ObservableList<Project> projectData = FXCollections.observableArrayList();
    private final ObservableList<ProjectAssignment> assignmentData = FXCollections.observableArrayList();

    private FilteredList<Project> filteredProjects;
    private FilteredList<ProjectAssignment> filteredAssignments;

    private Map<Integer, String> employeeNameMap = new HashMap<>();

    private double originalBudgetTND;
    private String currentCurrency = "TND";

    private ScheduledExecutorService scheduler;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadSampleEmployees();
        buildEmployeeNameMap();

        statusFilterCombo.setItems(FXCollections.observableArrayList(
                "All statuses", "PLANNING", "IN PROGRESS", "ACTIVE", "ON HOLD", "COMPLETED"
        ));
        statusFilterCombo.setValue("All statuses");

        setupProjectsListView();
        setupAssignmentsListView();
        setupSearch();
        setupListeners();
        loadProjects();

        budgetToggleButton.setOnAction(e -> toggleBudgetCurrency());
        startRateUpdater();
        updateStatus("Connected - Database: INTEGRA_DB");
    }

    private void startRateUpdater() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                // Récupérer les taux depuis le service
                Map<String, Double> rates = currencyService.getRates(); // méthode à ajouter
                double usd = rates.get("USD");
                double eur = rates.get("EUR");
                Platform.runLater(() -> {
                    usdRateLabel.setText(String.format("%.2f", usd));
                    eurRateLabel.setText(String.format("%.2f", eur));
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    usdRateLabel.setText("—");
                    eurRateLabel.setText("—");
                });
            }
        }, 0, 60, TimeUnit.SECONDS);
    }

    public void stopUpdater() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }

    private void loadSampleEmployees() {
        employeeList.addAll(
                new EmployesDTO(101, "Sarah Johnson"),
                new EmployesDTO(102, "Michael Chen"),
                new EmployesDTO(103, "Emily Rodriguez"),
                new EmployesDTO(104, "David Thompson"),
                new EmployesDTO(105, "Jessica Martinez"),
                new EmployesDTO(106, "Ahmed Benali"),
                new EmployesDTO(107, "Fatima Zahra"),
                new EmployesDTO(108, "Thomas Dubois"),
                new EmployesDTO(109, "Sophie Laurent"),
                new EmployesDTO(110, "Carlos Mendez"),
                new EmployesDTO(111, "Yuki Tanaka"),
                new EmployesDTO(112, "Olga Ivanova"),
                new EmployesDTO(113, "James Wilson"),
                new EmployesDTO(114, "Linda Brown"),
                new EmployesDTO(115, "Robert Garcia"),
                new EmployesDTO(116, "Maria Gonzalez"),
                new EmployesDTO(117, "Jean Dupont"),
                new EmployesDTO(118, "Anna Kowalski"),
                new EmployesDTO(119, "Mohammed Al-Farsi"),
                new EmployesDTO(120, "Elena Petrova")
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
                    hbox.setPadding(new Insets(10, 12, 10, 12));
                    String bgColor = (getIndex() % 2 == 0)
                            ? "linear-gradient(to right, #e6f0ff, #ffffff)"
                            : "linear-gradient(to right, #ffffff, #f5faff)";
                    hbox.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,100,200,0.1), 5, 0, 0, 2);");

                    Label idLabel = createStyledLabel(String.valueOf(project.getProjectId()), 100, true);
                    idLabel.setTextFill(Color.web("#0B63CE"));

                    Label nameLabel = createStyledLabel(project.getName(), 250, false);
                    nameLabel.setFont(Font.font("Segoe UI", 13));

                    Label startLabel = createStyledLabel(project.getStartDate() != null ? project.getStartDate().toString() : "", 120, false);
                    Label endLabel = createStyledLabel(project.getEndDate() != null ? project.getEndDate().toString() : "", 120, false);
                    Label statusLabel = createStatusBadge(project.getStatus(), 140);

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
                    hbox.setPadding(new Insets(10, 12, 10, 12));
                    String bgColor = (getIndex() % 2 == 0)
                            ? "linear-gradient(to right, #e0f7e8, #ffffff)"
                            : "linear-gradient(to right, #ffffff, #f0faf0)";
                    hbox.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,150,0,0.1), 5, 0, 0, 2);");

                    Label idLabel = createStyledLabel(String.valueOf(assignment.getIdAssignment()), 110, true);
                    idLabel.setTextFill(Color.web("#0FA36B"));

                    String empName = employeeNameMap.getOrDefault(assignment.getEmployeeId(), "Emp " + assignment.getEmployeeId());
                    Label empLabel = createStyledLabel(empName, 160, false);
                    Label roleLabel = createStyledLabel(assignment.getRole(), 170, false);
                    Label allocLabel = createStyledLabel(assignment.getAllocationRate() + "%", 100, false);
                    Label startLabel = createStyledLabel(assignment.getAssignedFrom() != null ? assignment.getAssignedFrom().toString() : "", 130, false);
                    Label endLabel = createStyledLabel(assignment.getAssignedTo() != null ? assignment.getAssignedTo().toString() : "", 130, false);

                    hbox.getChildren().addAll(idLabel, empLabel, roleLabel, allocLabel, startLabel, endLabel);
                    setGraphic(hbox);
                }
            }
        });
    }

    private Label createStyledLabel(String text, double width, boolean bold) {
        Label label = new Label(text);
        label.setPrefWidth(width);
        label.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 13;" + (bold ? "-fx-font-weight: bold;" : ""));
        label.setTextFill(Color.web("#2c3e50"));
        return label;
    }

    private Label createStatusBadge(String status, double width) {
        Label badge = new Label(status);
        badge.setPrefWidth(width);
        badge.setAlignment(Pos.CENTER);
        badge.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 4 10;");
        badge.setTextFill(Color.WHITE);

        switch (status) {
            case "PLANNING": badge.setStyle(badge.getStyle() + "-fx-background-color: #2563eb;"); break;
            case "IN PROGRESS": badge.setStyle(badge.getStyle() + "-fx-background-color: #f59e0b;"); break;
            case "ACTIVE": badge.setStyle(badge.getStyle() + "-fx-background-color: #10b981;"); break;
            case "ON HOLD": badge.setStyle(badge.getStyle() + "-fx-background-color: #ef4444;"); break;
            case "COMPLETED": badge.setStyle(badge.getStyle() + "-fx-background-color: #6b7280;"); break;
            default: badge.setStyle(badge.getStyle() + "-fx-background-color: #6b7280;");
        }
        return badge;
    }

    private void setupSearch() {
        filteredProjects = new FilteredList<>(projectData, p -> true);
        projectsListView.setItems(filteredProjects);
        searchProjectField.textProperty().addListener((obs, oldVal, newVal) -> updateProjectFilter());
        statusFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> updateProjectFilter());

        filteredAssignments = new FilteredList<>(assignmentData, a -> true);
        assignmentsListView.setItems(filteredAssignments);
        searchAssignmentField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredAssignments.setPredicate(createAssignmentPredicate(newVal));
        });
    }

    private void updateProjectFilter() {
        String searchText = searchProjectField.getText();
        String selectedStatus = statusFilterCombo.getValue();

        filteredProjects.setPredicate(project -> {
            boolean textMatch = true;
            if (searchText != null && !searchText.trim().isEmpty()) {
                String lowerSearch = searchText.toLowerCase().trim();
                textMatch = String.valueOf(project.getProjectId()).contains(lowerSearch) ||
                        project.getName().toLowerCase().contains(lowerSearch);
            }
            boolean statusMatch = true;
            if (selectedStatus != null && !"All statuses".equals(selectedStatus)) {
                statusMatch = selectedStatus.equals(project.getStatus());
            }
            return textMatch && statusMatch;
        });
    }

    private Predicate<ProjectAssignment> createAssignmentPredicate(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) return a -> true;
        String lower = searchText.toLowerCase().trim();
        return a -> String.valueOf(a.getIdAssignment()).contains(lower) ||
                a.getRole().toLowerCase().contains(lower) ||
                employeeNameMap.getOrDefault(a.getEmployeeId(), "").toLowerCase().contains(lower);
    }

    private void loadProjects() {
        List<Project> projects = projectService.getAll();
        projectData.setAll(projects);
        if (!projectData.isEmpty()) projectsListView.getSelectionModel().selectFirst();
    }

    private void loadAssignmentsForProject(int projectId) {
        List<ProjectAssignment> assignments = assignmentService.getByProjectId(projectId);
        assignmentData.setAll(assignments);
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
            if (selected != null) openProjectForm(selected);
            else showAlert("No selection", "Please select a project to edit.");
        });
        deleteProjectButton.setOnAction(e -> deleteSelectedProject());

        assignEmployeeButton.setOnAction(e -> openAssignmentForm(projectsListView.getSelectionModel().getSelectedItem(), null));
        updateAssignmentButton.setOnAction(e -> {
            ProjectAssignment selected = assignmentsListView.getSelectionModel().getSelectedItem();
            if (selected != null) openAssignmentForm(selected.getProject(), selected);
            else showAlert("No selection", "Please select an assignment to update.");
        });
        removeAssignmentButton.setOnAction(e -> deleteSelectedAssignment());

        // PDF Export
        exportAllPdfButton.setOnAction(e -> exportAllToPdf());
        exportProjectPdfButton.setOnAction(e -> exportSelectedProjectToPdf());

        // EXCEL Export
        exportAllExcelButton.setOnAction(e -> exportAllToExcel());
        exportProjectExcelButton.setOnAction(e -> exportSelectedProjectToExcel());

        // AI Assistant (Ollama)
        aiAssistantButton.setOnAction(e -> openAIAssistant());

        // AI Recommendation (HuggingFace)
        aiRecommendButton.setOnAction(e -> openAIRecommendation());
    }

    // ==================== PDF EXPORT (inchangé) ====================
    private void exportAllToPdf() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer le PDF");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
            fileChooser.setInitialFileName("tous_les_projets.pdf");
            File file = fileChooser.showSaveDialog(projectsListView.getScene().getWindow());
            if (file == null) return;

            List<ProjectAssignment> allAssignments = assignmentService.getAll();
            String logoPath = getClass().getResource("/images/logo.png").toExternalForm();
            PdfExportService pdfService = new PdfExportService();
            pdfService.exportAllProjects(projectData, allAssignments, file.getAbsolutePath(), logoPath);
            showAlert("Succès", "PDF généré : " + file.getName());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de générer le PDF : " + e.getMessage());
        }
    }

    private void exportSelectedProjectToPdf() {
        Project selected = projectsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Aucun projet", "Veuillez sélectionner un projet.");
            return;
        }
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer le PDF");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
            fileChooser.setInitialFileName("projet_" + selected.getProjectId() + ".pdf");
            File file = fileChooser.showSaveDialog(projectsListView.getScene().getWindow());
            if (file == null) return;

            List<ProjectAssignment> assignments = assignmentService.getByProjectId(selected.getProjectId());
            String logoPath = getClass().getResource("/images/logo.png").toExternalForm();
            PdfExportService pdfService = new PdfExportService();
            pdfService.exportSingleProject(selected, assignments, file.getAbsolutePath(), logoPath);
            showAlert("Succès", "PDF généré : " + file.getName());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de générer le PDF : " + e.getMessage());
        }
    }

    // ==================== EXCEL EXPORT ====================
    private void exportAllToExcel() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer le fichier Excel");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers Excel", "*.xlsx"));
            fileChooser.setInitialFileName("rapport_complet.xlsx");
            File file = fileChooser.showSaveDialog(projectsListView.getScene().getWindow());
            if (file == null) return;

            List<ProjectAssignment> allAssignments = assignmentService.getAll();
            ExcelExportService excelService = new ExcelExportService();
            excelService.exportAllData(projectData, allAssignments, employeeList, file.getAbsolutePath());
            showAlert("Succès", "Excel généré : " + file.getName());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de générer le fichier Excel : " + e.getMessage());
        }
    }

    private void exportSelectedProjectToExcel() {
        Project selected = projectsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Aucun projet", "Veuillez sélectionner un projet.");
            return;
        }
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer le fichier Excel");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers Excel", "*.xlsx"));
            fileChooser.setInitialFileName("projet_" + selected.getProjectId() + ".xlsx");
            File file = fileChooser.showSaveDialog(projectsListView.getScene().getWindow());
            if (file == null) return;

            List<ProjectAssignment> assignments = assignmentService.getByProjectId(selected.getProjectId());
            ExcelExportService excelService = new ExcelExportService();
            excelService.exportSingleProject(selected, assignments, employeeList, file.getAbsolutePath());
            showAlert("Succès", "Excel généré : " + file.getName());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de générer le fichier Excel : " + e.getMessage());
        }
    }

    // ==================== AI Assistant (Ollama) ====================
    private void openAIAssistant() {
        Project selected = projectsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Aucun projet", "Veuillez sélectionner un projet.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AIAssistant.fxml"));
            Parent root = loader.load();
            AIAssistantController controller = loader.getController();
            controller.setProject(selected);
            controller.setEmployeeMap(employeeNameMap);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Assistant IA - " + selected.getName());
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir l'assistant IA.");
        }
    }

    // ==================== AI Recommendation (HuggingFace) ====================
    private void openAIRecommendation() {
        Project selected = projectsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Aucun projet", "Veuillez sélectionner un projet.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AIRecommendation.fxml"));
            Parent root = loader.load();
            AIRecommendationController controller = loader.getController();
            controller.setProject(selected);
            controller.setEmployeeList(new java.util.ArrayList<>(employeeList));
            controller.setEmployeeMap(employeeNameMap);
            controller.setOnAssignCallback(this::refreshAfterSave);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("🤖 Recommandation IA - " + selected.getName());
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la recommandation IA.");
        }
    }

    // ==================== Méthodes métier communes ====================
    private void showProjectDetails(Project p) {
        detailName.setText(p.getName());
        detailDescription.setText(p.getDescription());
        detailStart.setText(p.getStartDate() != null ? p.getStartDate().toString() : "");
        detailEnd.setText(p.getEndDate() != null ? p.getEndDate().toString() : "");
        detailStatus.setText(p.getStatus());

        originalBudgetTND = p.getBudget();
        currentCurrency = "TND";
        detailBudget.setText(String.format("%.2f", originalBudgetTND));
        detailBudgetCurrency.setText("TND");
    }

    private void toggleBudgetCurrency() {
        if (originalBudgetTND == 0) return;
        try {
            String nextCurrency = switch (currentCurrency) {
                case "TND" -> "USD";
                case "USD" -> "EUR";
                default -> "TND";
            };
            double converted = currencyService.convert(originalBudgetTND, "TND", nextCurrency);
            detailBudget.setText(String.format("%.2f", converted));
            detailBudgetCurrency.setText(nextCurrency);
            currentCurrency = nextCurrency;
        } catch (Exception e) {
            showAlert("Currency Error", "Impossible de convertir le budget : " + e.getMessage());
        }
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
                for (ProjectAssignment a : assignments) assignmentService.delete(a.getIdAssignment());
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
                if (project != null) loadAssignmentsForProject(project.getProjectId());
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
        if (selected != null) loadAssignmentsForProject(selected.getProjectId());
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