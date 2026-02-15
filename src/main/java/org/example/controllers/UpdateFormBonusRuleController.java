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
        cmbStatus.setItems(FXCollections.observableArrayList(BonusRuleStatus.values()));
        setupValidation();
    }

    private void setupValidation() {
        // Validation du pourcentage en temps réel
        txtPercentage.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                txtPercentage.setText(oldValue);
            }
        });

        txtPercentage.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.length() > 6) {
                return null;
            }
            return change;
        }));

        txtNomRegle.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.length() > 100) {
                return null;
            }
            return change;
        }));

        txtCondition.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.length() > 500) {
                return null;
            }
            return change;
        }));
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
     * Recalcule le bonus total du salaire en fonction des règles ACTIVES
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

    /**
     * VALIDATION COMPLÈTE (Version améliorée)
     */
    /**
     * ⭐ VALIDATION COMPLÈTE avec contrôle des caractères spéciaux
     */
    private boolean validateInputs() {
        StringBuilder errors = new StringBuilder();

        // 1. Vérifier le nom de la règle
        if (txtNomRegle.getText() == null || txtNomRegle.getText().trim().isEmpty()) {
            errors.append("• Le nom de la règle est obligatoire\n");
        } else {
            String nomRegle = txtNomRegle.getText().trim();

            // ⭐ Vérifier les caractères valides (lettres, chiffres, espaces, tirets)
            if (!nomRegle.matches("^[a-zA-ZÀ-ÿ0-9\\s-]+$")) {
                errors.append("• Le nom ne peut contenir que des lettres, chiffres, espaces et tirets\n");
            } else if (nomRegle.length() < 3) {
                errors.append("• Le nom doit contenir au moins 3 caractères\n");
            } else if (nomRegle.length() > 100) {
                errors.append("• Le nom ne peut pas dépasser 100 caractères\n");
            }
        }

        // 2. Vérifier le pourcentage
        if (txtPercentage.getText() == null || txtPercentage.getText().trim().isEmpty()) {
            errors.append("• Le pourcentage est obligatoire\n");
        } else {
            try {
                double percentage = Double.parseDouble(txtPercentage.getText().trim());
                if (percentage <= 0) {
                    errors.append("• Le pourcentage doit être supérieur à 0%\n");
                } else if (percentage > 100) {
                    errors.append("• Le pourcentage ne peut pas dépasser 100%\n");
                }
            } catch (NumberFormatException e) {
                errors.append("• Le pourcentage doit être un nombre valide\n");
            }
        }

        // 3. Vérifier la condition
        if (txtCondition.getText() == null || txtCondition.getText().trim().isEmpty()) {
            errors.append("• La condition est obligatoire\n");
        } else {
            String condition = txtCondition.getText().trim();

            // ⭐ Vérifier les caractères valides (lettres, chiffres, espaces, ponctuation de base)
            if (!condition.matches("^[a-zA-ZÀ-ÿ0-9\\s.,;:!?'\"()\\->=<%]+$")) {
                errors.append("• La condition contient des caractères non autorisés\n");
            } else if (condition.length() < 5) {
                errors.append("• La condition doit contenir au moins 5 caractères\n");
            } else if (condition.length() > 500) {
                errors.append("• La condition ne peut pas dépasser 500 caractères\n");
            }
        }

        // 4. Vérifier le statut
        if (cmbStatus.getValue() == null) {
            errors.append("• Veuillez sélectionner un statut\n");
        }

        // Afficher les erreurs
        if (errors.length() > 0) {
            showAlert("Validation", errors.toString(), Alert.AlertType.WARNING);
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
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}