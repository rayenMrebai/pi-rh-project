package org.example.Controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.model.formation.Skill;
import org.example.model.formation.TrainingProgram;
import org.example.services.formation.EscoApiService;
import org.example.services.formation.EscoApiService.EscoSkill;
import org.example.services.formation.SkillService;
import org.example.services.formation.TrainingProgramService;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AddFormSkillController implements Initializable {

    // Champs formulaire
    @FXML private TextField nomField;
    @FXML private TextArea descriptionField;
    @FXML private ComboBox<String> levelCombo;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private ComboBox<TrainingProgram> trainingCombo;
    @FXML private Label errorLabel;

    // Champs ESCO
    @FXML private TextField escoSearchField;
    @FXML private ListView<EscoSkill> escoResultsList;
    @FXML private Label escoStatusLabel;

    private SkillService skillService;
    private TrainingProgramService trainingService;
    private EscoApiService escoService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        skillService   = new SkillService();
        trainingService = new TrainingProgramService();
        escoService    = new EscoApiService();

        levelCombo.setItems(FXCollections.observableArrayList(
                "1 - BASE", "2 - DÉBUTANT", "3 - INTERMÉDIAIRE", "4 - AVANCÉ", "5 - EXPERT"));
        categoryCombo.setItems(FXCollections.observableArrayList("technique", "soft"));

        loadTrainings();
        setupEscoList();
    }

    // ===== SETUP LISTE ESCO =====
    private void setupEscoList() {
        escoResultsList.setCellFactory(lv -> new ListCell<EscoSkill>() {
            @Override
            protected void updateItem(EscoSkill item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("📌 " + item.getTitle() + "  [" + item.getSkillType() + "]");
                }
            }
        });

        // ✅ Clic sur un résultat → remplir le formulaire automatiquement
        escoResultsList.getSelectionModel().selectedItemProperty().addListener(
                (obs, o, selected) -> {
                    if (selected != null) {
                        nomField.setText(selected.getTitle());
                        if (selected.getDescription() != null && !selected.getDescription().isEmpty()) {
                            descriptionField.setText(selected.getDescription());
                        }
                        // Détecter catégorie automatiquement
                        String type = selected.getSkillType().toLowerCase();
                        if (type.contains("knowledge") || type.contains("skill")) {
                            categoryCombo.setValue("technique");
                        } else {
                            categoryCombo.setValue("soft");
                        }
                        escoStatusLabel.setText("✅ Compétence importée depuis ESCO");
                        escoStatusLabel.setStyle("-fx-text-fill: #43a047; -fx-font-size: 10px;");
                    }
                });

        // ✅ Recherche automatique quand on tape dans escoSearchField
        escoSearchField.textProperty().addListener((obs, old, nv) -> {
            if (nv.length() >= 3) {
                handleEscoSearch();
            }
        });
    }

    // ===== RECHERCHE ESCO =====
    @FXML
    private void handleEscoSearch() {
        String keyword = escoSearchField.getText().trim();
        if (keyword.isEmpty()) return;

        escoStatusLabel.setText("⏳ Recherche en cours...");
        escoStatusLabel.setStyle("-fx-text-fill: #1976d2; -fx-font-size: 10px;");
        escoResultsList.setItems(FXCollections.observableArrayList());

        // ✅ Thread séparé pour ne pas bloquer l'UI
        Thread thread = new Thread(() -> {
            try {
                // Essayer français d'abord
                List<EscoSkill> results = escoService.searchSkillsFr(keyword);

                // Si vide, essayer anglais
                if (results.isEmpty()) {
                    results = escoService.searchSkillsEn(keyword);
                }

                final List<EscoSkill> finalResults = results;

                Platform.runLater(() -> {
                    escoResultsList.setItems(FXCollections.observableArrayList(finalResults));

                    if (finalResults.isEmpty()) {
                        escoStatusLabel.setText("⚠️ Aucun résultat — vérifiez votre connexion internet");
                        escoStatusLabel.setStyle("-fx-text-fill: #f57c00; -fx-font-size: 10px;");
                    } else {
                        escoStatusLabel.setText("✅ " + finalResults.size() + " compétence(s) trouvée(s) — cliquez pour importer");
                        escoStatusLabel.setStyle("-fx-text-fill: #43a047; -fx-font-size: 10px;");
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    escoStatusLabel.setText("❌ Erreur réseau : " + e.getMessage());
                    escoStatusLabel.setStyle("-fx-text-fill: #e53935; -fx-font-size: 10px;");
                });
            }
        });

        thread.setDaemon(true);
        thread.start();
    }

    // ===== CHARGER FORMATIONS =====
    private void loadTrainings() {
        List<TrainingProgram> trainings = trainingService.getAll();

        trainingCombo.setCellFactory(lv -> new ListCell<TrainingProgram>() {
            @Override
            protected void updateItem(TrainingProgram item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getTitle() + " [" + item.getType() + "]");
            }
        });
        trainingCombo.setButtonCell(new ListCell<TrainingProgram>() {
            @Override
            protected void updateItem(TrainingProgram item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Sélectionner une formation..." :
                        item.getTitle() + " [" + item.getType() + "]");
            }
        });
        trainingCombo.setItems(FXCollections.observableArrayList(trainings));
    }

    // ===== ENREGISTRER =====
    @FXML
    private void handleSave() {
        errorLabel.setText("");

        String nom  = nomField.getText().trim();
        String desc = descriptionField.getText().trim();
        String lvl  = levelCombo.getValue();
        String cat  = categoryCombo.getValue();

        if (nom.isEmpty() || desc.isEmpty() || lvl == null || cat == null) {
            errorLabel.setText("⚠️ Veuillez remplir tous les champs obligatoires.");
            return;
        }

        try {
            Skill skill = new Skill();
            skill.setNom(nom);
            skill.setDescription(desc);
            skill.setLevelRequired(Integer.parseInt(lvl.substring(0, 1)));
            skill.setCategorie(cat);

            TrainingProgram selectedTraining = trainingCombo.getValue();
            if (selectedTraining != null) {
                skill.setTrainingProgramId(selectedTraining.getId());
            }

            skillService.create(skill);

            new Alert(Alert.AlertType.INFORMATION,
                    "✅ Compétence '" + nom + "' créée !" +
                            (selectedTraining != null ? "\n🔗 Associée à '" + selectedTraining.getTitle() + "'" : ""))
                    .showAndWait();

            ((Stage) nomField.getScene().getWindow()).close();

        } catch (Exception e) {
            errorLabel.setText("❌ Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        ((Stage) nomField.getScene().getWindow()).close();
    }
}