package org.example.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.enums.BonusRuleStatus;
import org.example.model.salaire.BonusRule;
import org.example.model.salaire.Salaire;
import org.example.services.salaire.BonusRuleService;
import org.example.services.salaire.SalaireService;

import java.util.List;

public class UpdateFormBonusRuleController {

    @FXML private Label lblEmployeeName;
    @FXML private Label lblBaseAmount;
    @FXML private Label lblBonusAmount;

    @FXML private TextField txtNomRegle;
    @FXML private TextField txtPercentage;
    @FXML private TextArea txtCondition;
    @FXML private ComboBox<BonusRuleStatus> cmbStatus;

    @FXML private Button btnUpdate;
    @FXML private Button btnCancel;

    private BonusRuleService bonusRuleService;
    private BonusRule currentBonusRule;

    @FXML
    public void initialize() {
        bonusRuleService = new BonusRuleService();

        // Remplir la ComboBox avec les statuts
        cmbStatus.setItems(FXCollections.observableArrayList(BonusRuleStatus.values()));
    }

    public void setBonusRule(BonusRule bonusRule) {
        this.currentBonusRule = bonusRule;
        populateFields();
    }

    private void populateFields() {
        if (currentBonusRule != null) {
            // Informations non modifiables
            lblEmployeeName.setText(currentBonusRule.getSalaire().getUser().getName());
            lblBaseAmount.setText(String.format("%.2f DT", currentBonusRule.getSalaire().getBaseAmount()));
            lblBonusAmount.setText(String.format("%.2f DT", currentBonusRule.getBonus()));

            // Champs modifiables
            txtNomRegle.setText(currentBonusRule.getNomRegle());
            txtPercentage.setText(String.valueOf(currentBonusRule.getPercentage()));
            txtCondition.setText(currentBonusRule.getCondition());
            cmbStatus.setValue(currentBonusRule.getStatus());
        }
    }

    @FXML
    private void handleUpdate() {
        if (!validateInputs()) {
            return;
        }

        try {
            // Sauvegarder l'ancien statut pour détecter un changement
            BonusRuleStatus oldStatus = currentBonusRule.getStatus();

            // Mettre à jour les champs
            currentBonusRule.setNomRegle(txtNomRegle.getText().trim());
            currentBonusRule.setCondition(txtCondition.getText().trim());
            currentBonusRule.setStatus(cmbStatus.getValue());

            // ⭐ setPercentage() recalcule AUTOMATIQUEMENT le bonus
            currentBonusRule.setPercentage(Double.parseDouble(txtPercentage.getText().trim()));

            // Sauvegarder la règle dans la DB
            bonusRuleService.update(currentBonusRule);

            // ⭐ Si le statut a changé, recalculer le bonus total du salaire
            BonusRuleStatus newStatus = cmbStatus.getValue();
            if (oldStatus != newStatus) {
                recalculateSalaryBonus(currentBonusRule.getSalaire().getId());
            }

            showAlert("Succès", "Règle de bonus mise à jour avec succès !", Alert.AlertType.INFORMATION);
            closeWindow();

        } catch (NumberFormatException e) {
            showAlert("Erreur", "Le pourcentage doit être un nombre valide", Alert.AlertType.ERROR);
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de la mise à jour: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    /**
     * ⭐ Recalcule le bonus total du salaire en fonction des règles ACTIVES
     */
    private void recalculateSalaryBonus(int salaireId) {
        try {
            SalaireService salaireService = new SalaireService();

            // Récupérer le salaire complet
            Salaire salaire = salaireService.getById(salaireId);

            if (salaire != null) {
                // Récupérer toutes les règles du salaire
                List<BonusRule> rules = bonusRuleService.getRulesBySalaire(salaireId);
                salaire.setBonusRules(rules);

                // ⭐ Recalculer le bonus total
                salaire.recalculateBonusFromActiveRules();

                // ⭐ Mettre à jour dans la DB
                salaireService.updateBonusAndTotal(salaire);

                System.out.println("✅ Bonus recalculé: " + salaire.getBonusAmount() + " DT");
                System.out.println("✅ Total mis à jour: " + salaire.getTotalAmount() + " DT");
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur recalcul bonus: " + e.getMessage());
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

        if (cmbStatus.getValue() == null) {
            showAlert("Validation", "Veuillez sélectionner un statut", Alert.AlertType.WARNING);
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