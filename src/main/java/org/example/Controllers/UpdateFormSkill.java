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

public class UpdateFormSkill implements Initializable {

    @FXML private TextField idField;
    @FXML private TextField nomField;
    @FXML private TextArea descriptionField;
    @FXML private ComboBox<String> categorieCombo;
    @FXML private Spinner<Integer> levelSpinner;
    @FXML private Button modifierBtn;
    @FXML private Label statusLabel;

    private SkillService skillService;
    private Skill skillToUpdate;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        skillService = new SkillService();

        // Configurer le ComboBox
        categorieCombo.setItems(FXCollections.observableArrayList("technique", "soft"));

        // Configurer le Spinner
        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 5, 1);
        levelSpinner.setValueFactory(valueFactory);

        System.out.println("✅ Formulaire de modification initialisé");
    }

    /**
     * MÉTHODE CRUCIALE : Pré-remplir le formulaire avec les données du skill
     */
    public void setSkill(Skill skill) {
        if (skill == null) {
            System.err.println("❌ ERREUR : Le skill passé est null !");
            return;
        }

        this.skillToUpdate = skill;

        System.out.println("📝 Pré-remplissage du formulaire avec : " + skill);

        // Remplir chaque champ
        idField.setText(String.valueOf(skill.getId()));
        nomField.setText(skill.getNom());
        descriptionField.setText(skill.getDescription());
        categorieCombo.setValue(skill.getCategorie());
        levelSpinner.getValueFactory().setValue(skill.getLevelRequired());

        System.out.println("✅ Formulaire pré-rempli avec succès :");
        System.out.println("   - ID: " + skill.getId());
        System.out.println("   - Nom: " + skill.getNom());
        System.out.println("   - Description: " + skill.getDescription());
        System.out.println("   - Catégorie: " + skill.getCategorie());
        System.out.println("   - Niveau: " + skill.getLevelRequired());
    }

    @FXML
    private void handleModifier() {
        try {
            System.out.println("🔄 Début de la modification...");

            // Validation
            if (!validerChamps()) {
                return;
            }

            // Afficher les anciennes et nouvelles valeurs
            System.out.println("📊 Modifications :");
            System.out.println("   Ancien nom: " + skillToUpdate.getNom() + " → Nouveau: " + nomField.getText().trim());

            // Mettre à jour les données
            skillToUpdate.setNom(nomField.getText().trim());
            skillToUpdate.setDescription(descriptionField.getText().trim());
            skillToUpdate.setCategorie(categorieCombo.getValue());
            skillToUpdate.setLevelRequired(levelSpinner.getValue());

            // Enregistrer en base
            skillService.update(skillToUpdate);

            // Message de succès
            afficherMessage("✅ Compétence modifiée avec succès ! Redirection...", "success");

            // Redirection après 1.5 seconde
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
            afficherMessage("❌ Erreur lors de la modification : " + e.getMessage(), "error");
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
        Stage stage = (Stage) modifierBtn.getScene().getWindow();
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