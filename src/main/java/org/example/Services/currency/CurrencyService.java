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
    private static final String API_URL = "https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies/tnd.json";
    private static final Duration CACHE_DURATION = Duration.ofSeconds(60);

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
        if (cachedRates.isEmpty() || lastFetchTime.plus(CACHE_DURATION).isBefore(now)) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new Exception("API returned status " + response.statusCode());
            }

            JsonNode root = objectMapper.readTree(response.body());
            // La réponse a la structure { "tnd": { "usd": 0.32, "eur": 0.29, ... } }
            JsonNode tndNode = root.get("tnd");
            if (tndNode == null) {
                throw new Exception("TND rates not found in API response");
            }

            double usd = tndNode.get("usd").asDouble();
            double eur = tndNode.get("eur").asDouble();

            cachedRates.put("USD", usd);
            cachedRates.put("EUR", eur);
            lastFetchTime = now;
        }
        return cachedRates;
    }

    public double convert(double amount, String fromCurrency, String toCurrency) throws Exception {
        if (fromCurrency.equals(toCurrency)) {
            return amount;
        }

        Map<String, Double> rates = fetchRates();

        double amountInTND;
        if (fromCurrency.equals("TND")) {
            amountInTND = amount;
        } else {
            Double rate = rates.get(fromCurrency);
            if (rate == null) {
                throw new Exception("Taux non disponible pour " + fromCurrency);
            }
            amountInTND = amount / rate;
        }

        if (toCurrency.equals("TND")) {
            return amountInTND;
        } else {
            Double rate = rates.get(toCurrency);
            if (rate == null) {
                throw new Exception("Taux non disponible pour " + toCurrency);
            }
            return amountInTND * rate;
        }
    }

    public String getDisplayRates() {
        try {
            Map<String, Double> rates = fetchRates();
            double usd = rates.get("USD");
            double eur = rates.get("EUR");
            return String.format("1 TND = %.2f USD | %.2f EUR", usd, eur);
        } catch (Exception e) {
            return "Taux indisponible";
        }
    }
}