package org.example.model.user;

public class UserSettings {
    private int settingsId;
    private int userId;
    private String theme; // "clair" ou "sombre"
    private String language;
    private String defaultModule;
    private boolean notificationsEnabled;
    private String dashboardLayout;
    private String accessPreferences;

    public UserSettings() {}

    public UserSettings(int userId, String theme, String language,
                        String defaultModule, boolean notificationsEnabled) {
        this.userId = userId;
        this.theme = theme;
        this.language = language;
        this.defaultModule = defaultModule;
        this.notificationsEnabled = notificationsEnabled;
    }

    public UserSettings(int settingsId, int userId, String theme, String language,
                        String defaultModule, boolean notificationsEnabled,
                        String dashboardLayout, String accessPreferences) {
        this.settingsId = settingsId;
        this.userId = userId;
        this.theme = theme;
        this.language = language;
        this.defaultModule = defaultModule;
        this.notificationsEnabled = notificationsEnabled;
        this.dashboardLayout = dashboardLayout;
        this.accessPreferences = accessPreferences;
    }

    // Getters et setters
    public int getSettingsId() { return settingsId; }
    public void setSettingsId(int settingsId) { this.settingsId = settingsId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getDefaultModule() { return defaultModule; }
    public void setDefaultModule(String defaultModule) { this.defaultModule = defaultModule; }

    public boolean isNotificationsEnabled() { return notificationsEnabled; }
    public void setNotificationsEnabled(boolean notificationsEnabled) { this.notificationsEnabled = notificationsEnabled; }

    public String getDashboardLayout() { return dashboardLayout; }
    public void setDashboardLayout(String dashboardLayout) { this.dashboardLayout = dashboardLayout; }

    public String getAccessPreferences() { return accessPreferences; }
    public void setAccessPreferences(String accessPreferences) { this.accessPreferences = accessPreferences; }

    @Override
    public String toString() {
        return "UserSettings{" +
                "settingsId=" + settingsId +
                ", userId=" + userId +
                ", theme='" + theme + '\'' +
                ", language='" + language + '\'' +
                '}';
    }
}