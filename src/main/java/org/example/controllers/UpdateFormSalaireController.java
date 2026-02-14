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

        // Remplir la ComboBox avec les valeurs de l'enum
        cmbStatus.setItems(FXCollections.observableArrayList(SalaireStatus.values()));
    }

    public void setSalaire(Salaire salaire) {
        this.currentSalaire = salaire;
        populateFields();
    }

    private void populateFields() {
        if (currentSalaire != null) {
            // Informations non modifiables - utilise getName()
            lblUserName.setText(currentSalaire.getUser().getName());
            lblBaseAmount.setText(String.format("%.2f DT", currentSalaire.getBaseAmount()));
            lblBonusAmount.setText(String.format("%.2f DT", currentSalaire.getBonusAmount()));
            lblTotalAmount.setText(String.format("%.2f DT", currentSalaire.getTotalAmount()));

            // Champs modifiables
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
            // Mettre à jour uniquement les champs modifiables
            currentSalaire.setStatus(cmbStatus.getValue());
            currentSalaire.setDatePaiement(datePickerPaiement.getValue());

            salaireService.update(currentSalaire);

            showAlert("Succès", "Salaire mis à jour avec succès !", Alert.AlertType.INFORMATION);
            closeWindow();

        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de la mise à jour: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private boolean validateInputs() {
        if (cmbStatus.getValue() == null) {
            showAlert("Validation", "Veuillez sélectionner un statut", Alert.AlertType.WARNING);
            return false;
        }

        if (datePickerPaiement.getValue() == null) {
            showAlert("Validation", "Veuillez sélectionner une date de paiement", Alert.AlertType.WARNING);
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