package org.example.controllers;

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
import org.example.util.SessionManager;

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

    @FXML private Button navDashboard;
    @FXML private Button navUsers;
    @FXML private Button navFormations;
    @FXML private Button navProjets;
    @FXML private Button navRecrutement;
    @FXML private Button navSalaire;

    private UserAccount loggedInUser;
    private UserSettingsService settingsService = new UserSettingsService();

    public void setLoggedInUser(UserAccount user) {
        this.loggedInUser = user;

        // ✅ FIX : Toujours synchroniser SessionManager
        SessionManager.setCurrentUser(user);

        userNameLabel.setText(user.getUsername() + " (" + user.getRole() + ")");
        usernameDisplay.setText(user.getUsername());
        emailDisplay.setText(user.getEmail());
        roleDisplay.setText(user.getRole().toString());
        statusDisplay.setText(user.getAccountStatus());

        adaptSidebarToRole();

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
        logoutButton.setOnAction(event -> logout());

        navDashboard.setOnAction(e -> {});
        navUsers.setOnAction(e -> openUserList());
        navFormations.setOnAction(e -> showComingSoon("Formations"));
        navProjets.setOnAction(e -> showComingSoon("Projets"));
        navRecrutement.setOnAction(e -> showComingSoon("Recrutement"));
        navSalaire.setOnAction(e -> openSalaireManagement());
    }

    private void adaptSidebarToRole() {
        if (loggedInUser == null) return;

        UserRole role = loggedInUser.getRole();

        if (role == UserRole.ADMINISTRATEUR || role == UserRole.MANAGER) {
            navUsers.setVisible(true);
            navUsers.setManaged(true);
            navSalaire.setVisible(true);
            navSalaire.setManaged(true);
        } else {
            // EMPLOYE : seulement Salaire
            navUsers.setVisible(false);
            navUsers.setManaged(false);
            navSalaire.setVisible(true);
            navSalaire.setManaged(true);
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
        Scene scene = settingsButton.getScene();
        if (scene == null) return;
        scene.getRoot().getStyleClass().removeAll("theme-light", "theme-dark");
        if ("sombre".equals(theme)) {
            scene.getRoot().getStyleClass().add("theme-dark");
        } else {
            scene.getRoot().getStyleClass().add("theme-light");
        }
    }

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

    private void openUserList() {
        if (loggedInUser.getRole() != UserRole.ADMINISTRATEUR &&
                loggedInUser.getRole() != UserRole.MANAGER) {
            showAlert(Alert.AlertType.ERROR, "Accès refusé",
                    "Vous n'avez pas accès à la gestion des utilisateurs.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UserList.fxml"));
            Parent root = loader.load();

            UserListController controller = loader.getController();
            controller.setLoggedInUser(loggedInUser); // ✅ toujours passer le user

            Stage stage = (Stage) navUsers.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Gestion des utilisateurs");
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

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}