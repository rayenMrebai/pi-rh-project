package org.example.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
    private UserAccountService userAccountService;

    @FXML
    public void initialize() {
        salaireService = new SalaireService();
        userAccountService = new UserAccountService();

        loadUsers();
        setupValidation();
    }

    private void loadUsers() {
        try {
            List<UserAccount> users = userAccountService.getAll();
            ObservableList<UserAccount> userList = FXCollections.observableArrayList(users);
            cmbUser.setItems(userList);

            cmbUser.setCellFactory(lv -> new ListCell<UserAccount>() {
                @Override
                protected void updateItem(UserAccount user, boolean empty) {
                    super.updateItem(user, empty);
                    setText(empty || user == null ? "" : user.getName() + " (ID: " + user.getId() + ")");
                }
            });

            cmbUser.setButtonCell(new ListCell<UserAccount>() {
                @Override
                protected void updateItem(UserAccount user, boolean empty) {
                    super.updateItem(user, empty);
                    setText(empty || user == null ? "" : user.getName() + " (ID: " + user.getId() + ")");
                }
            });

        } catch (Exception e) {
            showAlert("Erreur", "Impossible de charger les utilisateurs: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * ⭐ Configuration de la validation en temps réel
     */
    private void setupValidation() {
        // Validation du montant en temps réel
        txtBaseAmount.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                txtBaseAmount.setText(oldValue);
            }
        });

        // Limiter la longueur du montant
        txtBaseAmount.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.length() > 10) {
                return null;
            }
            return change;
        }));
    }

    @FXML
    private void handleSave() {
        if (!validateInputs()) {
            return;
        }

        try {
            UserAccount selectedUser = cmbUser.getValue();
            double baseAmount = Double.parseDouble(txtBaseAmount.getText().trim());
            LocalDate datePaiement = datePickerPaiement.getValue();

            Salaire salaire = new Salaire(selectedUser, baseAmount, datePaiement);
            salaireService.create(salaire);

            showAlert("Succès", "Salaire créé avec succès !", Alert.AlertType.INFORMATION);
            closeWindow();

        } catch (NumberFormatException e) {
            showAlert("Erreur", "Format de montant invalide", Alert.AlertType.ERROR);
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

        // 1. Vérifier l'utilisateur
        if (cmbUser.getValue() == null) {
            errors.append("• Veuillez sélectionner un employé\n");
        }

        // 2. Vérifier le montant de base
        if (txtBaseAmount.getText() == null || txtBaseAmount.getText().trim().isEmpty()) {
            errors.append("• Le salaire de base est obligatoire\n");
        } else {
            try {
                double amount = Double.parseDouble(txtBaseAmount.getText().trim());
                if (amount <= 0) {
                    errors.append("• Le salaire doit être supérieur à 0 DT\n");
                } else if (amount > 1_000_000) {
                    errors.append("• Le salaire ne peut pas dépasser 1,000,000 DT\n");
                }
            } catch (NumberFormatException e) {
                errors.append("• Le salaire doit être un nombre valide\n");
            }
        }

        // 3. Vérifier la date de paiement
        if (datePickerPaiement.getValue() == null) {
            errors.append("• La date de paiement est obligatoire\n");
        } else {
            LocalDate selectedDate = datePickerPaiement.getValue();
            LocalDate today = LocalDate.now();

            if (selectedDate.isBefore(today)) {
                errors.append("• La date de paiement ne peut pas être dans le passé\n");
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