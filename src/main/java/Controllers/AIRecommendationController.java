package Controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.example.Services.ai.EmployeeMatchingService;
import org.example.Services.projet.ProjectAssignmentService;
import org.example.model.projet.EmployesDTO;
import org.example.model.projet.MatchResult;
import org.example.model.projet.Project;
import org.example.model.projet.ProjectAssignment;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class AIRecommendationController implements Initializable {

    // HEADER
    @FXML private Label projectNameLabel;
    @FXML private Label statusBadge;

    // PODIUM
    @FXML private Label name1, role1, score1, alloc1;
    @FXML private Label name2, role2, score2, alloc2;
    @FXML private Label name3, role3, score3, alloc3;
    @FXML private Button assign1, assign2, assign3;

    // TABLEAU
    @FXML private ListView<MatchResult> resultsListView;
    @FXML private Button analyzeButton;
    @FXML private Label countLabel;
    @FXML private Label footerLabel;
    @FXML private ProgressBar progressBar;

    private Project project;
    private List<EmployesDTO> employees;
    private Map<Integer, String> employeeNameMap;
    private Runnable onAssignCallback;

    private final EmployeeMatchingService matchingService = new EmployeeMatchingService();
    private final ProjectAssignmentService assignmentService = new ProjectAssignmentService(); // Ajout
    private final ObservableList<MatchResult> resultData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        resultsListView.setItems(resultData);
        setupListViewCellFactory();
        analyzeButton.setOnAction(e -> runAnalysis());
    }

    public void setProject(Project project) {
        this.project = project;
        projectNameLabel.setText("Project: " + project.getName());
    }

    public void setEmployeeList(List<EmployesDTO> employees) {
        this.employees = employees;
    }

    public void setEmployeeMap(Map<Integer, String> map) {
        this.employeeNameMap = map;
    }

    public void setOnAssignCallback(Runnable callback) {
        this.onAssignCallback = callback;
    }

    private void runAnalysis() {
        if (project == null || employees == null || employees.isEmpty()) {
            footerLabel.setText("⚠️ Données insuffisantes pour l'analyse.");
            return;
        }

        analyzeButton.setDisable(true);
        progressBar.setProgress(-1);
        statusBadge.setText("⏳ ANALYSE...");
        statusBadge.setStyle("-fx-text-fill: #fbbf24; -fx-font-size: 12; -fx-font-weight: bold;");
        footerLabel.setText("🔍 Calcul des embeddings en cours via HuggingFace...");
        resultData.clear();
        clearPodium();

        new Thread(() -> {
            try {
                List<MatchResult> results = matchingService.rankEmployeesForProject(project, employees);

                Platform.runLater(() -> {
                    resultData.setAll(results);
                    countLabel.setText(results.size() + " candidat(s)");
                    updatePodium(results);
                    progressBar.setProgress(1.0);
                    analyzeButton.setDisable(false);
                    statusBadge.setText("✅ TERMINÉ");
                    statusBadge.setStyle("-fx-text-fill: #34d399; -fx-font-size: 12; -fx-font-weight: bold;");
                    footerLabel.setText("✅ Analyse terminée — " + results.size() + " employé(s) analysé(s).");
                });

            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> {
                    progressBar.setProgress(0);
                    analyzeButton.setDisable(false);
                    statusBadge.setText("❌ ERREUR");
                    statusBadge.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 12; -fx-font-weight: bold;");
                    footerLabel.setText("❌ Erreur : " + ex.getMessage());
                    showError("Erreur API", ex.getMessage());
                });
            }
        }).start();
    }

    private void updatePodium(List<MatchResult> results) {
        if (results.size() >= 1) fillCard(results.get(0), name1, role1, score1, alloc1, assign1);
        if (results.size() >= 2) fillCard(results.get(1), name2, role2, score2, alloc2, assign2);
        if (results.size() >= 3) fillCard(results.get(2), name3, role3, score3, alloc3, assign3);
    }

    private void fillCard(MatchResult r, Label name, Label role, Label score,
                          Label alloc, Button btn) {
        name.setText(r.getEmployeeName());
        role.setText(r.getRole());
        score.setText(String.format("%.0f%%", r.getFinalScore() * 100));
        alloc.setText("Alloc actuelle : " + String.format("%.0f%%", r.getAllocationRate()));
        btn.setOnAction(e -> assignEmployee(r));
    }

    private void clearPodium() {
        for (Label l : new Label[]{name1, role1, score1, alloc1,
                name2, role2, score2, alloc2,
                name3, role3, score3, alloc3}) {
            l.setText("—");
        }
    }

    private void setupListViewCellFactory() {
        resultsListView.setCellFactory(lv -> new ListCell<MatchResult>() {
            @Override
            protected void updateItem(MatchResult r, boolean empty) {
                super.updateItem(r, empty);
                if (empty || r == null) {
                    setGraphic(null);
                    return;
                }

                HBox row = new HBox(0);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(9, 14, 9, 14));

                String bg = (getIndex() % 2 == 0) ? "#f8fafc" : "white";
                row.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 8;");

                int rank = getIndex() + 1;
                String rankText = rank == 1 ? "🥇" : rank == 2 ? "🥈" : rank == 3 ? "🥉" : "#" + rank;
                Label rankLabel = styledLabel(rankText, 60, true);
                Label nameLabel = styledLabel(r.getEmployeeName(), 170, false);
                Label roleLabel = styledLabel(r.getRole(), 160, false);

                double sim = r.getSimilarityScore() * 100;
                Label simLabel = styledLabel(String.format("%.0f%%", sim), 90, true);
                simLabel.setTextFill(sim >= 80 ? Color.web("#10b981")
                        : sim >= 65 ? Color.web("#f59e0b") : Color.web("#ef4444"));

                double dispo = 100 - r.getAllocationRate();
                Label dispoLabel = styledLabel(String.format("%.0f%%", dispo), 80, false);

                double finalPct = r.getFinalScore() * 100;
                Label finalLabel = styledLabel(String.format("%.0f%%", finalPct), 100, true);
                finalLabel.setTextFill(Color.web("#0B63CE"));

                Label suggestLabel = styledLabel(r.getSuggestedAllocation(), 110, false);
                suggestLabel.setStyle(suggestLabel.getStyle()
                        + "-fx-background-color: #dbeafe; -fx-background-radius: 12; -fx-padding: 2 8;");
                suggestLabel.setTextFill(Color.web("#1d4ed8"));

                Button btn = new Button("Affecter");
                btn.setPrefWidth(90);
                btn.setStyle("-fx-background-color: #0B63CE; -fx-text-fill: white; "
                        + "-fx-font-weight: bold; -fx-background-radius: 20; "
                        + "-fx-padding: 5 12; -fx-cursor: hand; -fx-font-size: 11;");
                btn.setOnAction(e -> assignEmployee(r));

                row.getChildren().addAll(rankLabel, nameLabel, roleLabel,
                        simLabel, dispoLabel, finalLabel, suggestLabel, btn);
                setGraphic(row);
            }
        });
    }

    private Label styledLabel(String text, double width, boolean bold) {
        Label l = new Label(text);
        l.setPrefWidth(width);
        l.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 13;"
                + (bold ? "-fx-font-weight: bold;" : ""));
        l.setTextFill(Color.web("#2c3e50"));
        return l;
    }

    // Méthode corrigée : crée l'affectation dans la base de données
    private void assignEmployee(MatchResult r) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Affecter " + r.getEmployeeName() + " au projet " + project.getName()
                        + " ?\nAllocation suggérée : " + r.getSuggestedAllocation(),
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmation d'affectation");
        confirm.setHeaderText("Affectation IA recommandée");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    // Créer une nouvelle affectation
                    ProjectAssignment assignment = new ProjectAssignment();
                    assignment.setProject(project);
                    assignment.setEmployeeId(r.getEmployeeId());
                    assignment.setRole(r.getRole()); // rôle suggéré
                    int allocation = Integer.parseInt(r.getSuggestedAllocation().replace("%", ""));
                    assignment.setAllocationRate(allocation);
                    assignment.setAssignedFrom(LocalDate.now());
                    // Date de fin : celle du projet, si null on met un an plus tard
                    if (project.getEndDate() != null) {
                        assignment.setAssignedTo(project.getEndDate());
                    } else {
                        assignment.setAssignedTo(LocalDate.now().plusYears(1));
                    }
                    assignmentService.create(assignment);

                    footerLabel.setText("✅ " + r.getEmployeeName()
                            + " affecté(e) avec succès (allocation : " + r.getSuggestedAllocation() + ")");
                    if (onAssignCallback != null) onAssignCallback.run();
                } catch (Exception e) {
                    e.printStackTrace();
                    showError("Erreur", "Impossible de créer l'affectation : " + e.getMessage());
                }
            }
        });
    }

    private void showError(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}