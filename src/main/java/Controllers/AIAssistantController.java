package Controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.Services.projet.ProjectService;
import org.example.model.projet.Project;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.ResourceBundle;

public class AIAssistantController implements Initializable {

    @FXML private Label projectNameLabel;
    @FXML private TextArea summaryArea;
    @FXML private TextArea improvedDescriptionArea;
    @FXML private Button generateSummaryButton;
    @FXML private Button copySummaryButton;
    @FXML private Button translateButton;
    @FXML private Button improveButton;
    @FXML private Button replaceDescriptionButton;
    @FXML private Button closeButton;

    private Project currentProject;
    private Map<Integer, String> employeeNameMap; // optionnel
    private final ProjectService projectService = new ProjectService();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String OLLAMA_URL = "http://localhost:11434/api/generate";
    private static final String MODEL = "mistral";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialisation des actions
        generateSummaryButton.setOnAction(e -> generateSummary());
        copySummaryButton.setOnAction(e -> copyToClipboard(summaryArea.getText()));
        translateButton.setOnAction(e -> translateSummary());
        improveButton.setOnAction(e -> improveDescription());
        replaceDescriptionButton.setOnAction(e -> replaceDescription());
        closeButton.setOnAction(e -> closeWindow());
    }

    public void setProject(Project project) {
        this.currentProject = project;
        projectNameLabel.setText("Projet : " + project.getName());
        improvedDescriptionArea.setText(project.getDescription());
    }

    public void setEmployeeMap(Map<Integer, String> map) {
        this.employeeNameMap = map;
    }

    // ========== Appels IA ==========

    private void generateSummary() {
        String prompt = buildSummaryPrompt();
        callOllama(prompt, "Génération du résumé...", 30, response -> {
            summaryArea.setText(response);
        });
    }

    private void translateSummary() {
        String text = summaryArea.getText();
        if (text == null || text.isEmpty()) {
            showAlert("Rien à traduire", "Veuillez d'abord générer ou écrire un résumé.");
            return;
        }
        String prompt = "Traduis le texte suivant en anglais :\n" + text;
        callOllama(prompt, "Traduction en cours...", 30, response -> {
            summaryArea.setText(response);
        });
    }

    private void improveDescription() {
        String original = currentProject.getDescription();
        if (original == null || original.isEmpty()) {
            original = "Aucune description.";
        }
        String prompt = "Rédige une description de projet professionnelle et concise, en un seul paragraphe, en français, à partir de la description suivante :\n" + original;
        callOllama(prompt, "Amélioration en cours (cela peut prendre jusqu'à 60 secondes)...", 60, response -> {
            improvedDescriptionArea.setText(response);
        });
    }

    private void replaceDescription() {
        String newDesc = improvedDescriptionArea.getText();
        if (newDesc == null || newDesc.isEmpty()) {
            showAlert("Description vide", "La zone de description améliorée est vide.");
            return;
        }
        currentProject.setDescription(newDesc);
        projectService.update(currentProject);
        showAlert("Succès", "La description du projet a été mise à jour.");
    }

    // ========== Appel générique Ollama ==========

    private void callOllama(String prompt, String loadingMessage, int timeoutSeconds, java.util.function.Consumer<String> onSuccess) {
        setButtonsDisabled(true);
        System.out.println(loadingMessage);

        new Thread(() -> {
            try {
                String requestBody = objectMapper.writeValueAsString(
                        Map.of("model", MODEL, "prompt", prompt, "stream", false)
                );
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(OLLAMA_URL))
                        .timeout(Duration.ofSeconds(timeoutSeconds))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    JsonNode root = objectMapper.readTree(response.body());
                    String result = root.path("response").asText("");
                    Platform.runLater(() -> {
                        onSuccess.accept(result);
                        setButtonsDisabled(false);
                    });
                } else {
                    Platform.runLater(() -> {
                        showAlert("Erreur IA", "Code HTTP : " + response.statusCode());
                        setButtonsDisabled(false);
                    });
                }
            } catch (java.net.http.HttpTimeoutException e) {
                Platform.runLater(() -> {
                    showAlert("Erreur IA", "Timeout après " + timeoutSeconds + " secondes.\n" +
                            "Le serveur Ollama met trop de temps à répondre. Réessayez plus tard ou utilisez un modèle plus petit.");
                    setButtonsDisabled(false);
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showAlert("Erreur IA", "Impossible de contacter Ollama : " + e.getMessage());
                    setButtonsDisabled(false);
                });
            }
        }).start();
    }

    private String buildSummaryPrompt() {
        return String.format("""
                Rédige un résumé en une seule phrase du projet suivant (en français) :
                Nom : %s
                Description : %s
                Dates : du %s au %s
                Statut : %s
                Budget : %.2f TND
                """,
                currentProject.getName(),
                currentProject.getDescription() != null ? currentProject.getDescription() : "",
                currentProject.getStartDate(),
                currentProject.getEndDate(),
                currentProject.getStatus(),
                currentProject.getBudget()
        );
    }

    private void copyToClipboard(String text) {
        if (text == null || text.isEmpty()) return;
        StringSelection selection = new StringSelection(text);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
        showAlert("Copié", "Texte copié dans le presse-papier.");
    }

    private void setButtonsDisabled(boolean disabled) {
        generateSummaryButton.setDisable(disabled);
        translateButton.setDisable(disabled);
        improveButton.setDisable(disabled);
        replaceDescriptionButton.setDisable(disabled);
        copySummaryButton.setDisable(disabled);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}