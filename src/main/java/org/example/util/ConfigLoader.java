package org.example.util;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;

public class ConfigLoader {
    private static final String CONFIG_FILE = "/config.json";
    private static JsonNode config;

    static {
        try (InputStream is = ConfigLoader.class.getResourceAsStream(CONFIG_FILE)) {
            if (is == null) {
                throw new RuntimeException("Fichier config.json introuvable dans resources");
            }
            ObjectMapper mapper = new ObjectMapper();
            config = mapper.readTree(is);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lecture config.json", e);
        }
    }

    public static String getSendGridApiKey() {
        return config.has("sendgrid_api_key") ? config.get("sendgrid_api_key").asText() : null;
    }

    public static String getEmailValidationApiKey() {
        return config.has("email_validation_api_key") ? config.get("email_validation_api_key").asText() : null;
    }
}