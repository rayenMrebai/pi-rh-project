package org.example.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.model.salaire.Salaire;
import org.example.model.user.UserAccount;
import org.example.services.salaire.SalaireService;
import org.example.services.user.UserAccountService;

import java.time.LocalDate;
import java.util.List;

public class AddFormSalaireController {

    @FXML private ComboBox<UserAccount> cmbUser;
    @FXML private TextField txtBaseAmount;
    @FXML private DatePicker datePickerPaiement;
    @FXML private Button btnSave;
    @FXML private Button btnCancel;

    private SalaireService salaireService;
    private UserAccountService userService;

    @FXML
    public void initialize() {
        salaireService = new SalaireService();
        userService = new UserAccountService();

        loadUsers();

        // Format d'affichage pour la ComboBox - utilise getName()
        cmbUser.setCellFactory(lv -> new ListCell<UserAccount>() {
            @Override
            protected void updateItem(UserAccount user, boolean empty) {
                super.updateItem(user, empty);
                setText(empty || user == null ? null : user.getName() + " (ID: " + user.getId() + ")");
            }
        });

        cmbUser.setButtonCell(new ListCell<UserAccount>() {
            @Override
            protected void updateItem(UserAccount user, boolean empty) {
                super.updateItem(user, empty);
                setText(empty || user == null ? null : user.getName());
            }
        });
    }

    private void loadUsers() {
        try {
            List<UserAccount> users = userService.getAll();
            cmbUser.setItems(FXCollections.observableArrayList(users));
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de charger les utilisateurs: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleSave() {
        if (!validateInputs()) {
            return;
        }

        try {
            UserAccount selectedUser = cmbUser.getValue();
            double baseAmount = Double.parseDouble(txtBaseAmount.getText());
            LocalDate datePaiement = datePickerPaiement.getValue();

            // Utiliser le constructeur qui initialise automatiquement
            Salaire salaire = new Salaire(selectedUser, baseAmount, datePaiement);

            salaireService.create(salaire);

            showAlert("Succès", "Salaire créé avec succès !", Alert.AlertType.INFORMATION);
            closeWindow();

        } catch (NumberFormatException e) {
            showAlert("Erreur", "Le montant doit être un nombre valide", Alert.AlertType.ERROR);
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de la création: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private boolean validateInputs() {
        if (cmbUser.getValue() == null) {
            showAlert("Validation", "Veuillez sélectionner un utilisateur", Alert.AlertType.WARNING);
            return false;
        }

        if (txtBaseAmount.getText() == null || txtBaseAmount.getText().trim().isEmpty()) {
            showAlert("Validation", "Veuillez entrer le salaire de base", Alert.AlertType.WARNING);
            return false;
        }

        try {
            double amount = Double.parseDouble(txtBaseAmount.getText());
            if (amount <= 0) {
                showAlert("Validation", "Le salaire doit être supérieur à 0", Alert.AlertType.WARNING);
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Validation", "Le salaire doit être un nombre valide", Alert.AlertType.WARNING);
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