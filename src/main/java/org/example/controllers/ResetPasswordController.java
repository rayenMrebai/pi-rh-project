package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.services.user.PasswordResetService;

import java.io.IOException;

public class ResetPasswordController {

    @FXML private Label emailLabel;
    @FXML private TextField tokenField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button resetButton;
    @FXML private Hyperlink backToLoginLink;

    private PasswordResetService resetService = new PasswordResetService();
    private String email;

    public void setEmail(String email) {
        this.email = email;
        emailLabel.setText("Email : " + email);
    }

    @FXML
    public void initialize() {
        resetButton.setOnAction(event -> handleReset());
        backToLoginLink.setOnAction(event -> goToLogin());
    }

    private void handleReset() {
        String token = tokenField.getText().trim();
        String newPass = newPasswordField.getText().trim();
        String confirm = confirmPasswordField.getText().trim();

        if (token.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Tous les champs sont obligatoires.");
            return;
        }

        if (!newPass.equals(confirm)) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Les mots de passe ne correspondent pas.");
            return;
        }

        if (newPass.length() < 6) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le mot de passe doit contenir au moins 6 caractères.");
            return;
        }

        boolean success = resetService.resetPassword(token, newPass);
        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Votre mot de passe a été réinitialisé. Vous pouvez maintenant vous connecter.");
            goToLogin();
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Code invalide ou expiré. Veuillez réessayer.");
        }
    }

    private void goToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Login.fxml"));
            Stage stage = (Stage) backToLoginLink.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Connexion");
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