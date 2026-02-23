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
import java.util.List;
import java.util.ResourceBundle;

public class AddFormSkillController implements Initializable {

    @FXML private TextField nomField;
    @FXML private TextArea descriptionField;
    @FXML private ComboBox<String> levelCombo;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private ComboBox<TrainingProgram> trainingCombo; // ✅ NOUVEAU
    @FXML private Label errorLabel;

    private SkillService skillService;
    private TrainingProgramService trainingService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        skillService = new SkillService();
        trainingService = new TrainingProgramService();

        levelCombo.setItems(FXCollections.observableArrayList(
                "1 - BASE", "2 - DÉBUTANT", "3 - INTERMÉDIAIRE", "4 - AVANCÉ", "5 - EXPERT"));

        categoryCombo.setItems(FXCollections.observableArrayList("technique", "soft"));

        // ✅ Charger les formations dans le ComboBox
        loadTrainings();
    }

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
                setText(empty || item == null ? "Sélectionner une formation..." : item.getTitle() + " [" + item.getType() + "]");
            }
        });

        trainingCombo.setItems(FXCollections.observableArrayList(trainings));
    }

    @FXML
    private void handleSave() {
        errorLabel.setText("");

        String nom = nomField.getText().trim();
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

            // ✅ Si une formation est sélectionnée, l'associer
            TrainingProgram selectedTraining = trainingCombo.getValue();
            if (selectedTraining != null) {
                skill.setTrainingProgramId(selectedTraining.getId());
            }

            skillService.create(skill);

            new Alert(Alert.AlertType.INFORMATION,
                    "✅ Compétence '" + nom + "' créée avec succès !" +
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