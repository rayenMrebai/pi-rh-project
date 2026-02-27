package Controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.Services.projet.ProjectService;
import org.example.model.projet.Project;
import org.vosk.Model;
import org.vosk.Recognizer;

import javax.sound.sampled.*;
import java.io.File;
import java.net.URL;
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
    @FXML private Button micButton;

    private Project projectToEdit;
    private Runnable onSaveCallback;
    private final ProjectService projectService = new ProjectService();

    private Model voskModel;
    private TargetDataLine microphone;
    private boolean isRecording = false;
    private Thread recordingThread;

    private static final String MODEL_PATH = "models/vosk-model-fr-0.22";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        statusCombo.setItems(FXCollections.observableArrayList(
                "PLANNING", "IN PROGRESS", "ACTIVE", "ON HOLD", "COMPLETED"
        ));

        saveButton.setOnAction(e -> saveProject());
        cancelButton.setOnAction(e -> closeWindow());

        // Test initial
        Platform.runLater(() -> {
            descriptionField.setText("Test initial");
            System.out.println("Test initial effectué");
        });

        initVoskModel();
        micButton.setOnAction(e -> toggleRecording());

        setupValidationListeners();
        Platform.runLater(() -> nameField.requestFocus());
    }

    private void initVoskModel() {
        try {
            File modelDir = new File(MODEL_PATH);
            if (!modelDir.exists()) {
                showAlert("Erreur Vosk", "Le dossier du modèle n'existe pas : " + modelDir.getAbsolutePath());
                return;
            }
            voskModel = new Model(modelDir.getAbsolutePath());
            System.out.println("Modèle Vosk chargé avec succès.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur Vosk", "Impossible de charger le modèle : " + e.getMessage());
        }
    }

    private void toggleRecording() {
        if (!isRecording) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void startRecording() {
        if (voskModel == null) {
            showAlert("Erreur", "Modèle non chargé.");
            return;
        }

        try {
            AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            if (!AudioSystem.isLineSupported(info)) {
                showAlert("Erreur", "Microphone non supporté.");
                return;
            }

            microphone = (TargetDataLine) AudioSystem.getLine(info);
            microphone.open(format);
            microphone.start();

            isRecording = true;
            micButton.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold;");
            micButton.setText("⏺ Arrêter");

            Platform.runLater(() -> descriptionField.clear());

            recordingThread = new Thread(() -> {
                byte[] buffer = new byte[4096];
                StringBuilder fullText = new StringBuilder();
                try (Recognizer recognizer = new Recognizer(voskModel, 16000)) {
                    recognizer.setWords(true);
                    System.out.println("Début de l'enregistrement...");
                    while (isRecording) {
                        int bytesRead = microphone.read(buffer, 0, buffer.length);
                        if (bytesRead > 0) {
                            if (recognizer.acceptWaveForm(buffer, bytesRead)) {
                                String result = recognizer.getResult();
                                System.out.println("Résultat final (phrase) : " + result);
                                String text = extractTextFromResult(result);
                                if (!text.isEmpty()) {
                                    fullText.append(text).append(" ");
                                    System.out.println("Ajouté au fullText : " + text);
                                } else {
                                    System.out.println("Texte extrait vide pour résultat : " + result);
                                }
                            }
                        }
                    }
                    String finalResult = recognizer.getFinalResult();
                    System.out.println("Résultat final global : " + finalResult);
                    String finalText = extractTextFromResult(finalResult);
                    if (!finalText.isEmpty()) {
                        fullText.append(finalText);
                        System.out.println("Ajouté finalText : " + finalText);
                    }
                    String finalDescription = fullText.toString().trim();
                    System.out.println("Texte final accumulé : '" + finalDescription + "'");
                    Platform.runLater(() -> {
                        descriptionField.setText(finalDescription);
                        descriptionField.requestLayout();
                        System.out.println("Mise à jour UI effectuée avec : " + finalDescription);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    Platform.runLater(() -> resetMicButton());
                }
            });
            recordingThread.start();

        } catch (LineUnavailableException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'accéder au microphone : " + e.getMessage());
        }
    }

    private void stopRecording() {
        isRecording = false;
        if (microphone != null) {
            microphone.stop();
            microphone.close();
        }
    }

    private void resetMicButton() {
        micButton.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #1e293b; -fx-font-weight: bold; -fx-background-radius: 30; -fx-cursor: hand;");
        micButton.setText("🎤");
    }

    private String extractTextFromResult(String jsonResult) {
        try {
            // Essayer d'abord sans espace
            String search1 = "\"text\":\"";
            int start = jsonResult.indexOf(search1);
            if (start != -1) {
                start += search1.length();
                int end = jsonResult.indexOf("\"", start);
                return jsonResult.substring(start, end);
            }
            // Essayer avec espace
            String search2 = "\"text\" : \"";
            start = jsonResult.indexOf(search2);
            if (start != -1) {
                start += search2.length();
                int end = jsonResult.indexOf("\"", start);
                return jsonResult.substring(start, end);
            }
            return "";
        } catch (Exception e) {
            return "";
        }
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
        if (isRecording) {
            stopRecording();
        }
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}