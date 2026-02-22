package org.example.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.enums.SalaireStatus;
import org.example.model.salaire.Salaire;
import org.example.model.salaire.BonusRule;
import org.example.services.salaire.BonusRuleService;
import org.example.services.salaire.SalaireService;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.example.enums.BonusRuleStatus;

import org.example.services.pdf.PDFService;
import javafx.application.HostServices;

public class SalaireManagementController {

    @FXML private TextField searchField;
    @FXML private Button btnAddSalary;
    @FXML private ListView<Salaire> salaryListView;

    @FXML private VBox detailsContainer;

    @FXML private Label lblEmployeeName;
    @FXML private Label lblBaseAmount;
    @FXML private Label lblBonusAmount;
    @FXML private Label lblTotalAmount;
    @FXML private Label lblStatus;
    @FXML private Label lblDatePaiement;

    @FXML private Button btnUpdateSalary;
    @FXML private Button btnDeleteSalary;

    @FXML private ListView<BonusRule> bonusRulesListView;
    @FXML private Button btnAddRule;

    private SalaireService salaireService;
    private BonusRuleService bonusRuleService;
    private ObservableList<Salaire> salaryList;
    private Salaire selectedSalaire;

    @FXML private Button btnGeneratePDF;
    private PDFService pdfService;

