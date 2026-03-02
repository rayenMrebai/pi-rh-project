package org.example.Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.model.salaire.Salaire;
import org.example.model.salaire.BonusRule;
import org.example.services.salaire.SalaireService;
import org.example.services.salaire.BonusRuleService;
import java.io.IOException;

public class SalaireManagementController {

    @FXML private ListView<Salaire> salaryListView;
    @FXML private Label lblTotalAmount;
    @FXML private Label lblStatus;

    private final SalaireService salaireService = new SalaireService();
    private final BonusRuleService bonusRuleService = new BonusRuleService();
    private ObservableList<Salaire> salaryList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        loadData();
    }

    private void loadData() {
        salaryList.setAll(salaireService.getAll());
        salaryListView.setItems(salaryList);
    }

    @FXML
    private void handleAddSalary() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/AddFormSalaire.fxml"));
        showModal(loader, "Nouveau Salaire");
        loadData(); // Rafraîchir après fermeture
    }

    @FXML
    private void handleUpdateSalary() throws IOException {
        Salaire selected = salaryListView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/UpdateFormSalaire.fxml"));
        Parent root = loader.load();

        // PASSAGE DE L'OBJET AU CONTROLEUR DE MODIFICATION
        UpdateFormSalaireController ctrl = loader.getController();
        ctrl.setSalaire(selected);

        showStage(root, "Modifier Salaire");
        loadData(); // Rafraîchir après modification
    }

    @FXML
    private void handleAddBonus() throws IOException {
        Salaire selected = salaryListView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/AddFormBonusRule.fxml"));
        Parent root = loader.load();

        org.example.Controllers.AddFormBonusRuleController ctrl = loader.getController();
        ctrl.setSalaire(selected); // Lier le bonus au salaire sélectionné

        showStage(root, "Ajouter un Bonus");
        loadData(); // Très important pour voir le nouveau Total recalculé
    }

    private void showModal(FXMLLoader loader, String title) throws IOException {
        showStage(loader.load(), title);
    }

    private void showStage(Parent root, String title) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(title);
        stage.setScene(new Scene(root));
        stage.showAndWait(); // Bloque ici jusqu'à la fermeture de la fenêtre
    }
}