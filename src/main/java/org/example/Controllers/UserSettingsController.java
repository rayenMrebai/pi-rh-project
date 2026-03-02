package org.example.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.model.user.UserAccount;
import org.example.model.user.UserSettings;
import org.example.services.user.UserSettingsService;
import java.sql.SQLException;

public class UserSettingsController {

    @FXML private ComboBox<String> themeCombo;
    @FXML private ComboBox<String> languageCombo;
    @FXML private TextField defaultModuleField;
    @FXML private CheckBox notificationsCheck;
    @FXML private TextArea dashboardLayoutArea;
    @FXML private TextArea accessPreferencesArea;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

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
                currentSettings.setNotificationsEnabled(true);
            } else {
                themeCombo.setValue(currentSettings.getTheme());
                languageCombo.setValue(currentSettings.getLanguage());
                defaultModuleField.setText(currentSettings.getDefaultModule());
                notificationsCheck.setSelected(currentSettings.isNotificationsEnabled());
                dashboardLayoutArea.setText(currentSettings.getDashboardLayout());
                accessPreferencesArea.setText(currentSettings.getAccessPreferences());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les paramètres.");
        }
    }

    private void saveSettings() {
        if (currentUser == null) return;

        currentSettings.setTheme(themeCombo.getValue());
        currentSettings.setLanguage(languageCombo.getValue());
        currentSettings.setDefaultModule(defaultModuleField.getText());
        currentSettings.setNotificationsEnabled(notificationsCheck.isSelected());
        currentSettings.setDashboardLayout(dashboardLayoutArea.getText());
        currentSettings.setAccessPreferences(accessPreferencesArea.getText());

        try {
            settingsService.createOrUpdate(currentSettings);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Paramètres enregistrés.");
            closeWindow();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec : " + e.getMessage());
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