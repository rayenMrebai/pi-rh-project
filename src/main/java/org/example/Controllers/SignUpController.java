package org.example.Controllers;

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
            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Adresse email invalide.");
                return;
            }

            try {
                UserAccount newUser = new UserAccount(username, email, "temp", role);
                userService.createUser(newUser, password);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Compte créé !");
                closeWindow();
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

    private void openLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Login.fxml"));
            Stage stage = (Stage) loginLink.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Connexion");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) signUpButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}