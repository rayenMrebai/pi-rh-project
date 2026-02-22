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

public class AddFormSkillController implements Initializable {

    @FXML private TextField nomField;
    @FXML private TextArea descriptionField;
    @FXML private ComboBox<String> levelCombo;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private Label errorLabel;

    private SkillService skillService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        skillService = new SkillService();
        levelCombo.setItems(FXCollections.observableArrayList(
                "1 - BASE", "2 - DÉBUTANT", "3 - INTERMÉDIAIRE", "4 - AVANCÉ", "5 - EXPERT"));
        categoryCombo.setItems(FXCollections.observableArrayList("technique", "soft"));
    }

    @FXML
    private void handleSave() {
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
            Skill s = new Skill();
            s.setNom(nom);
            s.setDescription(desc);
            s.setLevelRequired(Integer.parseInt(lvl.substring(0, 1)));
            s.setCategorie(cat);
            skillService.create(s);
            new Alert(Alert.AlertType.INFORMATION, "✅ Compétence '" + nom + "' ajoutée !").showAndWait();
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