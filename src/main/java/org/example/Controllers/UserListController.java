package org.example.Controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.enums.UserRole;
import org.example.model.user.UserAccount;
import org.example.model.user.UserSettings;
import org.example.services.user.UserAccountService;
import org.example.services.user.UserSettingsService;
import org.example.util.SessionManager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class UserListController {

    @FXML private Label userNameLabel;
    @FXML private Button logoutButton;

    @FXML private Button navDashboard;
    @FXML private Button navUsers;
    @FXML private Button navFormations; // Le bouton que vous cliquez sur l'image
    @FXML private Button navProjets;
    @FXML private Button navRecrutement;
    @FXML private Button navSalaire;

    @FXML private ListView<UserAccount> userListView;
    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button settingsButton;
    @FXML private Button refreshButton;

    private UserAccountService userService = new UserAccountService();
    private UserSettingsService settingsService = new UserSettingsService();
    private ObservableList<UserAccount> userList = FXCollections.observableArrayList();
    private UserAccount loggedInUser;

    public void setLoggedInUser(UserAccount user) {
        this.loggedInUser = user;
        SessionManager.setCurrentUser(user);
        userNameLabel.setText(user.getUsername() + " (" + user.getRole() + ")");

        if (user.getRole() != UserRole.ADMINISTRATEUR && user.getRole() != UserRole.MANAGER) {
            Platform.runLater(this::openDashboard);
            return;
        }

        if (userListView.getScene() != null) {
            loadUserSettings();
        } else {
            userListView.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) loadUserSettings();
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

        // --- NAVIGATION ---
        navDashboard.setOnAction(e -> openDashboard());
        navUsers.setOnAction(e -> {}); // On est déjà dessus

        // MODIFICATION ICI : On appelle la méthode au lieu de l'alerte
        navFormations.setOnAction(e -> openManageTraining());

        navProjets.setOnAction(e -> showComingSoon("Projets"));
        navRecrutement.setOnAction(e -> showComingSoon("Recrutement"));
        navSalaire.setOnAction(e -> openSalaireManagement());

        // --- ACTIONS BOUTONS ---
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
                showAlert(Alert.AlertType.ERROR, "Accès refusé", "Droit de modification restreint.");
            }
        });

        deleteButton.setOnAction(event -> {
            UserAccount selected = userListView.getSelectionModel().getSelectedItem();
            if (selected != null) deleteUser(selected);
            else showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un utilisateur.");
        });

        settingsButton.setOnAction(event -> {
            UserAccount selected = userListView.getSelectionModel().getSelectedItem();
            if (selected != null) openSettings(selected);
        });

        refreshButton.setOnAction(event -> loadUsers());
        logoutButton.setOnAction(event -> logout());
    }

    // NOUVELLE MÉTHODE POUR OUVRIR LES FORMATIONS
    private void openManageTraining() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ManageTraining.fxml"));
            Parent root = loader.load();

            ManageTrainingController controller = loader.getController();
            controller.setLoggedInUser(loggedInUser);

            Stage stage = (Stage) navFormations.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("INTEGRA - Gestion des Formations");
            stage.setMaximized(true);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger le module Formation.");
        }
    }

    private void openDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Dashboard.fxml"));
            Parent root = loader.load();
            DashboardController dashboardController = loader.getController();
            dashboardController.setLoggedInUser(loggedInUser);

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
            SalaireManagementController controller = loader.getController();
            controller.setLoggedInUser(loggedInUser);

            Stage stage = (Stage) navSalaire.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("INTEGRA - Gestion Salaires");
            stage.setMaximized(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadUsers() {
        try {
            List<UserAccount> users = userService.getAll();
            userList.setAll(users);
            userListView.setItems(userList);
        } catch (Exception e) {
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
            applyTheme("clair");
        }
    }

    private void applyTheme(String theme) {
        Scene scene = userListView.getScene();
        if (scene == null) return;
        scene.getRoot().getStyleClass().removeAll("theme-light", "theme-dark");
        if ("sombre".equals(theme)) scene.getRoot().getStyleClass().add("theme-dark");
        else scene.getRoot().getStyleClass().add("theme-light");
    }

    private void openUserForm(UserAccount user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/SignUp.fxml"));
            Parent root = loader.load();
            SignUpController controller = loader.getController();
            controller.setUserToEdit(user);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            loadUsers();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteUser(UserAccount user) {
        if (loggedInUser.getRole() != UserRole.ADMINISTRATEUR) {
            showAlert(Alert.AlertType.ERROR, "Accès refusé", "Action réservée à l'administrateur.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer " + user.getUsername() + " ?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    userService.delete(user.getUserId());
                    loadUsers();
                } catch (Exception e) {
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
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            if (user.getUserId() == loggedInUser.getUserId()) loadUserSettings();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showComingSoon(String module) {
        showAlert(Alert.AlertType.INFORMATION, "Bientôt disponible", "Le module " + module + " sera implémenté prochainement.");
    }

    private void logout() {
        SessionManager.logout();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Login.fxml"));
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}