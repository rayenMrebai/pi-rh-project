package org.example.Controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.model.formation.Skill;
import org.example.model.formation.TrainingProgram;
import org.example.services.CourseraApiService;
import org.example.services.CourseraApiService.CourseraCourse;
import org.example.services.SkillService;
import org.example.services.TrainingProgramService;

import java.net.URL;
import java.sql.Date;
import java.util.List;
import java.util.ResourceBundle;

public class AddFormTrainingProgramController implements Initializable {

    // Champs formulaire
    @FXML private TextField titleField;
    @FXML private TextArea  descriptionField;
    @FXML private Spinner<Integer> durationSpinner;
    @FXML private ComboBox<String> typeCombo;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<Skill> skillCombo;
    @FXML private Label errorLabel;

    // Champs Coursera
    @FXML private TextField courseraSearchField;
    @FXML private ListView<CourseraCourse> courseraResultsList;
    @FXML private Label courseraStatusLabel;

    private TrainingProgramService trainingService;
    private SkillService skillService;
    private CourseraApiService courseraService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        trainingService = new TrainingProgramService();
        skillService    = new SkillService();
        courseraService = new CourseraApiService();

        typeCombo.setItems(FXCollections.observableArrayList(
                "en ligne", "présentiel", "hybride", "workshop"));

        durationSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 52, 4));

        loadSkills();
        setupCourseraList();
    }

    // ===== SETUP LISTE COURSERA =====
    private void setupCourseraList() {
        courseraResultsList.setCellFactory(lv -> new ListCell<CourseraCourse>() {
            @Override
            protected void updateItem(CourseraCourse item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("🎓 " + item.getTitle()
                            + (item.getPartner().isEmpty() ? "" : "  ·  " + item.getPartner())
                            + "  [" + item.getDifficulty() + "]");
                }
            }
        });

        // ✅ Clic → remplir formulaire automatiquement
        courseraResultsList.getSelectionModel().selectedItemProperty().addListener(
                (obs, o, selected) -> {
                    if (selected != null) {
                        titleField.setText(selected.getTitle());

                        String desc = selected.getDescription();
                        if (desc != null && !desc.isEmpty()) {
                            descriptionField.setText(desc);
                        }

                        // ✅ Détecter durée automatiquement
                        String dur = selected.getDuration().toLowerCase();
                        int weeks = extractWeeks(dur);
                        durationSpinner.getValueFactory().setValue(weeks);

                        // ✅ Détecter type automatiquement
                        typeCombo.setValue("en ligne");

                        courseraStatusLabel.setText("✅ Formation importée : " + selected.getTitle());
                        courseraStatusLabel.setStyle("-fx-text-fill: #2e7d32; -fx-font-size: 10px;");
                    }
                });

        // ✅ Recherche auto après 3 caractères
        courseraSearchField.textProperty().addListener((obs, old, nv) -> {
            if (nv.length() >= 3) {
                handleCourseraSearch();
            }
        });
    }

    // ===== RECHERCHE COURSERA =====
    @FXML
    private void handleCourseraSearch() {
        String keyword = courseraSearchField.getText().trim();
        if (keyword.isEmpty()) return;

        courseraStatusLabel.setText("⏳ Recherche Coursera en cours...");
        courseraStatusLabel.setStyle("-fx-text-fill: #1976d2; -fx-font-size: 10px;");
        courseraResultsList.setItems(FXCollections.observableArrayList());

        Thread thread = new Thread(() -> {
            List<CourseraCourse> results = courseraService.searchCourses(keyword);

            Platform.runLater(() -> {
                courseraResultsList.setItems(FXCollections.observableArrayList(results));
                if (results.isEmpty()) {
                    courseraStatusLabel.setText("⚠️ Aucun résultat trouvé pour : " + keyword);
                    courseraStatusLabel.setStyle("-fx-text-fill: #f57c00; -fx-font-size: 10px;");
                } else {
                    courseraStatusLabel.setText("✅ " + results.size()
                            + " formation(s) — cliquez pour importer");
                    courseraStatusLabel.setStyle("-fx-text-fill: #2e7d32; -fx-font-size: 10px;");
                }
            });
        });
        thread.setDaemon(true);
        thread.start();
    }

    // ===== CHARGER SKILLS =====
    private void loadSkills() {
        skillCombo.setCellFactory(lv -> new ListCell<Skill>() {
            @Override
            protected void updateItem(Skill item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null :
                        item.getNom() + " [" + item.getCategorie() + "]");
            }
        });
        skillCombo.setButtonCell(new ListCell<Skill>() {
            @Override
            protected void updateItem(Skill item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Sélectionner une compétence..." :
                        item.getNom() + " [" + item.getCategorie() + "]");
            }
        });
        skillCombo.setItems(FXCollections.observableArrayList(skillService.getAll()));
    }

    // ===== ENREGISTRER =====
    @FXML
    private void handleSave() {
        errorLabel.setText("");

        String title = titleField.getText().trim();
        String desc  = descriptionField.getText().trim();
        String type  = typeCombo.getValue();
        var start = startDatePicker.getValue();
        var end   = endDatePicker.getValue();

        if (title.isEmpty() || desc.isEmpty() || type == null || start == null || end == null) {
            errorLabel.setText("⚠️ Veuillez remplir tous les champs obligatoires.");
            return;
        }
        if (end.isBefore(start)) {
            errorLabel.setText("⚠️ La date de fin doit être après la date de début.");
            return;
        }

        try {
            TrainingProgram p = new TrainingProgram();
            p.setTitle(title);
            p.setDescription(desc);
            p.setDuration(durationSpinner.getValue());
            p.setType(type);
            p.setStartDate(Date.valueOf(start));
            p.setEndDate(Date.valueOf(end));

            trainingService.create(p);

            Skill selectedSkill = skillCombo.getValue();
            if (selectedSkill != null && p.getId() > 0) {
                skillService.assignToTraining(selectedSkill.getId(), p.getId());
            }

            new Alert(Alert.AlertType.INFORMATION,
                    "✅ Programme '" + title + "' créé avec succès !" +
                            (selectedSkill != null ?
                                    "\n🔗 Skill '" + selectedSkill.getNom() + "' associé." : ""))
                    .showAndWait();

            ((Stage) titleField.getScene().getWindow()).close();

        } catch (Exception e) {
            errorLabel.setText("❌ Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        ((Stage) titleField.getScene().getWindow()).close();
    }

    // ===== HELPER — extraire semaines =====
    private int extractWeeks(String duration) {
        try {
            if (duration.contains("week"))  {
                String num = duration.replaceAll("[^0-9]", "").trim();
                return num.isEmpty() ? 4 : Integer.parseInt(num);
            }
            if (duration.contains("month") || duration.contains("mois")) {
                String num = duration.replaceAll("[^0-9]", "").trim();
                int months = num.isEmpty() ? 1 : Integer.parseInt(num);
                return months * 4;
            }
            if (duration.contains("hour") || duration.contains("heure")) {
                String num = duration.replaceAll("[^0-9]", "").trim();
                int hours = num.isEmpty() ? 40 : Integer.parseInt(num);
                return Math.max(1, hours / 10);
            }
        } catch (Exception ignored) {}
        return 4; // valeur par défaut
    }
}