package org.example.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.enums.SalaireStatus;
import org.example.model.salaire.Salaire;
import org.example.services.salaire.SalaireService;

import java.time.LocalDate;

public class UpdateFormSalaireController {

    @FXML private Label lblUserName;
    @FXML private Label lblBaseAmount;
    @FXML private Label lblBonusAmount;
    @FXML private Label lblTotalAmount;

    @FXML private ComboBox<SalaireStatus> cmbStatus;
    @FXML private DatePicker datePickerPaiement;

    @FXML private Button btnUpdate;
    @FXML private Button btnCancel;

    private SalaireService salaireService;
    private Salaire currentSalaire;

    @FXML
    public void initialize() {
        salaireService = new SalaireService();
        cmbStatus.setItems(FXCollections.observableArrayList(SalaireStatus.values()));
    }

    public void setSalaire(Salaire salaire) {
        this.currentSalaire = salaire;
        populateFields();
    }

    private void populateFields() {
        if (currentSalaire != null) {
            lblUserName.setText(currentSalaire.getUser().getName());
            lblBaseAmount.setText(String.format("%.2f DT", currentSalaire.getBaseAmount()));
            lblBonusAmount.setText(String.format("%.2f DT", currentSalaire.getBonusAmount()));
            lblTotalAmount.setText(String.format("%.2f DT", currentSalaire.getTotalAmount()));

            cmbStatus.setValue(currentSalaire.getStatus());
            datePickerPaiement.setValue(currentSalaire.getDatePaiement());
        }
    }

    @FXML
    private void handleUpdate() {
        if (!validateInputs()) {
            return;
        }

        try {
            currentSalaire.setStatus(cmbStatus.getValue());
            currentSalaire.setDatePaiement(datePickerPaiement.getValue());

            salaireService.update(currentSalaire);

            showAlert("Succès", "Salaire mis à jour avec succès !", Alert.AlertType.INFORMATION);
            closeWindow();

        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de la mise à jour: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    /**
     * ⭐ VALIDATION COMPLÈTE
     */
    private boolean validateInputs() {
        StringBuilder errors = new StringBuilder();

        // 1. Vérifier le statut
        if (cmbStatus.getValue() == null) {
            errors.append("• Veuillez sélectionner un statut\n");
        }

        // 2. Vérifier la date de paiement
        if (datePickerPaiement.getValue() == null) {
            errors.append("• La date de paiement est obligatoire\n");
        } else {
            LocalDate selectedDate = datePickerPaiement.getValue();
            LocalDate today = LocalDate.now();

            // ⭐ Si le statut est PAYÉ, la date doit être aujourd'hui ou dans le passé
            if (cmbStatus.getValue() == SalaireStatus.PAYÉ) {
                if (selectedDate.isAfter(today)) {
                    errors.append("• Un salaire PAYÉ ne peut pas avoir une date future\n");
                }
            } else {
                // Pour les autres statuts, la date ne peut pas être dans le passé
                if (selectedDate.isBefore(today)) {
                    errors.append("• La date de paiement ne peut pas être dans le passé\n");
                }
            }
        }

        // 3. Vérification de cohérence : PAYÉ nécessite un bonus > 0 ou accepter 0
        if (cmbStatus.getValue() == SalaireStatus.PAYÉ && currentSalaire.getBonusAmount() == 0) {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirmation");
            confirmation.setHeaderText("Salaire sans bonus");
            confirmation.setContentText("Ce salaire n'a aucun bonus appliqué. Voulez-vous continuer ?");

            if (confirmation.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                return false;
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