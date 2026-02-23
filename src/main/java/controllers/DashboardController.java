package controllers;

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
    @FXML private Button userListButton;

    private UserAccount loggedInUser;
    private UserSettingsService settingsService = new UserSettingsService();

    public void setLoggedInUser(UserAccount user) {
        this.loggedInUser = user;
        userNameLabel.setText(user.getUsername() + " (" + user.getRole() + ")");
        usernameDisplay.setText(user.getUsername());
        emailDisplay.setText(user.getEmail());
        roleDisplay.setText(user.getRole().toString());
        statusDisplay.setText(user.getAccountStatus());

        // Afficher le bouton de liste utilisateurs seulement pour ADMIN et MANAGER
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
        // Si la scène est déjà définie, charger directement
        if (settingsButton.getScene() != null) {
            loadUserSettings();
        }
    }

    @FXML
    public void initialize() {
        settingsButton.setOnAction(event -> openSettings());
        userListButton.setOnAction(event -> openUserList());
        logoutButton.setOnAction(event -> logout());
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

            // Recharger le thème après modification
            loadUserSettings();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openUserList() {
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
}