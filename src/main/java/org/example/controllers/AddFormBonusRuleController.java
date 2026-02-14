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
        setupValidation();
    }

    /**
     * ⭐ Configuration de la validation en temps réel
     */
    private void setupValidation() {
        // Validation du pourcentage en temps réel
        txtPercentage.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                txtPercentage.setText(oldValue);
            }
        });

        // Limiter la longueur
        txtPercentage.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.length() > 6) { // Max: 100.99
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

            BonusRule bonusRule = new BonusRule(currentSalaire, nomRegle, percentage, condition);
            bonusRuleService.create(bonusRule);

            showAlert("Succès", "Règle de bonus créée avec succès !", Alert.AlertType.INFORMATION);
            closeWindow();

        } catch (NumberFormatException e) {
            showAlert("Erreur", "Format de pourcentage invalide", Alert.AlertType.ERROR);
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de la création: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    /**
     * ⭐ VALIDATION COMPLÈTE
     */
    private boolean validateInputs() {
        StringBuilder errors = new StringBuilder();

        // 1. Vérifier le nom de la règle
        if (txtNomRegle.getText() == null || txtNomRegle.getText().trim().isEmpty()) {
            errors.append("• Le nom de la règle est obligatoire\n");
        } else {
            String nomRegle = txtNomRegle.getText().trim();
            if (nomRegle.length() < 3) {
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
            if (condition.length() < 5) {
                errors.append("• La condition doit contenir au moins 5 caractères\n");
            } else if (condition.length() > 500) {
                errors.append("• La condition ne peut pas dépasser 500 caractères\n");
            }
        }

        // Afficher les erreurs
        if (errors.length() > 0) {
            showAlert("Validation", errors.toString(), Alert.AlertType.WARNING);
            return false;
        }

        return true;
    }

    @FXML
    private void handleCancel() {
        closeWindow();
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