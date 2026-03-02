package org.example.controllers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import org.example.enums.UserRole;
import org.example.model.user.UserAccount;
import org.example.model.user.UserSettings;
import org.example.services.user.UserAccountService;
import org.example.services.user.UserSettingsService;
import org.example.util.PdfExporter;
import org.example.util.SessionManager;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class UserListController {

    @FXML private Label userNameLabel;
    @FXML private Button logoutButton;

    @FXML private Button navDashboard;
    @FXML private Button navUsers;
    @FXML private Button navFormations;
    @FXML private Button navProjets;
    @FXML private Button navRecrutement;
    @FXML private Button navSalaire;

    @FXML private ListView<UserAccount> userListView;
    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button settingsButton;
    @FXML private Button refreshButton;
    @FXML private Button exportPdfButton;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> roleFilterCombo;
    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private Button clearFilterButton;

    private UserAccountService userService = new UserAccountService();
    private UserSettingsService settingsService = new UserSettingsService();
    private ObservableList<UserAccount> masterData = FXCollections.observableArrayList();
    private FilteredList<UserAccount> filteredData;
    private UserAccount loggedInUser;

    public void setLoggedInUser(UserAccount user) {
        this.loggedInUser = user;
        // ✅ FIX : Toujours synchroniser SessionManager
        SessionManager.setCurrentUser(user);
        userNameLabel.setText(user.getUsername() + " (" + user.getRole() + ")");

        // Vérifier le rôle : si employé, rediriger vers dashboard
        if (user.getRole() != UserRole.ADMINISTRATEUR && user.getRole() != UserRole.MANAGER) {
            Platform.runLater(() -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/Dashboard.fxml"));
                    Parent root = loader.load();
                    DashboardController dashboardController = loader.getController();
                    dashboardController.setLoggedInUser(user);

                    Stage stage = (Stage) userListView.getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.setTitle("Mon Tableau de Bord");
                    stage.setMaximized(true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            return;
        }

        if (userListView.getScene() != null) {
            loadUserSettings();
        } else {
            userListView.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    loadUserSettings();
                }
            });
        }
    }

    @FXML
    public void initialize() {
        userListView.setCellFactory(lv -> new ListCell<UserAccount>() {
            @Override
            protected void updateItem(UserAccount user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label nameLabel = new Label(user.getUsername());
                    nameLabel.getStyleClass().add("name-label");

                    Label emailLabel = new Label(user.getEmail());
                    emailLabel.getStyleClass().add("email-label");

                    Label roleLabel = new Label(user.getRole().toString());
                    roleLabel.getStyleClass().add("role-label");

                    Label statusLabel = new Label(user.getAccountStatus());
                    statusLabel.getStyleClass().addAll("status-label",
                            "ACTIVE".equals(user.getAccountStatus()) ? "status-active" : "status-inactive");

                    VBox vbox = new VBox(5, nameLabel, emailLabel);
                    HBox hbox = new HBox(10, vbox, roleLabel, statusLabel);
                    hbox.getStyleClass().add("user-cell");
                    setGraphic(hbox);
                }
            }
        });

        loadUsers();


        setupFilters();

        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilter());
        roleFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilter());
        statusFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilter());

        clearFilterButton.setOnAction(event -> clearFilters());
        exportPdfButton.setOnAction(event -> exportToPdf());

        navDashboard.setOnAction(e -> openDashboard());
        navUsers.setOnAction(e -> {});
        navFormations.setOnAction(e -> showComingSoon("Formations"));
        navProjets.setOnAction(e -> showComingSoon("Projets"));
        navRecrutement.setOnAction(e -> showComingSoon("Recrutement"));
        navSalaire.setOnAction(e -> openSalaireManagement());

        addButton.setOnAction(event -> openUserForm(null));

        editButton.setOnAction(event -> {
            UserAccount selected = userListView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un utilisateur.");
                return;
            }
            if (loggedInUser.getRole() == UserRole.ADMINISTRATEUR || selected.getUserId() == loggedInUser.getUserId()) {
                openUserForm(selected);
            } else {
                showAlert(Alert.AlertType.ERROR, "Accès refusé", "Vous ne pouvez modifier que votre propre compte.");
            }
        });

        deleteButton.setOnAction(event -> {
            UserAccount selected = userListView.getSelectionModel().getSelectedItem();
            if (selected != null) deleteUser(selected);
            else showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un utilisateur.");
        });

        settingsButton.setOnAction(event -> {
            UserAccount selected = userListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                if (loggedInUser.getRole() == UserRole.ADMINISTRATEUR || selected.getUserId() == loggedInUser.getUserId()) {
                    openSettings(selected);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Accès refusé",
                            "Vous n'avez pas les droits pour modifier les paramètres de cet utilisateur.");
                }
            } else {
                showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un utilisateur.");
            }
        });

        refreshButton.setOnAction(event -> loadUsers());
        logoutButton.setOnAction(event -> logout());
    }

    private void setupFilters() {
        List<String> roles = Arrays.stream(UserRole.values())
                .map(Enum::name)
                .collect(Collectors.toList());
        roles.add(0, "Tous");
        roleFilterCombo.setItems(FXCollections.observableArrayList(roles));
        roleFilterCombo.getSelectionModel().select("Tous");

        List<String> statuses = masterData.stream()
                .map(UserAccount::getAccountStatus)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        statuses.add(0, "Tous");
        statusFilterCombo.setItems(FXCollections.observableArrayList(statuses));
        statusFilterCombo.getSelectionModel().select("Tous");
    }

    private void applyFilter() {
        String searchText = searchField.getText().toLowerCase().trim();
        String selectedRole = roleFilterCombo.getSelectionModel().getSelectedItem();
        String selectedStatus = statusFilterCombo.getSelectionModel().getSelectedItem();

        Predicate<UserAccount> predicate = user -> {
            if (!searchText.isEmpty()) {
                boolean matchesSearch = user.getUsername().toLowerCase().contains(searchText) ||
                        user.getEmail().toLowerCase().contains(searchText);
                if (!matchesSearch) return false;
            }
            if (selectedRole != null && !"Tous".equals(selectedRole)) {
                if (!user.getRole().name().equals(selectedRole)) return false;
            }
            if (selectedStatus != null && !"Tous".equals(selectedStatus)) {
                if (!user.getAccountStatus().equals(selectedStatus)) return false;
            }
            return true;
        };

        filteredData.setPredicate(predicate);
    }

    private void clearFilters() {
        searchField.clear();
        roleFilterCombo.getSelectionModel().select("Tous");
        statusFilterCombo.getSelectionModel().select("Tous");
    }

    private void exportToPdf() {
        List<UserAccount> usersToExport = filteredData.stream().collect(Collectors.toList());
        if (usersToExport.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Aucune donnée", "Aucun utilisateur à exporter.");
            return;
        }

        String downloadsPath = System.getProperty("user.home") + "/Downloads/";
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = "utilisateurs_" + timestamp + ".pdf";
        String filePath = downloadsPath + filename;

        try {
            PdfExporter.exportUsersToPdf(usersToExport, filePath);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Export PDF terminé !\nFichier : " + filePath);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de l'export PDF : " + e.getMessage());
        }
    }

    private void openDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Dashboard.fxml"));
            Parent root = loader.load();
            DashboardController dashboardController = loader.getController();
            dashboardController.setLoggedInUser(loggedInUser); // ✅ passer le user

            Stage stage = (Stage) navDashboard.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Mon Tableau de Bord");
            stage.setMaximized(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openSalaireManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/SalaireManagement.fxml"));
            Parent root = loader.load();

            // ✅ FIX : Passer l'utilisateur au contrôleur SalaireManagement
            SalaireManagementController controller = loader.getController();
            controller.setLoggedInUser(loggedInUser);

            Stage stage = (Stage) navSalaire.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("INTEGRA - Gestion Salaires");
            stage.setMaximized(true);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'ouvrir la gestion des salaires : " + e.getMessage());
        }
    }

    private void loadUsers() {
        try {
            List<UserAccount> users = userService.getAll();
            masterData.setAll(users);

            // ✅ Initialiser filteredData ici
            filteredData = new FilteredList<>(masterData, p -> true);
            userListView.setItems(filteredData);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les utilisateurs.");
        }
    }

    private void loadUserSettings() {
        if (loggedInUser == null) return;
        try {
            UserSettings settings = settingsService.getSettingsByUserId(loggedInUser.getUserId());
            String theme = (settings != null && settings.getTheme() != null) ? settings.getTheme() : "clair";
            applyTheme(theme);
        } catch (SQLException e) {
            e.printStackTrace();
            applyTheme("clair");
        }
    }

    private void applyTheme(String theme) {
        Scene scene = userListView.getScene();
        if (scene == null) return;
        scene.getRoot().getStyleClass().removeAll("theme-light", "theme-dark");
        if ("sombre".equals(theme)) {
            scene.getRoot().getStyleClass().add("theme-dark");
        } else {
            scene.getRoot().getStyleClass().add("theme-light");
        }
    }

    private void openUserForm(UserAccount user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/SignUp.fxml"));
            Parent root = loader.load();
            SignUpController controller = loader.getController();
            controller.setUserToEdit(user);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle(user == null ? "Ajouter un utilisateur" : "Modifier un utilisateur");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            loadUsers();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteUser(UserAccount user) {
        if (loggedInUser.getRole() != UserRole.ADMINISTRATEUR) {
            showAlert(Alert.AlertType.ERROR, "Accès refusé", "Seul l'administrateur peut supprimer un utilisateur.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer " + user.getUsername() + " ?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    userService.delete(user.getUserId());
                    loadUsers();
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la suppression.");
                }
            }
        });
    }

    private void openSettings(UserAccount user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UserSettings.fxml"));
            Parent root = loader.load();
            UserSettingsController controller = loader.getController();
            controller.setUser(user);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Paramètres de " + user.getUsername());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            if (user.getUserId() == loggedInUser.getUserId()) {
                loadUserSettings();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showComingSoon(String module) {
        showAlert(Alert.AlertType.INFORMATION, "Bientôt disponible",
                "Le module " + module + " sera implémenté prochainement.");
    }

    private void logout() {
        SessionManager.logout();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Login.fxml"));
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Connexion");
            stage.setMaximized(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void switchSceneWithMaximize(Stage stage, Parent root, String title) {
        stage.setScene(new Scene(root));
        stage.setTitle(title);
        stage.addEventHandler(WindowEvent.WINDOW_SHOWN, e -> {
            Platform.runLater(() -> stage.setMaximized(true));
        });
        stage.show();
        Timeline fallback = new Timeline(new KeyFrame(Duration.millis(100), e -> {
            if (!stage.isMaximized()) {
                stage.setMaximized(true);
            }
        }));
        fallback.setCycleCount(1);
        fallback.play();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}