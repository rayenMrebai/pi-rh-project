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

public class AddFormSkillController implements Initializable {

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

        // Ajouter des contrôles en temps réel
        ajouterControlesEnTempsReel();
    }

    /**
     * Ajouter des contrôles de saisie en temps réel
     */
    private void ajouterControlesEnTempsReel() {
        // Limiter le nom à 100 caractères
        nomField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 100) {
                nomField.setText(oldValue);
            }
        });

        // Limiter la description à 500 caractères
        descriptionField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 500) {
                descriptionField.setText(oldValue);
            }
        });

        // Empêcher les chiffres dans le nom (optionnel)
        // nomField.textProperty().addListener((observable, oldValue, newValue) -> {
        //     if (!newValue.matches("[a-zA-ZÀ-ÿ\\s+-]*")) {
        //         nomField.setText(oldValue);
        //     }
        // });
    }

    @FXML
    private void handleAjouter() {
        try {
            // Validation complète
            if (!validerChamps()) {
                return;
            }

            // Créer le Skill
            Skill newSkill = new Skill(
                    nomField.getText().trim(),
                    descriptionField.getText().trim(),
                    categorieCombo.getValue(),
                    levelSpinner.getValue().intValue()
            );
            newSkill.setLevelRequired(levelSpinner.getValue());

            // Ajouter en base
            skillService.create(newSkill);

            // Message de succès
            afficherMessage("✅ Compétence ajoutée avec succès ! Redirection...", "success");

            // Redirection
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

    /**
     * VALIDATION COMPLÈTE DES CHAMPS
     */
    private boolean validerChamps() {
        // 1. Validation du nom
        if (nomField.getText() == null || nomField.getText().trim().isEmpty()) {
            afficherMessage("⚠️ Le nom de la compétence est obligatoire", "warning");
            nomField.requestFocus();
            nomField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
            return false;
        }
        nomField.setStyle(""); // Réinitialiser le style

        // Vérifier la longueur minimale
        if (nomField.getText().trim().length() < 2) {
            afficherMessage("⚠️ Le nom doit contenir au moins 2 caractères", "warning");
            nomField.requestFocus();
            nomField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
            return false;
        }
        nomField.setStyle("");

        // Vérifier la longueur maximale
        if (nomField.getText().trim().length() > 100) {
            afficherMessage("⚠️ Le nom ne doit pas dépasser 100 caractères", "warning");
            nomField.requestFocus();
            nomField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
            return false;
        }
        nomField.setStyle("");

        // 2. Validation de la description
        if (descriptionField.getText() == null || descriptionField.getText().trim().isEmpty()) {
            afficherMessage("⚠️ La description est obligatoire", "warning");
            descriptionField.requestFocus();
            descriptionField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
            return false;
        }
        descriptionField.setStyle("");

        // Vérifier la longueur minimale de la description
        if (descriptionField.getText().trim().length() < 10) {
            afficherMessage("⚠️ La description doit contenir au moins 10 caractères", "warning");
            descriptionField.requestFocus();
            descriptionField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
            return false;
        }
        descriptionField.setStyle("");

        // Vérifier la longueur maximale de la description
        if (descriptionField.getText().trim().length() > 500) {
            afficherMessage("⚠️ La description ne doit pas dépasser 500 caractères", "warning");
            descriptionField.requestFocus();
            descriptionField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
            return false;
        }
        descriptionField.setStyle("");

        // 3. Validation de la catégorie
        if (categorieCombo.getValue() == null) {
            afficherMessage("⚠️ Veuillez sélectionner une catégorie", "warning");
            categorieCombo.requestFocus();
            categorieCombo.setStyle("-fx-border-color: red; -fx-border-width: 2;");
            return false;
        }
        categorieCombo.setStyle("");

        // Vérifier que la catégorie est valide
        if (!categorieCombo.getValue().equals("technique") && !categorieCombo.getValue().equals("soft")) {
            afficherMessage("⚠️ Catégorie invalide. Choisissez 'technique' ou 'soft'", "warning");
            categorieCombo.requestFocus();
            categorieCombo.setStyle("-fx-border-color: red; -fx-border-width: 2;");
            return false;
        }
        categorieCombo.setStyle("");

        // 4. Validation du niveau
        if (levelSpinner.getValue() == null) {
            afficherMessage("⚠️ Veuillez définir un niveau requis", "warning");
            levelSpinner.requestFocus();
            levelSpinner.setStyle("-fx-border-color: red; -fx-border-width: 2;");
            return false;
        }
        levelSpinner.setStyle("");

        // Vérifier que le niveau est dans la plage valide
        if (levelSpinner.getValue() < 0 || levelSpinner.getValue() > 5) {
            afficherMessage("⚠️ Le niveau doit être entre 0 et 5", "warning");
            levelSpinner.requestFocus();
            levelSpinner.setStyle("-fx-border-color: red; -fx-border-width: 2;");
            return false;
        }
        levelSpinner.setStyle("");

        // Toutes les validations passées
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