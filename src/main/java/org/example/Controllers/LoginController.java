package org.example.Controllers;

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
import org.example.util.SessionManager;

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

                // ✅ Sauvegarder dans la session
                SessionManager.setCurrentUser(user);
                System.out.println("✅ Connecté : " + user.getUsername()
                        + " | Rôle : " + user.getRole());

                // ✅ Naviguer vers le Dashboard pour tous les rôles
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Dashboard.fxml"));
                Parent root = loader.load();

                // ✅ Passer l'utilisateur connecté au DashboardController
                DashboardController dashboardController = loader.getController();
                dashboardController.setLoggedInUser(user);

                Stage stage = (Stage) loginButton.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("INTEGRA – Tableau de Bord");
                stage.setMinWidth(900);
                stage.setMinHeight(620);
                stage.show();
                Platform.runLater(() -> stage.setMaximized(true));

            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Nom d'utilisateur ou mot de passe incorrect.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Erreur de connexion : " + e.getMessage());
        }
    }

    private void openSignUp() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Signup.fxml"));
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