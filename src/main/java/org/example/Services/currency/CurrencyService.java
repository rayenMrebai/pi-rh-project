package org.example.Services.currency;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class CurrencyService {

    // API 1 principale : ExchangeRate-API (pas de clé, mis à jour quotidien)
    private static final String API_PRIMARY =
            "https://open.er-api.com/v6/latest/TND";

    // API 2 fallback : fawazahmed0 (ton ancienne API, en backup)
    private static final String API_FALLBACK =
            "https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies/tnd.min.json";

    // Cache 30 minutes — données fraîches sans surcharger l'API
    private static final Duration CACHE_DURATION = Duration.ofMinutes(30);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private Map<String, Double> cachedRates;
    private Instant lastFetchTime;

    public CurrencyService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
        this.cachedRates = new HashMap<>();
        this.lastFetchTime = Instant.MIN;
    }

    private Map<String, Double> fetchRates() throws Exception {
        Instant now = Instant.now();

        // Si cache encore valide, retourner directement
        if (!cachedRates.isEmpty() && lastFetchTime.plus(CACHE_DURATION).isAfter(now)) {
            return cachedRates;
        }

        // Essayer API principale d'abord
        try {
            return fetchFromPrimary(now);
        } catch (Exception e) {
            System.err.println("API principale échouée, fallback : " + e.getMessage());
        }

        // Fallback sur ancienne API
        return fetchFromFallback(now);
    }

    private Map<String, Double> fetchFromPrimary(Instant now) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_PRIMARY))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(
                request, HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() != 200) {
            throw new Exception("Status " + response.statusCode());
        }

        JsonNode root = objectMapper.readTree(response.body());

        // Structure : { "result": "success", "rates": { "USD": 0.32, "EUR": 0.29 } }
        if (!"success".equals(root.path("result").asText())) {
            throw new Exception("API result not success");
        }

        JsonNode rates = root.get("rates");
        if (rates == null) throw new Exception("rates node missing");

        double usd = rates.get("USD").asDouble();
        double eur = rates.get("EUR").asDouble();

        cachedRates.put("USD", usd);
        cachedRates.put("EUR", eur);
        lastFetchTime = now;

        System.out.println("Taux chargés (ExchangeRate-API) : USD=" + usd + " EUR=" + eur);
        return cachedRates;
    }

    private Map<String, Double> fetchFromFallback(Instant now) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_FALLBACK))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(
                request, HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() != 200) {
            throw new Exception("Fallback API status " + response.statusCode());
        }

        JsonNode root = objectMapper.readTree(response.body());
        JsonNode tndNode = root.get("tnd");
        if (tndNode == null) throw new Exception("tnd node missing in fallback");

        double usd = tndNode.get("usd").asDouble();
        double eur = tndNode.get("eur").asDouble();

        cachedRates.put("USD", usd);
        cachedRates.put("EUR", eur);
        lastFetchTime = now;

        System.out.println("Taux chargés (Fallback API) : USD=" + usd + " EUR=" + eur);
        return cachedRates;
    }

    public double convert(double amount, String fromCurrency, String toCurrency) throws Exception {
        if (fromCurrency.equals(toCurrency)) return amount;

        Map<String, Double> rates = fetchRates();

        // Convertir d'abord en TND
        double amountInTND;
        if (fromCurrency.equals("TND")) {
            amountInTND = amount;
        } else {
            Double rate = rates.get(fromCurrency);
            if (rate == null) throw new Exception("Taux non disponible pour " + fromCurrency);
            // rate = TND → fromCurrency, donc inverse pour avoir fromCurrency → TND
            amountInTND = amount / rate;
        }

        // Convertir TND vers la devise cible
        if (toCurrency.equals("TND")) return amountInTND;

        Double rate = rates.get(toCurrency);
        if (rate == null) throw new Exception("Taux non disponible pour " + toCurrency);
        return amountInTND * rate;
    }

    public String getDisplayRates() {
        try {
            Map<String, Double> rates = fetchRates();
            double usd = rates.get("USD");
            double eur = rates.get("EUR");
            // Dans CurrencyService.getDisplayRates()
            return String.format("%.4f USD  •  %.4f EUR", usd, eur);
        } catch (Exception e) {
            return "⚠️ Taux indisponible";
        }
    }
    /**
     * Retourne les taux actuels (USD et EUR) sous forme de map.
     */
    public Map<String, Double> getRates() throws Exception {
        fetchRates(); // force le rafraîchissement du cache
        return new HashMap<>(cachedRates);
    }
}