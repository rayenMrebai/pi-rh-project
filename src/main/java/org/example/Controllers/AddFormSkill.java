package org.example.Controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.model.formation.Skill;
import org.example.services.SkillService;

import java.net.URL;
import java.util.ResourceBundle;

public class AddFormSkill implements Initializable {

    @FXML private TextField nomField;
    @FXML private TextArea descriptionField;
    @FXML private ComboBox<String> categorieCombo;
    @FXML private Spinner<Integer> levelSpinner;
    @FXML private Button ajouterBtn;
    @FXML private Label statusLabel;

    private SkillService skillService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        skillService = new SkillService();

        // Configurer le ComboBox
        categorieCombo.setItems(FXCollections.observableArrayList("technique", "soft"));

        // Configurer le Spinner
        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 5, 1);
        levelSpinner.setValueFactory(valueFactory);
    }

    @FXML
    private void handleAjouter() {
        try {
            // Validation
            if (!validerChamps()) {
                return;
            }

            // Créer le Skill
            Skill newSkill = new Skill();
            newSkill.setNom(nomField.getText().trim());
            newSkill.setDescription(descriptionField.getText().trim());
            newSkill.setCategorie(categorieCombo.getValue());
            newSkill.setLevelRequired(levelSpinner.getValue());

            newSkill.setLevelRequired(levelSpinner.getValue());

            // Ajouter en base
            skillService.create(newSkill);

            // Message de succès
            afficherMessage("✅ Compétence ajoutée avec succès ! Redirection...", "success");

            // Rediriger vers la liste après 1.5 seconde
            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                    javafx.application.Platform.runLater(() -> {
                        try {
                            naviguerVersListe();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (Exception e) {
            afficherMessage("❌ Erreur lors de l'ajout : " + e.getMessage(), "error");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRetourListe() {
        try {
            naviguerVersListe();
        } catch (Exception e) {
            afficherMessage("❌ Erreur de navigation", "error");
            e.printStackTrace();
        }
    }

    private boolean validerChamps() {
        if (nomField.getText().trim().isEmpty()) {
            afficherMessage("⚠️ Le nom de la compétence est obligatoire", "warning");
            nomField.requestFocus();
            return false;
        }

        if (descriptionField.getText().trim().isEmpty()) {
            afficherMessage("⚠️ La description est obligatoire", "warning");
            descriptionField.requestFocus();
            return false;
        }

        if (categorieCombo.getValue() == null) {
            afficherMessage("⚠️ Veuillez sélectionner une catégorie", "warning");
            categorieCombo.requestFocus();
            return false;
        }

        return true;
    }

    private void naviguerVersListe() throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/ListSkills.fxml"));
        Stage stage = (Stage) ajouterBtn.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Liste des Compétences");
        stage.setMaximized(true);
    }

    private void afficherMessage(String message, String type) {
        statusLabel.setText(message);

        switch (type) {
            case "success":
                statusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-size: 14px;");
                break;
            case "error":
                statusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-font-size: 14px;");
                break;
            case "warning":
                statusLabel.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold; -fx-font-size: 14px;");
                break;
        }
    }
}