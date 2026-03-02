package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.services.user.PasswordResetService;

import java.io.IOException;

public class ForgotPasswordController {

    @FXML private TextField emailField;
    @FXML private Button sendButton;
    @FXML private Hyperlink backToLoginLink;

    private PasswordResetService resetService = new PasswordResetService();

    @FXML
    public void initialize() {
        sendButton.setOnAction(event -> handleSend());
        backToLoginLink.setOnAction(event -> goToLogin());
    }

    private void handleSend() {
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez entrer votre adresse email.");
            return;
        }

        boolean initiated = resetService.initiatePasswordReset(email);
        if (initiated) {
            showAlert(Alert.AlertType.INFORMATION, "Email envoyé",
                    "Un email contenant un code de réinitialisation a été envoyé à " + email +
                            ".\nVeuillez vérifier votre boîte de réception.");

            // Open ResetPassword view, passing email
            goToResetPassword(email);
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Aucun compte trouvé avec cette adresse email. Veuillez vérifier ou créer un compte.");
        }
    }

    private void goToResetPassword(String email) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ResetPassword.fxml"));
            Parent root = loader.load();
            ResetPasswordController controller = loader.getController();
            controller.setEmail(email); // pass email for display (optional)

            Stage stage = (Stage) sendButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Réinitialisation du mot de passe");
            stage.setMaximized(false);
        } catch (IOException e) {
            e.printStackTrace();
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