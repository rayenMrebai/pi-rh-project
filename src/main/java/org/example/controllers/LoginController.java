package org.example.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.enums.UserRole;
import org.example.model.user.UserAccount;
import org.example.services.user.UserAccountService;

import java.io.IOException;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Hyperlink signUpLink;

    private UserAccountService userService = new UserAccountService();

    @FXML
    public void initialize() {
        loginButton.setOnAction(event -> handleLogin());
        signUpLink.setOnAction(event -> openSignUp());
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez remplir tous les champs.");
            return;
        }

        try {
            UserAccount user = userService.authenticate(username, password);
            if (user != null) {
                // Redirection selon le rôle
                if (user.getRole() == UserRole.ADMINISTRATEUR || user.getRole() == UserRole.MANAGER) {
                    // Aller vers la liste des utilisateurs
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/UserList.fxml"));
                    Parent root = loader.load();
                    UserListController listController = loader.getController();
                    listController.setLoggedInUser(user);

                    Stage stage = (Stage) loginButton.getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.setTitle("Gestion des utilisateurs");
                    stage.show();
                    Platform.runLater(() -> stage.setMaximized(true));
                } else {
                    // Aller vers le tableau de bord personnel
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/Dashboard.fxml"));
                    Parent root = loader.load();
                    DashboardController dashboardController = loader.getController();
                    dashboardController.setLoggedInUser(user);

                    Stage stage = (Stage) loginButton.getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.setTitle("Mon Tableau de Bord");
                    stage.show();
                    Platform.runLater(() -> stage.setMaximized(true));
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Nom d'utilisateur ou mot de passe incorrect.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de connexion : " + e.getMessage());
        }
    }

    private void openSignUp() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/SignUp.fxml"));
            Stage stage = (Stage) signUpLink.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Inscription");
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