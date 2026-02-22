package org.example.Controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.model.formation.Skill;
import org.example.services.SkillService;

import java.net.URL;
import java.util.ResourceBundle;

public class UpdateFormSkillController implements Initializable {

    @FXML private Label idLabel;
    @FXML private TextField nomField;
    @FXML private TextArea descriptionField;
    @FXML private ComboBox<String> levelCombo;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private Label errorLabel;

    private SkillService skillService;
    private Skill currentSkill;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        skillService = new SkillService();
        levelCombo.setItems(FXCollections.observableArrayList(
                "1 - BASE", "2 - DÉBUTANT", "3 - INTERMÉDIAIRE", "4 - AVANCÉ", "5 - EXPERT"));
        categoryCombo.setItems(FXCollections.observableArrayList("technique", "soft"));
    }

    public void setSkill(Skill skill) {
        this.currentSkill = skill;
        idLabel.setText(String.valueOf(skill.getId()));
        nomField.setText(skill.getNom());
        descriptionField.setText(skill.getDescription());
        categoryCombo.setValue(skill.getCategorie());
        switch (skill.getLevelRequired()) {
            case 5: levelCombo.setValue("5 - EXPERT"); break;
            case 4: levelCombo.setValue("4 - AVANCÉ"); break;
            case 3: levelCombo.setValue("3 - INTERMÉDIAIRE"); break;
            case 2: levelCombo.setValue("2 - DÉBUTANT"); break;
            default: levelCombo.setValue("1 - BASE");
        }
    }

    @FXML
    private void handleUpdate() {
        errorLabel.setText("");
        String nom = nomField.getText().trim();
        String desc = descriptionField.getText().trim();
        String lvl = levelCombo.getValue();
        String cat = categoryCombo.getValue();

        if (nom.isEmpty() || desc.isEmpty() || lvl == null || cat == null) {
            errorLabel.setText("⚠️ Veuillez remplir tous les champs obligatoires.");
            return;
        }
        try {
            currentSkill.setNom(nom);
            currentSkill.setDescription(desc);
            currentSkill.setLevelRequired(Integer.parseInt(lvl.substring(0, 1)));
            currentSkill.setCategorie(cat);
            skillService.update(currentSkill);
            new Alert(Alert.AlertType.INFORMATION, "✅ Compétence mise à jour !").showAndWait();
            ((Stage) nomField.getScene().getWindow()).close();
        } catch (Exception e) {
            errorLabel.setText("❌ Erreur : " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        ((Stage) nomField.getScene().getWindow()).close();
    }
}