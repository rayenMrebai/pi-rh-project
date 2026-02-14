package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.model.salaire.Salaire;
import org.example.model.salaire.BonusRule;
import org.example.services.salaire.BonusRuleService;

public class AddFormBonusRuleController {

    @FXML private Label lblEmployeeName;
    @FXML private Label lblBaseAmount;

    @FXML private TextField txtNomRegle;
    @FXML private TextField txtPercentage;
    @FXML private TextArea txtCondition;

    @FXML private Button btnSave;
    @FXML private Button btnCancel;

    private BonusRuleService bonusRuleService;
    private Salaire currentSalaire;

    @FXML
    public void initialize() {
        bonusRuleService = new BonusRuleService();
    }

    /**
     * Méthode appelée depuis SalaireManagementController
     */
    public void setSalaire(Salaire salaire) {
        this.currentSalaire = salaire;
        displaySalaireInfo();
    }

    private void displaySalaireInfo() {
        if (currentSalaire != null) {
            lblEmployeeName.setText(currentSalaire.getUser().getName());
            lblBaseAmount.setText(String.format("%.2f DT", currentSalaire.getBaseAmount()));
        }
    }

    @FXML
    private void handleSave() {
        if (!validateInputs()) {
            return;
        }

        try {
            String nomRegle = txtNomRegle.getText().trim();
            double percentage = Double.parseDouble(txtPercentage.getText().trim());
            String condition = txtCondition.getText().trim();

            // ⭐ Créer la règle - Le bonus est calculé automatiquement dans le constructeur
            BonusRule bonusRule = new BonusRule(currentSalaire, nomRegle, percentage, condition);

            // Sauvegarder dans la DB
            bonusRuleService.create(bonusRule);

            showAlert("Succès", "Règle de bonus créée avec succès !", Alert.AlertType.INFORMATION);
            closeWindow();

        } catch (NumberFormatException e) {
            showAlert("Erreur", "Le pourcentage doit être un nombre valide", Alert.AlertType.ERROR);
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de la création: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private boolean validateInputs() {
        if (txtNomRegle.getText() == null || txtNomRegle.getText().trim().isEmpty()) {
            showAlert("Validation", "Veuillez entrer le nom de la règle", Alert.AlertType.WARNING);
            return false;
        }

        if (txtPercentage.getText() == null || txtPercentage.getText().trim().isEmpty()) {
            showAlert("Validation", "Veuillez entrer le pourcentage", Alert.AlertType.WARNING);
            return false;
        }

        try {
            double percentage = Double.parseDouble(txtPercentage.getText().trim());
            if (percentage <= 0 || percentage > 100) {
                showAlert("Validation", "Le pourcentage doit être entre 0 et 100", Alert.AlertType.WARNING);
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Validation", "Le pourcentage doit être un nombre valide", Alert.AlertType.WARNING);
            return false;
        }

        if (txtCondition.getText() == null || txtCondition.getText().trim().isEmpty()) {
            showAlert("Validation", "Veuillez entrer une condition", Alert.AlertType.WARNING);
            return false;
        }

        return true;
    }

    private void closeWindow() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}