    @FXML
    public void initialize() {
        salaireService = new SalaireService();
        bonusRuleService = new BonusRuleService();
        salaryList = FXCollections.observableArrayList();

        pdfService = new PDFService();

        // Configuration ListView des salaires
        salaryListView.setCellFactory(lv -> new ListCell<Salaire>() {
            @Override
            protected void updateItem(Salaire salaire, boolean empty) {
                super.updateItem(salaire, empty);
                if (empty || salaire == null) {
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    HBox row = createSalaryRow(salaire);
                    setGraphic(row);

                    setOnMouseEntered(e -> setStyle("-fx-background-color: #f7fafc; -fx-cursor: hand;"));
                    setOnMouseExited(e -> setStyle("-fx-background-color: transparent;"));
                }
            }
        });

        // Listener pour la sélection d'un salaire
        salaryListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        selectedSalaire = newSelection;
                        displaySalaryDetails(newSelection);
                        loadBonusRules(newSelection);
                        showDetailsPanel(true);
                    } else {
                        showDetailsPanel(false);
                    }
                }
        );

        // Configuration ListView des règles de bonus
        bonusRulesListView.setCellFactory(lv -> new ListCell<BonusRule>() {
            @Override
            protected void updateItem(BonusRule rule, boolean empty) {
                super.updateItem(rule, empty);
                if (empty || rule == null) {
                    setGraphic(null);
                } else {
                    HBox row = createBonusRuleRow(rule);
                    setGraphic(row);
                }
            }
        });

        // Recherche dynamique
        searchField.textProperty().addListener((obs, oldValue, newValue) -> filterSalaries(newValue));

        // Charger les données
        loadAllSalaries();
    }

    /**
     * Crée une ligne personnalisée pour chaque salaire dans la ListView
     */
    private HBox createSalaryRow(Salaire salaire) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 10, 12, 10));
        row.setStyle("-fx-background-radius: 8;");

        Label empId = new Label(String.valueOf(salaire.getUser().getId()));
        empId.setPrefWidth(60);
        empId.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #4a5568;");

        Label empName = new Label(salaire.getUser().getName());
        empName.setPrefWidth(150);
        empName.setStyle("-fx-font-size: 13px; -fx-text-fill: #2d3748;");

        Label baseSalary = new Label(String.format("%.2f DT", salaire.getBaseAmount()));
        baseSalary.setPrefWidth(90);
        baseSalary.setStyle("-fx-font-size: 13px; -fx-text-fill: #2b6cb0;");

        Label bonus = new Label(String.format("%.2f DT", salaire.getBonusAmount()));
        bonus.setPrefWidth(80);
        bonus.setStyle("-fx-font-size: 13px; -fx-text-fill: #38a169; -fx-font-weight: bold;");

        Label total = new Label(String.format("%.2f DT", salaire.getTotalAmount()));
        total.setPrefWidth(100);
        total.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #5a67d8;");

        Label status = new Label(salaire.getStatus().name());
        status.setPrefWidth(100);
        status.setAlignment(Pos.CENTER);
        status.setStyle(getStatusStyle(salaire.getStatus()));

        row.getChildren().addAll(empId, empName, baseSalary, bonus, total, status);
        return row;
    }

    /**
     * Crée une ligne personnalisée pour chaque règle de bonus avec boutons Edit/Delete
     */
    private HBox createBonusRuleRow(BonusRule rule) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10));
        row.setStyle("-fx-background-color: white; -fx-background-radius: 6; -fx-border-color: #e2e8f0; -fx-border-radius: 6;");

        Label ruleId = new Label(String.valueOf(rule.getId()));
        ruleId.setPrefWidth(60);
        ruleId.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");

        Label ruleName = new Label(rule.getNomRegle());
        ruleName.setPrefWidth(150);
        ruleName.setStyle("-fx-font-size: 12px;");

        Label percentage = new Label(String.format("%.0f%%", rule.getPercentage()));
        percentage.setPrefWidth(90);
        percentage.setStyle("-fx-font-size: 12px; -fx-text-fill: #38a169; -fx-font-weight: bold;");

        Label condition = new Label(rule.getCondition());
        condition.setPrefWidth(200);
        condition.setStyle("-fx-font-size: 11px; -fx-text-fill: #718096;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // ⭐ Vérifier si la règle est ACTIVE
        boolean isActive = rule.getStatus() == BonusRuleStatus.ACTIVE;

        // ⭐ Bouton Edit (désactivé si ACTIVE)
        Button btnEdit = new Button("Edit");
        if (isActive) {
            btnEdit.setDisable(true);
            btnEdit.setStyle("-fx-background-color: #cbd5e0; -fx-text-fill: #718096; -fx-font-size: 12px; -fx-padding: 5 10; -fx-background-radius: 5; -fx-opacity: 0.6;");
        } else {
            btnEdit.setStyle("-fx-background-color: #4299e1; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5 10; -fx-background-radius: 5; -fx-cursor: hand;");
            btnEdit.setOnAction(e -> handleEditRule(rule));
        }

        // ⭐ Bouton Delete (désactivé si ACTIVE)
        Button btnDelete = new Button("Delete");
        if (isActive) {
            btnDelete.setDisable(true);
            btnDelete.setStyle("-fx-background-color: #cbd5e0; -fx-text-fill: #718096; -fx-font-size: 12px; -fx-padding: 5 10; -fx-background-radius: 5; -fx-opacity: 0.6;");
        } else {
            btnDelete.setStyle("-fx-background-color: #f56565; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5 10; -fx-background-radius: 5; -fx-cursor: hand;");
            btnDelete.setOnAction(e -> handleDeleteRule(rule));
        }

        row.getChildren().addAll(ruleId, ruleName, percentage, condition, spacer, btnEdit, btnDelete);
        return row;
    }

    /**
     * Retourne le style CSS selon le statut du salaire
     */
    private String getStatusStyle(SalaireStatus status) {
        String baseStyle = "-fx-padding: 5 12; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold;";
        switch (status) {
            case PAYÉ:
                return baseStyle + "-fx-background-color: #48bb78; -fx-text-fill: white;";
            case EN_COURS:
                return baseStyle + "-fx-background-color: #4299e1; -fx-text-fill: white;";
            case CREÉ:
                return baseStyle + "-fx-background-color: #ed8936; -fx-text-fill: white;";
            default:
                return baseStyle + "-fx-background-color: #a0aec0; -fx-text-fill: white;";
        }
    }

    /**
     * Affiche ou cache le panneau des détails
     */
    private void showDetailsPanel(boolean show) {
        detailsContainer.setVisible(show);
        detailsContainer.setManaged(show);
    }

    /**
     * Charge tous les salaires depuis la base de données
     */
    private void loadAllSalaries() {
        try {
            salaryList.setAll(salaireService.getAll());
            salaryListView.setItems(salaryList);
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de charger les salaires: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Filtre les salaires par nom d'employé
     */
    private void filterSalaries(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            salaryListView.setItems(salaryList);
        } else {
            ObservableList<Salaire> filtered = salaryList.filtered(s ->
                    s.getUser().getName().toLowerCase().contains(searchText.toLowerCase())
            );
            salaryListView.setItems(filtered);
        }
    }

    /**
     * Affiche les détails du salaire sélectionné
     */
    private void displaySalaryDetails(Salaire salaire) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        lblEmployeeName.setText(salaire.getUser().getName());
        lblBaseAmount.setText(String.format("%.2f DT", salaire.getBaseAmount()));
        lblBonusAmount.setText(String.format("%.2f DT", salaire.getBonusAmount()));
        lblTotalAmount.setText(String.format("%.2f DT", salaire.getTotalAmount()));
        lblStatus.setText(salaire.getStatus().name());
        lblStatus.setStyle(getStatusStyle(salaire.getStatus()));
        lblDatePaiement.setText(salaire.getDatePaiement().format(formatter));

        // ⭐ Vérifier si le salaire est PAYÉ
        boolean isPaid = salaire.getStatus() == SalaireStatus.PAYÉ;

        // ⭐ Désactiver les boutons Update et Delete
        btnUpdateSalary.setDisable(isPaid);
        btnDeleteSalary.setDisable(isPaid);

        // ⭐ Désactiver le bouton Add Bonus Rule
        btnAddRule.setDisable(isPaid);

        // ⭐ Désactiver le bouton PDF si salaire PAYÉ
        btnGeneratePDF.setDisable(isPaid);


        if (isPaid) {
            btnUpdateSalary.setStyle("-fx-background-color: #cbd5e0; -fx-text-fill: #718096; -fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-opacity: 0.6;");
            btnDeleteSalary.setStyle("-fx-background-color: #cbd5e0; -fx-text-fill: #718096; -fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-opacity: 0.6;");
            btnAddRule.setStyle("-fx-background-color: #cbd5e0; -fx-text-fill: #718096; -fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-opacity: 0.6;");
            btnGeneratePDF.setStyle("-fx-background-color: #cbd5e0; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-opacity: 0.6;");
        } else {
            btnUpdateSalary.setStyle("-fx-background-color: #4299e1; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 8;");
            btnDeleteSalary.setStyle("-fx-background-color: #f56565; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 8;");
            btnAddRule.setStyle("-fx-background-color: #9f7aea; -fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 8;");
            btnGeneratePDF.setStyle("-fx-background-color: #9f7aea; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 8;");
        }
    }

    /**
     * Charge les règles de bonus depuis la base de données
     */
    private void loadBonusRules(Salaire salaire) {
        try {
            List<BonusRule> rules = bonusRuleService.getRulesBySalaire(salaire.getId());
            ObservableList<BonusRule> rulesList = FXCollections.observableArrayList(rules);
            bonusRulesListView.setItems(rulesList);
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de charger les règles de bonus: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    // ========== GESTION DES SALAIRES ==========

    /**
     * Ouvre le formulaire pour AJOUTER un nouveau salaire
     */
    @FXML
    private void handleAddSalary() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AddFormSalaire.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Ajouter un Salaire");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadAllSalaries();

        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    /**
     * Ouvre le formulaire pour MODIFIER le salaire sélectionné
     */
    @FXML
    private void handleUpdateSalary() {
        if (selectedSalaire != null) {
            // ⭐ Vérifier si le salaire est PAYÉ
            if (selectedSalaire.getStatus() == SalaireStatus.PAYÉ) {
                showAlert("Action refusée", "Un salaire PAYÉ ne peut pas être modifié.", Alert.AlertType.WARNING);
                return;
            }

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/UpdateFormSalaire.fxml"));
                Parent root = loader.load();

                UpdateFormSalaireController controller = loader.getController();
                controller.setSalaire(selectedSalaire);

                Stage stage = new Stage();
                stage.setTitle("Modifier le Salaire");
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setScene(new Scene(root));
                stage.showAndWait();

                loadAllSalaries();

                Salaire updatedSalaire = salaireService.getById(selectedSalaire.getId());
                if (updatedSalaire != null) {
                    selectedSalaire = updatedSalaire;
                    displaySalaryDetails(updatedSalaire);
                }

            } catch (IOException e) {
                showAlert("Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        } else {
            showAlert("Attention", "Veuillez d'abord sélectionner un salaire", Alert.AlertType.WARNING);
        }
    }

    /**
     * Supprime le salaire sélectionné
     * ⭐ Vérification : Impossible si le salaire est PAYÉ
     */
    @FXML
    private void handleDeleteSalary() {
        if (selectedSalaire != null) {
            // ⭐ Vérifier si le salaire est PAYÉ
            if (selectedSalaire.getStatus() == SalaireStatus.PAYÉ) {
                showAlert("Action refusée", "Un salaire PAYÉ ne peut pas être supprimé.", Alert.AlertType.WARNING);
                return;
            }

            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirmation");
            confirmation.setHeaderText("Supprimer le salaire de " + selectedSalaire.getUser().getName());
            confirmation.setContentText("Cette action est irréversible. Continuer ?");

            confirmation.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        salaireService.delete(selectedSalaire.getId());
                        showAlert("Succès", "Salaire supprimé avec succès", Alert.AlertType.INFORMATION);
                        loadAllSalaries();
                        showDetailsPanel(false);
                        selectedSalaire = null;
                    } catch (Exception e) {
                        showAlert("Erreur", "Erreur lors de la suppression: " + e.getMessage(), Alert.AlertType.ERROR);
                    }
                }
            });
        } else {
            showAlert("Attention", "Veuillez d'abord sélectionner un salaire", Alert.AlertType.WARNING);
        }
    }


    /**
     * Gère le clic sur le bouton "Générer PDF"
     */
    @FXML
    private void handleGeneratePDF() {
        if (selectedSalaire == null) {
            showAlert("Erreur", "Veuillez sélectionner un salaire", Alert.AlertType.WARNING);
            return;
        }

        try {
            // Générer le PDF
            String pdfPath = pdfService.generatePayslip(selectedSalaire);

            if (pdfPath != null) {
                showAlert(
                        "Succès",
                        "Fiche de paie générée avec succès !\n\nFichier : " + pdfPath,
                        Alert.AlertType.INFORMATION
                );
            } else {
                showAlert(
                        "Erreur",
                        "Erreur lors de la génération du PDF",
                        Alert.AlertType.ERROR
                );
            }

        } catch (Exception e) {
            showAlert(
                    "Erreur",
                    "Erreur lors de la génération du PDF : " + e.getMessage(),
                    Alert.AlertType.ERROR
            );
            e.printStackTrace();
        }
    }

    // ========== GESTION DES RÈGLES DE BONUS ==========

    /**
     * Ouvre le formulaire pour AJOUTER une règle de bonus
     */
    @FXML
    private void handleAddRule() {
        if (selectedSalaire != null) {
            // ⭐ Vérifier si le salaire est PAYÉ
            if (selectedSalaire.getStatus() == SalaireStatus.PAYÉ) {
                showAlert("Action refusée",
                        "Impossible d'ajouter une règle de bonus à un salaire PAYÉ.",
                        Alert.AlertType.WARNING);
                return;
            }

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/AddFormBonusRule.fxml"));
                Parent root = loader.load();

                AddFormBonusRuleController controller = loader.getController();
                controller.setSalaire(selectedSalaire);

                Stage stage = new Stage();
                stage.setTitle("Ajouter une Règle de Bonus");
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setScene(new Scene(root));
                stage.showAndWait();

                // Rafraîchir les règles
                loadBonusRules(selectedSalaire);

            } catch (IOException e) {
                showAlert("Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        } else {
            showAlert("Attention", "Veuillez d'abord sélectionner un salaire", Alert.AlertType.WARNING);
        }
    }

    /**
     * Ouvre le formulaire pour MODIFIER une règle de bonus spécifique
     */
    private void handleEditRule(BonusRule rule) {
        // ⭐ Vérifier si la règle est ACTIVE
        if (rule.getStatus() == BonusRuleStatus.ACTIVE) {
            showAlert("Action refusée", "Une règle de bonus ACTIVE ne peut pas être modifiée.", Alert.AlertType.WARNING);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UpdateFormBonusRule.fxml"));
            Parent root = loader.load();

            UpdateFormBonusRuleController controller = loader.getController();
            controller.setBonusRule(rule);

            Stage stage = new Stage();
            stage.setTitle("Modifier la Règle de Bonus");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadAllSalaries();

            Salaire updatedSalaire = salaireService.getById(selectedSalaire.getId());
            if (updatedSalaire != null) {
                selectedSalaire = updatedSalaire;
                displaySalaryDetails(updatedSalaire);
                loadBonusRules(updatedSalaire);
            }

        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    /**
     * Supprime une règle de bonus spécifique
     * ⭐ Vérification : Impossible si la règle est ACTIVE
     */
    private void handleDeleteRule(BonusRule rule) {
        // ⭐ Vérifier si la règle est ACTIVE
        if (rule.getStatus() == BonusRuleStatus.ACTIVE) {
            showAlert("Action refusée", "Une règle de bonus ACTIVE ne peut pas être supprimée.", Alert.AlertType.WARNING);
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Supprimer Règle");
        confirmation.setHeaderText("Supprimer: " + rule.getNomRegle());
        confirmation.setContentText("Cette action est irréversible. Continuer ?");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    bonusRuleService.delete(rule.getId());
                    showAlert("Succès", "Règle supprimée avec succès", Alert.AlertType.INFORMATION);
                    loadBonusRules(selectedSalaire);
                } catch (Exception e) {
                    showAlert("Erreur", "Erreur lors de la suppression: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    /**
     * Affiche une boîte de dialogue d'alerte
     */
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}