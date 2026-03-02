package org.example.Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.enums.BonusRuleStatus;
import org.example.enums.SalaireStatus;
import org.example.enums.UserRole;
import org.example.model.salaire.BonusRule;
import org.example.model.salaire.Salaire;
import org.example.model.user.UserAccount;
import org.example.services.pdf.PDFService;
import org.example.services.salaire.BonusRuleService;
import org.example.services.salaire.SalaireService;
import org.example.util.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;

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

    @FXML private Button btnGeneratePDF;
    @FXML private Button btnExportExcel;

    @FXML private Button btnNavSalaires;
    @FXML private Button btnNavStatistics;
    @FXML private Label lblCurrentUser;
    @FXML private Label lblCurrentRole;
    @FXML private Label lblModuleTitle;
    @FXML private Button btnNavHome;

    private SalaireService salaireService;
    private BonusRuleService bonusRuleService;
    private PDFService pdfService;
    private ObservableList<Salaire> salaryList;
    private Salaire selectedSalaire;

    // ✅ FIX : Méthode setter pour recevoir l'utilisateur depuis les autres contrôleurs
    public void setLoggedInUser(UserAccount user) {
        if (user != null) {
            SessionManager.setCurrentUser(user);
        }
        // Rafraîchir l'affichage après avoir défini l'utilisateur
        displayCurrentUserInfo();
        adaptInterfaceToRole();
    }

    @FXML
    public void initialize() {
        // ✅ FIX : Initialiser les services EN PREMIER avant tout appel UI
        salaireService = new SalaireService();
        bonusRuleService = new BonusRuleService();
        pdfService = new PDFService();
        salaryList = FXCollections.observableArrayList();

        // ✅ FIX : Afficher les infos utilisateur et adapter l'interface
        // (sera rappelé dans setLoggedInUser si le user arrive plus tard)
        displayCurrentUserInfo();
        adaptInterfaceToRole();

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

        // Listener sélection salaire
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

        loadAllSalaries();
    }

    private void displayCurrentUserInfo() {
        // ✅ FIX : Vérifier que les labels sont injectés ET que la session est active
        if (lblCurrentUser == null || lblCurrentRole == null || lblModuleTitle == null) {
            System.out.println("⚠️ Labels non injectés (vérifier fx:id dans FXML)");
            return;
        }

        if (SessionManager.isLoggedIn()) {
            UserAccount currentUser = SessionManager.getCurrentUser();
            UserRole currentRole = SessionManager.getCurrentRole();

            lblCurrentUser.setText(currentUser.getUsername());
            lblCurrentRole.setText(currentRole.toString());

            if (SessionManager.isAdmin()) {
                lblModuleTitle.setText("Administrator / RH Module");
            } else if (SessionManager.isManager()) {
                lblModuleTitle.setText("Manager / Consultation Salaires");
            } else {
                lblModuleTitle.setText("Consultation de mes salaires");
            }
        } else {
            lblCurrentUser.setText("Non connecté");
            lblCurrentRole.setText("-");
            lblModuleTitle.setText("Mode Test");
        }
    }

    private void adaptInterfaceToRole() {
        // ✅ FIX : Vérification null sur getCurrentUser() pour éviter NullPointerException
        if (!SessionManager.isLoggedIn() || SessionManager.getCurrentUser() == null) {
            // Sécurité totale si pas de session
            if (btnAddSalary != null)       btnAddSalary.setVisible(false);
            if (btnUpdateSalary != null)    btnUpdateSalary.setDisable(true);
            if (btnDeleteSalary != null)    btnDeleteSalary.setDisable(true);
            if (btnAddRule != null)         btnAddRule.setDisable(true);
            if (btnExportExcel != null)     btnExportExcel.setVisible(false);
            if (btnNavStatistics != null) {
                btnNavStatistics.setVisible(false);
                btnNavStatistics.setManaged(false);
            }
            if (btnGeneratePDF != null)     btnGeneratePDF.setDisable(true);
            return;
        }

        String role = SessionManager.getCurrentUser().getRole().name();

        switch (role) {
            case "ADMINISTRATEUR" -> {
                btnAddSalary.setVisible(true);
                btnUpdateSalary.setDisable(false);
                btnDeleteSalary.setDisable(false);
                btnAddRule.setDisable(false);
                btnExportExcel.setVisible(true);
                btnNavStatistics.setVisible(true);
                btnNavStatistics.setManaged(true);
                btnGeneratePDF.setDisable(false);
            }
            case "MANAGER" -> {
                btnAddSalary.setVisible(false);
                btnUpdateSalary.setDisable(true);   // ✅ Manager ne peut pas modifier
                btnDeleteSalary.setDisable(true);   // ✅ Manager ne peut pas supprimer
                btnAddRule.setDisable(true);
                btnExportExcel.setVisible(true);
                btnNavStatistics.setVisible(true);
                btnNavStatistics.setManaged(true);
                btnGeneratePDF.setDisable(false);
            }
            case "EMPLOYE" -> {
                btnAddSalary.setVisible(false);
                btnUpdateSalary.setDisable(true);
                btnDeleteSalary.setDisable(true);
                btnAddRule.setDisable(true);
                btnExportExcel.setVisible(false);
                btnNavStatistics.setVisible(false);
                btnNavStatistics.setManaged(false);
                btnGeneratePDF.setDisable(false);
            }
            default -> {
                btnAddSalary.setVisible(false);
                btnUpdateSalary.setDisable(true);
                btnDeleteSalary.setDisable(true);
                btnAddRule.setDisable(true);
                btnExportExcel.setVisible(false);
                btnNavStatistics.setVisible(false);
                btnNavStatistics.setManaged(false);
                btnGeneratePDF.setDisable(true);
            }
        }

        adaptBonusRuleButtons(role);
    }

    private void adaptBonusRuleButtons(String role) {
        if (!"ADMINISTRATEUR".equals(role)) {
            bonusRulesListView.setCellFactory(lv -> new ListCell<BonusRule>() {
                @Override
                protected void updateItem(BonusRule item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setGraphic(null);
                    } else {
                        HBox container = new HBox(10);
                        container.setAlignment(Pos.CENTER_LEFT);
                        container.setPadding(new Insets(10));

                        Label lblName = new Label(item.getNomRegle());
                        lblName.setPrefWidth(150);
                        Label lblPct = new Label(item.getPercentage() + "%");
                        lblPct.setPrefWidth(90);
                        Label lblCond = new Label(item.getCondition());
                        lblCond.setPrefWidth(200);

                        Button btnEdit = new Button("Edit");
                        btnEdit.setDisable(true);
                        Button btnDelete = new Button("Delete");
                        btnDelete.setDisable(true);

                        container.getChildren().addAll(lblName, lblPct, lblCond, btnEdit, btnDelete);
                        setGraphic(container);
                    }
                }
            });
        }
    }

    // ✅ FIX : Retour vers la page d'accueil avec passage correct de l'utilisateur
    @FXML
    private void handleNavHome() {
        try {
            if (!SessionManager.isLoggedIn() || SessionManager.getCurrentUser() == null) {
                // Rediriger vers Login si session perdue
                Parent root = FXMLLoader.load(getClass().getResource("/Login.fxml"));
                Stage stage = (Stage) btnNavHome.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Connexion");
                stage.setMaximized(false);
                return;
            }

            UserAccount currentUser = SessionManager.getCurrentUser();
            UserRole role = currentUser.getRole();

            if (role == UserRole.ADMINISTRATEUR || role == UserRole.MANAGER) {
                // ✅ FIX : Charger UserList ET passer l'utilisateur
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/UserList.fxml"));
                Parent root = loader.load();
                UserListController ctrl = loader.getController();
                ctrl.setLoggedInUser(currentUser); // ✅ passer le user

                Stage stage = (Stage) btnNavHome.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Gestion des utilisateurs");
                stage.setMaximized(true);

            } else {
                // ✅ FIX : Charger Dashboard ET passer l'utilisateur
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Dashboard.fxml"));
                Parent root = loader.load();
                DashboardController ctrl = loader.getController();
                ctrl.setLoggedInUser(currentUser); // ✅ passer le user

                Stage stage = (Stage) btnNavHome.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Mon Tableau de Bord");
                stage.setMaximized(true);
            }

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la page: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private HBox createSalaryRow(Salaire salaire) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 10, 12, 10));
        row.setStyle("-fx-background-radius: 8;");

        Label empId = new Label(String.valueOf(salaire.getUser().getUserId()));
        empId.setPrefWidth(60);
        empId.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #4a5568;");

        Label empName = new Label(salaire.getUser().getUsername());
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

        boolean isActive = rule.getStatus() == BonusRuleStatus.ACTIVE;

        Button btnEdit = new Button("Edit");
        if (isActive) {
            btnEdit.setDisable(true);
            btnEdit.setStyle("-fx-background-color: #cbd5e0; -fx-text-fill: #718096; -fx-font-size: 12px; -fx-padding: 5 10; -fx-background-radius: 5; -fx-opacity: 0.6;");
        } else {
            btnEdit.setStyle("-fx-background-color: #4299e1; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5 10; -fx-background-radius: 5; -fx-cursor: hand;");
            btnEdit.setOnAction(e -> handleEditRule(rule));
        }

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

    private String getStatusStyle(SalaireStatus status) {
        String baseStyle = "-fx-padding: 5 12; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold;";
        switch (status) {
            case PAYÉ:      return baseStyle + "-fx-background-color: #48bb78; -fx-text-fill: white;";
            case EN_COURS:  return baseStyle + "-fx-background-color: #4299e1; -fx-text-fill: white;";
            case CREÉ:      return baseStyle + "-fx-background-color: #ed8936; -fx-text-fill: white;";
            default:        return baseStyle + "-fx-background-color: #a0aec0; -fx-text-fill: white;";
        }
    }

    private void showDetailsPanel(boolean show) {
        detailsContainer.setVisible(show);
        detailsContainer.setManaged(show);
    }

    private void loadAllSalaries() {
        try {
            salaryList.setAll(salaireService.getAll());
            salaryListView.setItems(salaryList);
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de charger les salaires: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void filterSalaries(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            salaryListView.setItems(salaryList);
        } else {
            ObservableList<Salaire> filtered = salaryList.filtered(s ->
                    s.getUser().getUsername().toLowerCase().contains(searchText.toLowerCase())
            );
            salaryListView.setItems(filtered);
        }
    }

    // ✅ FIX : displaySalaryDetails tient compte du rôle pour ne pas réactiver les boutons
    private void displaySalaryDetails(Salaire salaire) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        lblEmployeeName.setText(salaire.getUser().getUsername());
        lblBaseAmount.setText(String.format("%.2f DT", salaire.getBaseAmount()));
        lblBonusAmount.setText(String.format("%.2f DT", salaire.getBonusAmount()));
        lblTotalAmount.setText(String.format("%.2f DT", salaire.getTotalAmount()));
        lblStatus.setText(salaire.getStatus().name());
        lblStatus.setStyle(getStatusStyle(salaire.getStatus()));
        lblDatePaiement.setText(salaire.getDatePaiement().format(formatter));

        boolean isPaid = salaire.getStatus() == SalaireStatus.PAYÉ;

        // ✅ FIX : Combiner la contrainte du statut ET du rôle
        boolean isAdmin = SessionManager.isAdmin();

        // Update/Delete/AddRule : seulement si Admin ET salaire non payé
        btnUpdateSalary.setDisable(isPaid || !isAdmin);
        btnDeleteSalary.setDisable(isPaid || !isAdmin);
        btnAddRule.setDisable(isPaid || !isAdmin);

        // PDF : disponible pour tous si salaire non payé
        btnGeneratePDF.setDisable(isPaid);

        // Styles
        if (isPaid || !isAdmin) {
            String disabledStyle = "-fx-background-color: #cbd5e0; -fx-text-fill: #718096; -fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-opacity: 0.6;";
            if (isPaid || !isAdmin) btnUpdateSalary.setStyle(disabledStyle);
            if (isPaid || !isAdmin) btnDeleteSalary.setStyle(disabledStyle);
            if (isPaid || !isAdmin) btnAddRule.setStyle(disabledStyle);
        }

        if (!isPaid && isAdmin) {
            btnUpdateSalary.setStyle("-fx-background-color: #4299e1; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 8;");
            btnDeleteSalary.setStyle("-fx-background-color: #f56565; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 8;");
            btnAddRule.setStyle("-fx-background-color: #9f7aea; -fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 8;");
        }

        if (!isPaid) {
            btnGeneratePDF.setStyle("-fx-background-color: #9f7aea; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 8;");
        } else {
            btnGeneratePDF.setStyle("-fx-background-color: #cbd5e0; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-opacity: 0.6;");
        }
    }

    @FXML
    private void handleExportExcel() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ExportExcelDialog.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Export Excel - Configuration");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir la fenêtre d'export: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

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

    @FXML
    private void handleUpdateSalary() {
        if (selectedSalaire == null) {
            showAlert("Attention", "Veuillez d'abord sélectionner un salaire", Alert.AlertType.WARNING);
            return;
        }
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
    }

    @FXML
    private void handleDeleteSalary() {
        if (selectedSalaire == null) {
            showAlert("Attention", "Veuillez d'abord sélectionner un salaire", Alert.AlertType.WARNING);
            return;
        }
        if (selectedSalaire.getStatus() == SalaireStatus.PAYÉ) {
            showAlert("Action refusée", "Un salaire PAYÉ ne peut pas être supprimé.", Alert.AlertType.WARNING);
            return;
        }
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Supprimer le salaire de " + selectedSalaire.getUser().getUsername());
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
    }

    @FXML
    private void handleGeneratePDF() {
        if (selectedSalaire == null) {
            showAlert("Erreur", "Veuillez sélectionner un salaire", Alert.AlertType.WARNING);
            return;
        }
        if (!SessionManager.isAdmin()) {
            if (selectedSalaire.getUser().getUserId() != SessionManager.getCurrentUser().getUserId()) {
                showAlert("Accès refusé",
                        "Vous ne pouvez générer que vos propres fiches de paie",
                        Alert.AlertType.ERROR);
                return;
            }
        }
        try {
            Salaire salaireComplet = salaireService.getById(selectedSalaire.getId());
            if (salaireComplet == null) {
                showAlert("Erreur", "Impossible de charger les données du salaire", Alert.AlertType.ERROR);
                return;
            }
            String pdfPath = pdfService.generatePayslip(salaireComplet);
            if (pdfPath != null) {
                showAlert("Succès", "Fiche de paie générée !\nFichier : " + pdfPath, Alert.AlertType.INFORMATION);
            } else {
                showAlert("Erreur", "Erreur lors de la génération du PDF", Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            showAlert("Erreur", "Erreur : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddRule() {
        if (selectedSalaire == null) {
            showAlert("Attention", "Veuillez d'abord sélectionner un salaire", Alert.AlertType.WARNING);
            return;
        }
        if (selectedSalaire.getStatus() == SalaireStatus.PAYÉ) {
            showAlert("Action refusée",
                    "Impossible d'ajouter une règle de bonus à un salaire PAYÉ.",
                    Alert.AlertType.WARNING);
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AddFormBonusRule.fxml"));
            Parent root = loader.load();
            org.example.controllers.AddFormBonusRuleController controller = loader.getController();
            controller.setSalaire(selectedSalaire);
            Stage stage = new Stage();
            stage.setTitle("Ajouter une Règle de Bonus");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            loadBonusRules(selectedSalaire);
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void handleEditRule(BonusRule rule) {
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

    private void handleDeleteRule(BonusRule rule) {
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

    @FXML
    private void handleNavSalaires() {
        System.out.println("Déjà sur Gestion Salaires");
    }

    @FXML
    private void handleNavStatistics() {
        if (!SessionManager.isAdmin() && !SessionManager.isManager()) {
            showAlert("Accès refusé",
                    "Seuls les administrateurs peuvent accéder aux statistiques.",
                    Alert.AlertType.ERROR);
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/StatisticsView.fxml"));
            Parent root = loader.load();
            Stage currentStage = (Stage) btnNavStatistics.getScene().getWindow();
            currentStage.setScene(new Scene(root));
            currentStage.setTitle("INTEGRA - Statistiques & IA");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir les statistiques", Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}