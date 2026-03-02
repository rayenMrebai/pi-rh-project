package org.example.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.enums.UserRole;
import org.example.model.salaire.Salaire;
import org.example.model.user.UserAccount;
import org.example.enums.SalaireStatus;
import org.example.services.salaire.SalaireService;
import org.example.services.user.UserAccountService; // Supposant que ce service existe

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class AddFormSalaireController {

    @FXML private ComboBox<UserAccount> comboEmployee;
    @FXML private TextField txtBaseAmount;
    @FXML private DatePicker datePaiement;
    @FXML private Button btnSave;

    private final SalaireService salaireService = new SalaireService();
    private final UserAccountService userService = new UserAccountService();

    @FXML
    public void initialize() {
        // Remplacer getAllEmployees() par getAll()
        // et filtrer pour ne garder que les utilisateurs (si nécessaire)
        List<UserAccount> users = userService.getAll();

        // Si vous voulez filtrer par rôle directement dans le contrôleur :
        List<UserAccount> employees = users.stream()
                .filter(u -> u.getRole() == UserRole.EMPLOYE)
                .collect(Collectors.toList());

        comboEmployee.getItems().setAll(employees);
        datePaiement.setValue(LocalDate.now());
    }

    @FXML
    private void handleSave() {
        try {
            UserAccount selectedUser = comboEmployee.getValue();
            double base = Double.parseDouble(txtBaseAmount.getText());

            if (selectedUser == null) {
                showAlert("Erreur", "Veuillez sélectionner un employé.");
                return;
            }

            // Création de l'objet Salaire
            Salaire s = new Salaire();
            s.setUser(selectedUser);
            s.setBaseAmount(base);
            s.setBonusAmount(0.0); // Initialement 0
            s.setTotalAmount(base); // Total = Base au début
            s.setStatus(SalaireStatus.CREÉ);
            s.setDatePaiement(datePaiement.getValue());

            // Appel au service (le service gère l'INSERT)
            salaireService.create(s);

            closeWindow();
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Le montant doit être un nombre valide.");
        }
    }

    private void closeWindow() {
        ((Stage) btnSave.getScene().getWindow()).close();
    }

    private void showAlert(String title, String content) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setContentText(content);
        a.show();
    }
}