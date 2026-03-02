package org.example.services.user;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.example.util.ConfigLoader;

public class EmailValidationService {

    private static final String API_URL = "https://api.zerobounce.net/v2/validate?api_key=%s&email=%s";

    public static boolean isEmailValid(String email) {
        String apiKey = ConfigLoader.getEmailValidationApiKey();

        // Si pas de clé, on accepte l'email (mode dégradé)
        if (apiKey == null || apiKey.isEmpty() || "votre_cle_zerobounce_ici".equals(apiKey)) {
            System.err.println("⚠️ Clé API ZeroBounce manquante. Validation ignorée (compte créé quand même).");
            return true;
        }

        String url = String.format(API_URL, apiKey, email);
        System.out.println("🔍 Appel ZeroBounce : " + url.replace(apiKey, "****"));

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            try (CloseableHttpResponse response = client.execute(request)) {
                String json = EntityUtils.toString(response.getEntity());
                System.out.println("📩 Réponse brute : " + json);

                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(json);

                // Si la réponse HTTP n'est pas 200, on accepte l'email
                if (response.getStatusLine().getStatusCode() != 200) {
                    System.err.println("❌ Erreur HTTP " + response.getStatusLine().getStatusCode() + " → email accepté (mode dégradé)");
                    return true;
                }

                // Si la réponse contient une erreur (clé invalide, quota épuisé)
                if (root.has("error")) {
                    System.err.println("❌ Erreur API : " + root.get("error").asText() + " → email accepté (mode dégradé)");
                    return true;
                }

                String status = root.path("status").asText();
                String subStatus = root.path("sub_status").asText();
                System.out.println("Statut ZeroBounce : " + status + ", sous-statut : " + subStatus);

                // On accepte les emails valides ou catch-all
                if ("valid".equalsIgnoreCase(status) || "catch-all".equalsIgnoreCase(status)) {
                    return true;
                } else {
                    System.out.println("❌ Email rejeté par ZeroBounce (status=" + status + ")");
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Exception lors de l'appel ZeroBounce : " + e.getMessage() + " → email accepté (mode dégradé)");
            return true; // en cas d'exception, on accepte
        }
    }
}