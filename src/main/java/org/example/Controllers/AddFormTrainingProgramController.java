package org.example.Controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.model.formation.Skill;
import org.example.model.formation.TrainingProgram;
import org.example.services.SkillService;
import org.example.services.TrainingProgramService;

import java.net.URL;
import java.sql.Date;
import java.util.List;
import java.util.ResourceBundle;

public class AddFormTrainingProgramController implements Initializable {

    @FXML private TextField titleField;
    @FXML private TextArea descriptionField;
    @FXML private Spinner<Integer> durationSpinner;
    @FXML private ComboBox<String> typeCombo;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<Skill> skillCombo;  // ✅ NOUVEAU
    @FXML private Label errorLabel;

    private TrainingProgramService trainingService;
    private SkillService skillService;
    private List<Skill> allSkills;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        trainingService = new TrainingProgramService();
        skillService = new SkillService();

        typeCombo.setItems(FXCollections.observableArrayList(
                "en ligne", "présentiel", "hybride", "workshop"));

        durationSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 52, 4));

        // ✅ Charger les skills dans le ComboBox
        loadSkills();
    }

    private void loadSkills() {
        allSkills = skillService.getAll();

        // Afficher le nom du skill dans le ComboBox
        skillCombo.setCellFactory(lv -> new ListCell<Skill>() {
            @Override
            protected void updateItem(Skill item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNom() + " [" + item.getCategorie() + "]");
            }
        });

        skillCombo.setButtonCell(new ListCell<Skill>() {
            @Override
            protected void updateItem(Skill item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Sélectionner une compétence..." : item.getNom() + " [" + item.getCategorie() + "]");
            }
        });

        skillCombo.setItems(FXCollections.observableArrayList(allSkills));
    }

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
            // ✅ Créer le training program
            TrainingProgram p = new TrainingProgram();
            p.setTitle(title);
            p.setDescription(desc);
            p.setDuration(durationSpinner.getValue());
            p.setType(type);
            p.setStartDate(Date.valueOf(start));
            p.setEndDate(Date.valueOf(end));

            trainingService.create(p);

            // ✅ Si un skill est sélectionné, l'associer automatiquement
            Skill selectedSkill = skillCombo.getValue();
            if (selectedSkill != null && p.getId() > 0) {
                skillService.assignToTraining(selectedSkill.getId(), p.getId());
                System.out.println("✅ Skill '" + selectedSkill.getNom() + "' associé à '" + title + "'");
            }

            new Alert(Alert.AlertType.INFORMATION,
                    "✅ Programme '" + title + "' créé avec succès !" +
                            (selectedSkill != null ? "\n🔗 Skill '" + selectedSkill.getNom() + "' associé." : ""))
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
}