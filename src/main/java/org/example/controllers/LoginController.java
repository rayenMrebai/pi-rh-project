package org.example.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.enums.UserRole;
import org.example.model.user.UserAccount;
import org.example.services.user.UserAccountService;
import org.example.util.SessionManager;

import java.io.IOException;

public class LoginController {

    @FXML private GridPane passwordPane;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private VBox facePane;
    @FXML private Button scanFaceBtn;
    @FXML private Button tabPasswordBtn;
    @FXML private Button tabFaceBtn;
    @FXML private Label errorLabel;
    @FXML private Hyperlink signUpLink;
    @FXML private Hyperlink forgotPasswordLink;

    private UserAccountService userService = new UserAccountService();

    @FXML
    public void initialize() {
        // ❌ Supprime cette ligne (double login)
        // loginButton.setOnAction(event -> handleLogin());

        signUpLink.setOnAction(event -> openSignUp());

        // ✅ Ajoute cette ligne
        forgotPasswordLink.setOnAction(event -> openForgotPassword());
    }

    @FXML private void showPasswordTab() {
        passwordPane.setVisible(true); passwordPane.setManaged(true);
        facePane.setVisible(false); facePane.setManaged(false);
        tabPasswordBtn.getStyleClass().setAll("tab-btn-active");
        tabFaceBtn.getStyleClass().setAll("tab-btn-inactive");
        hideError();
    }


    @FXML private void handlePasswordLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs.");
            return;
        }

        UserAccount user = UserAccountService.authenticate(username, password);
        if (user == null) {
            showError("Nom d'utilisateur ou mot de passe incorrect.");
        } else {
            navigateToDashboard(user);
        }
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
                // ✅ FIX : Toujours enregistrer dans SessionManager en premier
                SessionManager.setCurrentUser(user);

                if (user.getRole() == UserRole.ADMINISTRATEUR || user.getRole() == UserRole.MANAGER) {
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

    private void navigateToDashboard(UserAccount user) {
        try {
            String fxml = (user.getRole().toString().equals("ADMINISTRATEUR") || user.getRole().toString().equals("MANAGER"))
                    ? "/UserList.fxml" : "/Dashboard.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Object controller = loader.getController();
            if (controller instanceof UserListController) ((UserListController) controller).setLoggedInUser(user);
            else if (controller instanceof DashboardController) ((DashboardController) controller).setLoggedInUser(user);
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showError("Erreur de navigation: " + e.getMessage());
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

    private void openForgotPassword() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/ForgotPassword.fxml"));
            Stage stage = (Stage) forgotPasswordLink.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Récupération de mot de passe");
            stage.setMaximized(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void hideError() {
        errorLabel.setVisible(false);
    }
}