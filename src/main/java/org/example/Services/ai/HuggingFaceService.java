package org.example.Services.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class HuggingFaceService {

    private static final String API_URL =
            "https://router.huggingface.co/hf-inference/models/" +
                    "sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2/" +
                    "pipeline/feature-extraction";

    private static final String API_TOKEN = loadToken();

    private final ObjectMapper mapper = new ObjectMapper();

    private static String loadToken() {
        // 1. Variable d'environnement système
        String token = System.getenv("HF_TOKEN");
        if (token != null && !token.isBlank()) return token.trim();

        // 2. Fichier config.properties à la racine du projet
        try {
            File configFile = new File("config.properties");
            if (configFile.exists()) {
                Properties props = new Properties();
                props.load(new FileInputStream(configFile));
                token = props.getProperty("HF_TOKEN");
                if (token != null && !token.isBlank()) return token.trim();
            }
        } catch (Exception e) {
            System.err.println("Erreur lecture config.properties : " + e.getMessage());
        }

        throw new RuntimeException(
                "Token HuggingFace introuvable.\n" +
                        "Crée un fichier config.properties à la racine avec :\n" +
                        "HF_TOKEN=hf_..."
        );
    }

    public float[] getEmbedding(String text) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("inputs", text);
        String jsonBody = mapper.writeValueAsString(payload);

        URL url = new URL(API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + API_TOKEN);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("X-Wait-For-Model", "true");
        conn.setDoOutput(true);
        conn.setConnectTimeout(20000);
        conn.setReadTimeout(60000);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
        }

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            InputStream err = conn.getErrorStream();
            String errMsg = err != null
                    ? new String(err.readAllBytes(), StandardCharsets.UTF_8)
                    : "Unknown error";
            throw new RuntimeException("HuggingFace API error " + responseCode + ": " + errMsg);
        }

        String responseBody = new String(
                conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8
        );
        JsonNode root = mapper.readTree(responseBody);

        JsonNode vectorNode;
        if (root.isArray() && root.size() > 0 && root.get(0).isArray()) {
            vectorNode = root.get(0);
        } else if (root.isArray() && root.size() > 0 && root.get(0).isNumber()) {
            vectorNode = root;
        } else if (root.has("embeddings")) {
            vectorNode = root.get("embeddings").get(0);
        } else {
            throw new RuntimeException("Format de réponse inattendu : " + root.toString());
        }

        float[] vector = new float[vectorNode.size()];
        for (int i = 0; i < vectorNode.size(); i++) {
            vector[i] = (float) vectorNode.get(i).asDouble();
        }
        return vector;
    }

    public double cosineSimilarity(float[] a, float[] b) {
        if (a.length != b.length)
            throw new IllegalArgumentException("Vectors must have same length");

        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot   += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        if (normA == 0 || normB == 0) return 0;
        double raw = dot / (Math.sqrt(normA) * Math.sqrt(normB));
        return (raw + 1.0) / 2.0;
    }
}