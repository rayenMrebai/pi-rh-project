package org.example.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class EmailConfig {

    private static Properties properties;

    static {
        properties = new Properties();
        try (InputStream input = EmailConfig.class.getClassLoader().getResourceAsStream("email.properties")) {

            if (input == null) {
                System.err.println("❌ Fichier email.properties introuvable !");
                return;
            }

            properties.load(input);
            System.out.println("✅ Configuration email chargée avec succès");

        } catch (IOException e) {
            System.err.println("❌ Erreur chargement config email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static String getSmtpHost() {
        return properties.getProperty("email.smtp.host");
    }

    public static String getSmtpPort() {
        return properties.getProperty("email.smtp.port");
    }

    public static String getUsername() {
        return properties.getProperty("email.username");
    }

    public static String getPassword() {
        return properties.getProperty("email.password");
    }

    public static String getFromAddress() {
        return properties.getProperty("email.from.address");
    }

    public static String getFromName() {
        return properties.getProperty("email.from.name");
    }

    public static boolean isSmtpAuthEnabled() {
        return Boolean.parseBoolean(properties.getProperty("email.smtp.auth"));
    }

    public static boolean isStartTlsEnabled() {
        return Boolean.parseBoolean(properties.getProperty("email.smtp.starttls.enable"));
    }
}