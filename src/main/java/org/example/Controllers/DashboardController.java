package org.example.Controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.enums.UserRole;
import org.example.model.user.UserAccount;
import org.example.model.user.UserSettings;
import org.example.services.user.UserSettingsService;

import java.io.IOException;
import java.sql.SQLException;

public class DashboardController {

    @FXML private Label userNameLabel;
    @FXML private Button logoutButton;
    @FXML private Label usernameDisplay;
    @FXML private Label emailDisplay;
    @FXML private Label roleDisplay;
    @FXML private Label statusDisplay;
    @FXML private Button settingsButton;
    @FXML private Button formationsButton;
    @FXML private Button userListButton;

    // Boutons de navigation sidebar
    @FXML private Button navDashboard;
    @FXML private Button navFormations;
    @FXML private Button navUsers;
    @FXML private Button navProjets;
    @FXML private Button navRecrutement;
    @FXML private Button navSalaire;

    private UserAccount loggedInUser;
    private UserSettingsService settingsService = new UserSettingsService();

    public void setLoggedInUser(UserAccount user) {
        this.loggedInUser = user;
        userNameLabel.setText(user.getUsername() + " (" + user.getRole() + ")");
        usernameDisplay.setText(user.getUsername());
        emailDisplay.setText(user.getEmail());
        roleDisplay.setText(user.getRole().toString());
        statusDisplay.setText(user.getAccountStatus());

        // Afficher le bouton Gestion des utilisateurs seulement pour ADMIN et MANAGER
        if (user.getRole() == UserRole.ADMINISTRATEUR || user.getRole() == UserRole.MANAGER) {
            userListButton.setVisible(true);
            userListButton.setManaged(true);
        }

        // Charger le thème après que la scène soit disponible
        settingsButton.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                loadUserSettings();
            }
        });
        if (settingsButton.getScene() != null) {
            loadUserSettings();
        }
    }

    @FXML
    public void initialize() {
        settingsButton.setOnAction(event -> openSettings());
        formationsButton.setOnAction(event -> openFormations());
        userListButton.setOnAction(event -> openUserList());
        logoutButton.setOnAction(event -> logout());

        // Navigation sidebar
        if (navDashboard != null)    navDashboard.setOnAction(e -> {}); // déjà sur dashboard
        if (navFormations != null)   navFormations.setOnAction(e -> openFormations());
        if (navUsers != null)        navUsers.setOnAction(e -> openUserList());
        if (navProjets != null)      navProjets.setOnAction(e -> showComingSoon("Projets"));
        if (navRecrutement != null)  navRecrutement.setOnAction(e -> showComingSoon("Recrutement"));
        if (navSalaire != null)      navSalaire.setOnAction(e -> showComingSoon("Salaire"));
    }

    // ===================== FORMATIONS =====================

    private void openFormations() {
        try {
            if (loggedInUser.getRole() == UserRole.ADMINISTRATEUR
                    || loggedInUser.getRole() == UserRole.MANAGER) {

                // Interface Admin/Manager
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ManageTraining.fxml"));
                Parent root = loader.load();

                ManageTrainingController ctrl = loader.getController();
                ctrl.setLoggedInUser(loggedInUser);

                Stage stage = (Stage) formationsButton.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("INTEGRA – Administration des Formations");
                stage.setMinWidth(1100);
                stage.setMinHeight(650);
                Platform.runLater(() -> stage.setMaximized(true));

            } else {
                // Interface User/Employé
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/UserTrainingList.fxml"));
                Parent root = loader.load();

                UserTrainingProgramListController ctrl = loader.getController();
                ctrl.setLoggedInUser(loggedInUser);

                Stage stage = (Stage) formationsButton.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("INTEGRA – Mes Formations");
                stage.setMinWidth(900);
                stage.setMinHeight(620);
                Platform.runLater(() -> stage.setMaximized(true));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ===================== USER LIST =====================

    private void openUserList() {
        if (loggedInUser == null) return;
        if (loggedInUser.getRole() != UserRole.ADMINISTRATEUR
                && loggedInUser.getRole() != UserRole.MANAGER) {
            showAlert(Alert.AlertType.ERROR, "Accès refusé",
                    "Vous n'avez pas les droits pour accéder à cette section.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UserList.fxml"));
            Parent root = loader.load();

            UserListController controller = loader.getController();
            controller.setLoggedInUser(loggedInUser);

            Stage stage = (Stage) userListButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Gestion des utilisateurs");
            stage.setMaximized(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ===================== SETTINGS =====================

    private void openSettings() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UserSettings.fxml"));
            Parent root = loader.load();

            UserSettingsController controller = loader.getController();
            controller.setUser(loggedInUser);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Paramètres");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadUserSettings();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ===================== THEME =====================

    private void loadUserSettings() {
        if (loggedInUser == null) return;
        try {
            UserSettings settings = settingsService.getSettingsByUserId(loggedInUser.getUserId());
            String theme = (settings != null && settings.getTheme() != null)
                    ? settings.getTheme() : "clair";
            applyTheme(theme);
        } catch (SQLException e) {
            e.printStackTrace();
            applyTheme("clair");
        }
    }

    private void applyTheme(String theme) {
        Scene scene = settingsButton.getScene();
        if (scene == null) return;
        scene.getRoot().getStyleClass().removeAll("theme-light", "theme-dark");
        if ("sombre".equals(theme)) {
            scene.getRoot().getStyleClass().add("theme-dark");
        } else {
            scene.getRoot().getStyleClass().add("theme-light");
        }
    }

    // ===================== LOGOUT =====================

    private void logout() {
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

    // ===================== HELPERS =====================

    private void showComingSoon(String module) {
        showAlert(Alert.AlertType.INFORMATION, "Bientôt disponible",
                "Le module " + module + " sera implémenté prochainement.");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}