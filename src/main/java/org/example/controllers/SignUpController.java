package org.example.controllers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import org.example.enums.UserRole;
import org.example.model.user.UserAccount;
import org.example.services.user.UserAccountService;
import org.example.services.user.EmailValidationService; // <-- IMPORT AJOUTÉ

import java.io.IOException;

public class SignUpController {

    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private ComboBox<UserRole> roleCombo;
    @FXML private Button signUpButton;
    @FXML private Hyperlink loginLink;

    private UserAccountService userService = new UserAccountService();
    private UserAccount userToEdit;
    private boolean fromLogin = false;

    public void setUserToEdit(UserAccount user) {
        this.userToEdit = user;
        if (user != null) {
            usernameField.setText(user.getUsername());
            emailField.setText(user.getEmail());
            roleCombo.setValue(user.getRole());
            passwordField.setPromptText("Laisser vide pour ne pas changer");
            confirmPasswordField.setPromptText("Laisser vide pour ne pas changer");
            signUpButton.setText("Modifier");
        } else {
            signUpButton.setText("S'inscrire");
        }
    }

    public void setFromLogin(boolean fromLogin) {
        this.fromLogin = fromLogin;
    }

    @FXML
    public void initialize() {
        roleCombo.getItems().setAll(UserRole.values());
        roleCombo.setValue(UserRole.EMPLOYE);

        signUpButton.setOnAction(event -> handleSignUp());
        loginLink.setOnAction(event -> openLogin());
    }

    private void handleSignUp() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();
        String confirm = confirmPasswordField.getText().trim();
        UserRole role = roleCombo.getValue();

        if (username.isEmpty() || email.isEmpty() || role == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Les champs obligatoires doivent être remplis.");
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Adresse email invalide.");
            return;
        }

        // Validation email via API ZeroBounce
        if (!EmailValidationService.isEmailValid(email)) {
            showAlert(Alert.AlertType.ERROR, "Email invalide",
                    "L'adresse email semble incorrecte ou n'existe pas. Veuillez vérifier.");
            return;
        }

        if (userToEdit == null) {
            // Ajout
            if (password.isEmpty() || confirm.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Le mot de passe est requis.");
                return;
            }
            if (!password.equals(confirm)) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Les mots de passe ne correspondent pas.");
                return;
            }
            if (password.length() < 6) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Le mot de passe doit contenir au moins 6 caractères.");
                return;
            }

            try {
                UserAccount newUser = new UserAccount(username, email, "temp", role);
                userService.createUser(newUser, password);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Compte créé !");

                if (fromLogin) {
                    loginAndRedirect(newUser, password);
                } else {
                    closeWindow();
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (e.getMessage().contains("Duplicate entry")) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Nom d'utilisateur ou email déjà utilisé.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur : " + e.getMessage());
                }
            }
        } else {
            // Modification
            userToEdit.setUsername(username);
            userToEdit.setEmail(email);
            userToEdit.setRole(role);
            try {
                userService.update(userToEdit);
                if (!password.isEmpty()) {
                    userService.changePassword(userToEdit.getUserId(), password);
                }
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Utilisateur modifié.");
                closeWindow();
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur : " + e.getMessage());
            }
        }
    }

    private void loginAndRedirect(UserAccount user, String password) {
        try {
            UserAccount authenticated = userService.authenticate(user.getUsername(), password);
            if (authenticated == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la connexion automatique.");
                openLogin();
                return;
            }

            Stage stage = (Stage) signUpButton.getScene().getWindow();

            String fxmlPath;
            if (authenticated.getRole() == UserRole.ADMINISTRATEUR || authenticated.getRole() == UserRole.MANAGER) {
                fxmlPath = "/UserList.fxml";
            } else {
                fxmlPath = "/Dashboard.fxml";
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Object controller = loader.getController();

            if (controller instanceof UserListController) {
                ((UserListController) controller).setLoggedInUser(authenticated);
            } else if (controller instanceof DashboardController) {
                ((DashboardController) controller).setLoggedInUser(authenticated);
            }

            switchSceneWithMaximize(stage, root,
                    authenticated.getRole() == UserRole.ADMINISTRATEUR || authenticated.getRole() == UserRole.MANAGER
                            ? "Gestion des utilisateurs" : "Mon Tableau de Bord");

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la redirection : " + e.getMessage());
            openLogin();
        }
    }

    private void openLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) loginLink.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Connexion");
            stage.setMaximized(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) signUpButton.getScene().getWindow();
        stage.close();
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