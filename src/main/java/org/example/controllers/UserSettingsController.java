package org.example.controllers;;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.model.user.UserAccount;
import org.example.model.user.UserSettings;
import org.example.services.user.UserSettingsService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserSettingsController {

    @FXML private ComboBox<String> themeCombo;
    @FXML private ComboBox<String> languageCombo;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Button enrollFaceButton;

    private UserSettingsService settingsService = new UserSettingsService();
    private UserAccount currentUser;
    private UserSettings currentSettings;

    public void setUser(UserAccount user) {
        this.currentUser = user;
        loadSettings();
    }

    @FXML
    public void initialize() {
        themeCombo.getItems().setAll("clair", "sombre");
        languageCombo.getItems().setAll("fr", "en", "es");

        saveButton.setOnAction(event -> saveSettings());
        cancelButton.setOnAction(event -> closeWindow());
        enrollFaceButton.setOnAction(e -> {
            enrollFaceButton.setText("Loading..."); // remettre le texte après
        });
    }

    private void loadSettings() {
        if (currentUser == null) return;
        try {
            currentSettings = settingsService.getSettingsByUserId(currentUser.getUserId());
            if (currentSettings == null) {
                currentSettings = new UserSettings();
                currentSettings.setUserId(currentUser.getUserId());
                currentSettings.setTheme("clair");
                currentSettings.setLanguage("fr");
                // Les autres champs restent à leur valeur par défaut (null ou false)
            } else {
                themeCombo.setValue(currentSettings.getTheme());
                languageCombo.setValue(currentSettings.getLanguage());
                // On ignore les autres champs
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les paramètres.");
        }
    }

    private void saveSettings() {
        if (currentUser == null) return;

        String theme = themeCombo.getValue();
        String language = languageCombo.getValue();
        if (theme == null || language == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un thème et une langue.");
            return;
        }

        currentSettings.setTheme(theme);
        currentSettings.setLanguage(language);
        // Les autres champs restent inchangés (ils conservent leurs anciennes valeurs)

        try {
            settingsService.createOrUpdate(currentSettings);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Paramètres enregistrés.");
            closeWindow();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la sauvegarde : " + e.getMessage());
        }
    }



    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
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