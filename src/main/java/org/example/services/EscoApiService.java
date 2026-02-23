package org.example.services;

import com.google.gson.*;
import okhttp3.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class EscoApiService {

    // ✅ NOUVELLE URL CORRECTE
    private static final String BASE_URL = "https://ec.europa.eu/esco/api";

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build();

    public static class EscoSkill {
        private String uri;
        private String title;
        private String description;
        private String skillType;

        public EscoSkill(String uri, String title, String description, String skillType) {
            this.uri = uri;
            this.title = title;
            this.description = description;
            this.skillType = skillType;
        }

        public String getUri()         { return uri; }
        public String getTitle()       { return title; }
        public String getDescription() { return description; }
        public String getSkillType()   { return skillType; }

        @Override
        public String toString() {
            return title + " [" + skillType + "]";
        }
    }

    // ===== RECHERCHE PRINCIPALE =====
    public List<EscoSkill> searchSkills(String keyword, String language) {
        List<EscoSkill> results = new ArrayList<>();

        // ✅ URL correcte ESCO API v1.1.1
        String url = BASE_URL + "/search"
                + "?language=" + language
                + "&type=skill"
                + "&text=" + keyword.replace(" ", "%20")
                + "&limit=10";

        System.out.println("🔍 ESCO URL : " + url);

        try {
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Accept", "application/json")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                System.out.println("📡 HTTP Status : " + response.code());

                if (response.code() == 404) {
                    System.err.println("❌ 404 — essai URL alternative...");
                    return tryAlternativeUrl(keyword, language);
                }

                if (!response.isSuccessful() || response.body() == null) {
                    System.err.println("❌ Réponse non valide : " + response.code());
                    return results;
                }

                String json = response.body().string();
                System.out.println("📦 JSON : " + json.substring(0, Math.min(json.length(), 300)));
                results = parseEscoResponse(json);
            }

        } catch (IOException e) {
            System.err.println("❌ Erreur réseau : " + e.getMessage());
        }

        System.out.println("✅ Résultats : " + results.size());
        return results;
    }

    // ===== URL ALTERNATIVES =====
    private List<EscoSkill> tryAlternativeUrl(String keyword, String language) {
        List<EscoSkill> results = new ArrayList<>();

        // Liste des URLs alternatives à essayer
        String[] urls = {
                // ✅ URL v1
                "https://esco.ec.europa.eu/api/search?language=" + language + "&type=skill&text=" + keyword.replace(" ", "%20") + "&limit=10",
                // ✅ URL avec selectedVersion
                "https://esco.ec.europa.eu/api/search?language=" + language + "&type=skill&text=" + keyword.replace(" ", "%20") + "&limit=10&selectedVersion=v1.1.1",
                // ✅ URL resource/skill
                "https://esco.ec.europa.eu/api/resource/skill?language=" + language + "&uri=http://data.europa.eu/esco/skill/" + keyword,
                // ✅ URL suggest
                "https://esco.ec.europa.eu/api/suggest?language=" + language + "&type=skill&text=" + keyword.replace(" ", "%20") + "&limit=10",
        };

        for (String url : urls) {
            System.out.println("🔄 Essai URL : " + url);
            try {
                Request request = new Request.Builder()
                        .url(url)
                        .addHeader("Accept", "application/json")
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    System.out.println("   → Status : " + response.code());

                    if (response.isSuccessful() && response.body() != null) {
                        String json = response.body().string();
                        System.out.println("   → JSON : " + json.substring(0, Math.min(json.length(), 200)));
                        results = parseEscoResponse(json);
                        if (!results.isEmpty()) {
                            System.out.println("✅ URL fonctionnelle : " + url);
                            return results;
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("   → Erreur : " + e.getMessage());
            }
        }

        // ✅ Si tout échoue → utiliser données locales
        System.out.println("⚠️ API ESCO inaccessible → utilisation données locales");
        return getFallbackSkills(keyword);
    }

    // ===== DONNÉES LOCALES DE SECOURS =====
    private List<EscoSkill> getFallbackSkills(String keyword) {
        List<EscoSkill> fallback = new ArrayList<>();
        String kw = keyword.toLowerCase();

        // Base de données locale de compétences courantes
        String[][] skills = {
                {"Java", "Maîtrise du langage de programmation Java pour le développement d'applications.", "technique"},
                {"Python", "Développement avec le langage Python pour l'analyse de données et l'automatisation.", "technique"},
                {"JavaScript", "Développement web frontend et backend avec JavaScript.", "technique"},
                {"SQL", "Gestion et interrogation de bases de données relationnelles avec SQL.", "technique"},
                {"Spring Boot", "Développement d'applications Java avec le framework Spring Boot.", "technique"},
                {"React", "Développement d'interfaces utilisateur avec la bibliothèque React.", "technique"},
                {"Docker", "Conteneurisation d'applications avec Docker.", "technique"},
                {"Git", "Gestion de versions et collaboration avec Git.", "technique"},
                {"Machine Learning", "Conception et entraînement de modèles d'apprentissage automatique.", "technique"},
                {"Data Analysis", "Analyse et interprétation de données pour la prise de décision.", "technique"},
                {"Gestion de projet", "Planification, organisation et suivi de projets.", "soft"},
                {"Communication", "Capacité à communiquer efficacement à l'oral et à l'écrit.", "soft"},
                {"Leadership", "Capacité à diriger et motiver une équipe.", "soft"},
                {"Travail en équipe", "Collaboration efficace au sein d'une équipe.", "soft"},
                {"Résolution de problèmes", "Analyse et résolution de problèmes complexes.", "soft"},
                {"Adaptabilité", "Capacité à s'adapter aux changements et nouvelles situations.", "soft"},
                {"Angular", "Développement d'applications web avec le framework Angular.", "technique"},
                {"Node.js", "Développement backend avec Node.js.", "technique"},
                {"PHP", "Développement web avec le langage PHP.", "technique"},
                {"C#", "Développement d'applications avec le langage C#.", "technique"},
                {"Swift", "Développement d'applications iOS avec Swift.", "technique"},
                {"Kotlin", "Développement d'applications Android avec Kotlin.", "technique"},
                {"DevOps", "Pratiques d'intégration et de déploiement continu.", "technique"},
                {"Cybersécurité", "Protection des systèmes informatiques contre les menaces.", "technique"},
                {"Cloud Computing", "Utilisation des services cloud (AWS, Azure, GCP).", "technique"},
                {"Scrum", "Application de la méthodologie agile Scrum.", "soft"},
                {"ISTQB", "Tests logiciels selon les standards ISTQB.", "technique"},
                {"Symfony", "Développement web avec le framework PHP Symfony.", "technique"},
                {"Laravel", "Développement web avec le framework PHP Laravel.", "technique"},
                {"Flutter", "Développement d'applications mobiles cross-platform avec Flutter.", "technique"},
        };

        for (String[] s : skills) {
            if (s[0].toLowerCase().contains(kw) || kw.contains(s[0].toLowerCase())) {
                fallback.add(new EscoSkill("local://" + s[0], s[0], s[1], s[2]));
            }
        }

        // Si aucune correspondance exacte, retourner toutes les compétences qui contiennent le mot
        if (fallback.isEmpty()) {
            for (String[] s : skills) {
                if (s[1].toLowerCase().contains(kw)) {
                    fallback.add(new EscoSkill("local://" + s[0], s[0], s[1], s[2]));
                }
            }
        }

        return fallback;
    }

    // ===== PARSER =====
    private List<EscoSkill> parseEscoResponse(String json) {
        List<EscoSkill> results = new ArrayList<>();
        try {
            if (json.trim().startsWith("[")) {
                JsonArray arr = JsonParser.parseString(json).getAsJsonArray();
                for (JsonElement el : arr) {
                    EscoSkill s = parseSkillObject(el.getAsJsonObject());
                    if (s != null) results.add(s);
                }
            } else {
                JsonObject root = JsonParser.parseString(json).getAsJsonObject();

                JsonArray arr = null;
                if (root.has("_embedded") && root.getAsJsonObject("_embedded").has("results")) {
                    arr = root.getAsJsonObject("_embedded").getAsJsonArray("results");
                } else if (root.has("results")) {
                    arr = root.getAsJsonArray("results");
                }

                if (arr != null) {
                    for (JsonElement el : arr) {
                        EscoSkill s = parseSkillObject(el.getAsJsonObject());
                        if (s != null) results.add(s);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur parsing : " + e.getMessage());
        }
        return results;
    }

    private EscoSkill parseSkillObject(JsonObject obj) {
        try {
            String title = "";
            if (obj.has("preferredLabel")) {
                JsonElement lbl = obj.get("preferredLabel");
                if (lbl.isJsonPrimitive()) title = lbl.getAsString();
                else if (lbl.isJsonObject()) {
                    JsonObject o = lbl.getAsJsonObject();
                    title = o.has("fr") ? o.get("fr").getAsString() :
                            o.has("en") ? o.get("en").getAsString() : "";
                }
            } else if (obj.has("title"))  title = obj.get("title").getAsString();
            else if (obj.has("label"))    title = obj.get("label").getAsString();

            if (title.isEmpty()) return null;

            String uri  = obj.has("uri")         ? obj.get("uri").getAsString()         :
                    obj.has("conceptUri")   ? obj.get("conceptUri").getAsString()  : "";
            String type = obj.has("skillType")    ? obj.get("skillType").getAsString()   :
                    obj.has("conceptType")  ? obj.get("conceptType").getAsString() : "skill";
            String desc = "";
            if (obj.has("description")) {
                JsonElement d = obj.get("description");
                if (d.isJsonPrimitive()) desc = d.getAsString();
                else if (d.isJsonObject()) {
                    JsonObject o = d.getAsJsonObject();
                    desc = o.has("fr") ? o.get("fr").getAsString() :
                            o.has("en") ? o.get("en").getAsString() : "";
                }
            }

            return new EscoSkill(uri, title, desc, type);
        } catch (Exception e) {
            return null;
        }
    }

    public List<EscoSkill> searchSkillsFr(String keyword) { return searchSkills(keyword, "fr"); }
    public List<EscoSkill> searchSkillsEn(String keyword) { return searchSkills(keyword, "en"); }
    public List<EscoSkill> suggestSimilar(String name) {
        List<EscoSkill> r = searchSkillsFr(name);
        return r.isEmpty() ? searchSkillsEn(name) : r;
    }
}