package org.example.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.model.salaire.Salaire;
import org.example.enums.SalaireStatus;
import org.example.services.salaire.SalaireService;

public class UpdateFormSalaireController {

    @FXML private Label lblEmployeeName;
    @FXML private TextField txtBaseAmount;
    @FXML private ComboBox<SalaireStatus> comboStatus;
    @FXML private DatePicker datePaiement;
    @FXML private Button btnUpdate;

    private Salaire selectedSalaire;
    private final SalaireService salaireService = new SalaireService();

    @FXML
    public void initialize() {
        comboStatus.getItems().setAll(SalaireStatus.values());
    }

    /**
     * Reçoit le salaire sélectionné depuis le SalaireManagementController
     */
    public void setSalaire(Salaire s) {
        this.selectedSalaire = s;
        lblEmployeeName.setText("Employé : " + s.getUser().getUsername());
        txtBaseAmount.setText(String.valueOf(s.getBaseAmount()));
        comboStatus.setValue(s.getStatus());
        datePaiement.setValue(s.getDatePaiement());
    }

    @FXML
    private void handleUpdate() {
        try {
            double newBase = Double.parseDouble(txtBaseAmount.getText());

            // Mise à jour de l'objet local
            selectedSalaire.setBaseAmount(newBase);
            // Recalcul du total au cas où la base a changé
            selectedSalaire.setTotalAmount(newBase + selectedSalaire.getBonusAmount());
            selectedSalaire.setStatus(comboStatus.getValue());
            selectedSalaire.setDatePaiement(datePaiement.getValue());

            // Appel au service (le service gère l'UPDATE SQL + Email si PAYÉ)
            salaireService.update(selectedSalaire);

            closeWindow();
        } catch (NumberFormatException e) {
            Alert a = new Alert(Alert.AlertType.ERROR, "Montant invalide");
            a.show();
        }
    }

    private void closeWindow() {
        ((Stage) btnUpdate.getScene().getWindow()).close();
    }
}