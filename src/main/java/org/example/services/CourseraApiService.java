package org.example.services;

import com.google.gson.*;
import okhttp3.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CourseraApiService {

    // ✅ Remplacez par votre vraie clé RapidAPI
    private static final String RAPID_API_KEY  = "72e2d8e622msh0ad24cb45cb6f78p164861jsn18f0e77a3774";
    private static final String RAPID_API_HOST = "coursera-courses-and-learning.p.rapidapi.com";
    private static final String BASE_URL       = "https://" + RAPID_API_HOST;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build();

    // ===== MODÈLE =====
    public static class CourseraCourse {
        private String id;
        private String title;
        private String description;
        private String partner;       // ex: Google, IBM, Stanford
        private String difficulty;    // Beginner, Intermediate, Advanced
        private String duration;
        private String url;
        private String type;          // course, specialization, certificate

        public CourseraCourse(String id, String title, String description,
                              String partner, String difficulty,
                              String duration, String url, String type) {
            this.id          = id;
            this.title       = title;
            this.description = description;
            this.partner     = partner;
            this.difficulty  = difficulty;
            this.duration    = duration;
            this.url         = url;
            this.type        = type;
        }

        public String getId()          { return id; }
        public String getTitle()       { return title; }
        public String getDescription() { return description; }
        public String getPartner()     { return partner; }
        public String getDifficulty()  { return difficulty; }
        public String getDuration()    { return duration; }
        public String getUrl()         { return url; }
        public String getType()        { return type; }

        @Override
        public String toString() {
            return title + (partner.isEmpty() ? "" : " — " + partner);
        }
    }

    // ===== RECHERCHE DE FORMATIONS =====
    public List<CourseraCourse> searchCourses(String keyword) {
        List<CourseraCourse> results = new ArrayList<>();

        String url = BASE_URL + "/search"
                + "?query=" + keyword.replace(" ", "%20")
                + "&limit=10";

        System.out.println("🔍 Coursera URL : " + url);

        try {
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("X-RapidAPI-Key",  RAPID_API_KEY)
                    .addHeader("X-RapidAPI-Host", RAPID_API_HOST)
                    .addHeader("Accept", "application/json")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                System.out.println("📡 HTTP Status : " + response.code());

                if (!response.isSuccessful() || response.body() == null) {
                    System.err.println("❌ Erreur : " + response.code());
                    return getFallbackCourses(keyword);
                }

                String json = response.body().string();
                System.out.println("📦 JSON : " + json.substring(0, Math.min(json.length(), 300)));

                results = parseCourseraResponse(json);

                if (results.isEmpty()) {
                    System.out.println("⚠️ Résultats vides → fallback local");
                    return getFallbackCourses(keyword);
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur réseau : " + e.getMessage());
            return getFallbackCourses(keyword);
        }

        System.out.println("✅ Coursera résultats : " + results.size());
        return results;
    }

    // ===== PARSER LA RÉPONSE =====
    private List<CourseraCourse> parseCourseraResponse(String json) {
        List<CourseraCourse> results = new ArrayList<>();

        try {
            JsonElement root = JsonParser.parseString(json);

            // Structure 1 : tableau direct [...]
            if (root.isJsonArray()) {
                for (JsonElement el : root.getAsJsonArray()) {
                    CourseraCourse c = parseCourseObject(el.getAsJsonObject());
                    if (c != null) results.add(c);
                }
                return results;
            }

            JsonObject rootObj = root.getAsJsonObject();

            // Structure 2 : { "courses": [...] }
            if (rootObj.has("courses")) {
                for (JsonElement el : rootObj.getAsJsonArray("courses")) {
                    CourseraCourse c = parseCourseObject(el.getAsJsonObject());
                    if (c != null) results.add(c);
                }
                return results;
            }

            // Structure 3 : { "results": [...] }
            if (rootObj.has("results")) {
                for (JsonElement el : rootObj.getAsJsonArray("results")) {
                    CourseraCourse c = parseCourseObject(el.getAsJsonObject());
                    if (c != null) results.add(c);
                }
                return results;
            }

            // Structure 4 : { "data": { "courses": { "elements": [...] } } }
            if (rootObj.has("data")) {
                JsonObject data = rootObj.getAsJsonObject("data");
                if (data.has("courses")) {
                    JsonObject courses = data.getAsJsonObject("courses");
                    if (courses.has("elements")) {
                        for (JsonElement el : courses.getAsJsonArray("elements")) {
                            CourseraCourse c = parseCourseObject(el.getAsJsonObject());
                            if (c != null) results.add(c);
                        }
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur parsing : " + e.getMessage());
        }

        return results;
    }

    private CourseraCourse parseCourseObject(JsonObject obj) {
        try {
            // ID
            String id = obj.has("id") ? obj.get("id").getAsString() :
                    obj.has("slug") ? obj.get("slug").getAsString() : "";

            // Titre
            String title = obj.has("name")  ? obj.get("name").getAsString()  :
                    obj.has("title") ? obj.get("title").getAsString() : "";
            if (title.isEmpty()) return null;

            // Description
            String desc = obj.has("description")     ? obj.get("description").getAsString()     :
                    obj.has("shortDescription") ? obj.get("shortDescription").getAsString() : "";

            // Partner / Organisation
            String partner = "";
            if (obj.has("partnerName")) {
                partner = obj.get("partnerName").getAsString();
            } else if (obj.has("partners") && obj.get("partners").isJsonArray()) {
                JsonArray partners = obj.getAsJsonArray("partners");
                if (partners.size() > 0) {
                    JsonObject p = partners.get(0).getAsJsonObject();
                    partner = p.has("name") ? p.get("name").getAsString() : "";
                }
            }

            // Difficulté
            String difficulty = obj.has("level")           ? obj.get("level").getAsString()           :
                    obj.has("difficultyLevel")  ? obj.get("difficultyLevel").getAsString() : "Beginner";

            // Durée
            String duration = obj.has("workload")       ? obj.get("workload").getAsString()       :
                    obj.has("estimatedTime")   ? obj.get("estimatedTime").getAsString()  : "4 semaines";

            // URL
            String courseUrl = "";
            if (obj.has("slug")) {
                courseUrl = "https://www.coursera.org/learn/" + obj.get("slug").getAsString();
            } else if (obj.has("url")) {
                courseUrl = obj.get("url").getAsString();
            }

            // Type
            String type = obj.has("courseType") ? obj.get("courseType").getAsString() :
                    obj.has("type")        ? obj.get("type").getAsString()        : "course";

            return new CourseraCourse(id, title, desc, partner, difficulty, duration, courseUrl, type);

        } catch (Exception e) {
            System.err.println("⚠️ Erreur parsing course : " + e.getMessage());
            return null;
        }
    }

    // ===== DONNÉES LOCALES DE SECOURS =====
    private List<CourseraCourse> getFallbackCourses(String keyword) {
        List<CourseraCourse> fallback = new ArrayList<>();
        String kw = keyword.toLowerCase();

        String[][] data = {
                {"java-programming", "Java Programming Masterclass", "Maîtrisez Java de zéro à expert.", "Tim Buchalka", "Beginner", "80 heures", "en ligne"},
                {"python-everybody", "Python for Everybody", "Introduction à Python pour la data.", "University of Michigan", "Beginner", "8 mois", "en ligne"},
                {"machine-learning", "Machine Learning Specialization", "Fondements du machine learning.", "Stanford / Andrew Ng", "Intermediate", "3 mois", "en ligne"},
                {"web-design", "Full Stack Web Development", "HTML, CSS, JavaScript, React, Node.", "The Hong Kong University", "Intermediate", "6 mois", "en ligne"},
                {"google-project", "Google Project Management", "Gestion de projet professionnelle.", "Google", "Beginner", "6 mois", "en ligne"},
                {"deep-learning", "Deep Learning Specialization", "Réseaux de neurones et IA.", "deeplearning.ai", "Advanced", "5 mois", "en ligne"},
                {"sql-databases", "SQL for Data Science", "Maîtrisez SQL pour l'analyse de données.", "UC Davis", "Beginner", "4 semaines", "en ligne"},
                {"cloud-aws", "AWS Cloud Solutions Architect", "Architecture cloud sur Amazon AWS.", "Amazon", "Intermediate", "4 mois", "en ligne"},
                {"cybersecurity", "Google Cybersecurity Certificate", "Fondements de la cybersécurité.", "Google", "Beginner", "6 mois", "en ligne"},
                {"agile-scrum", "Agile Project Management", "Méthodes agiles et Scrum en pratique.", "Google", "Beginner", "4 semaines", "en ligne"},
                {"data-science", "IBM Data Science Professional", "Data science complète avec IBM.", "IBM", "Beginner", "11 mois", "en ligne"},
                {"android-dev", "Android App Development", "Développement Android avec Kotlin.", "Meta", "Intermediate", "7 mois", "en ligne"},
                {"react-native", "React Native Development", "Applications mobiles avec React Native.", "Meta", "Intermediate", "4 mois", "en ligne"},
                {"devops", "DevOps on AWS Specialization", "CI/CD, Docker, Kubernetes sur AWS.", "Amazon", "Advanced", "3 mois", "en ligne"},
                {"leadership", "Leadership and Management", "Leadership et management d'équipe.", "HEC Paris", "Beginner", "5 mois", "en ligne"},
        };

        for (String[] d : data) {
            if (d[1].toLowerCase().contains(kw) || d[2].toLowerCase().contains(kw) ||
                    d[0].toLowerCase().contains(kw)  || d[3].toLowerCase().contains(kw)) {
                fallback.add(new CourseraCourse(
                        d[0], d[1], d[2], d[3], d[4], d[5],
                        "https://www.coursera.org/learn/" + d[0], d[6]));
            }
        }

        // Si aucune correspondance → retourner les 5 premiers
        if (fallback.isEmpty()) {
            for (int i = 0; i < Math.min(5, data.length); i++) {
                String[] d = data[i];
                fallback.add(new CourseraCourse(
                        d[0], d[1], d[2], d[3], d[4], d[5],
                        "https://www.coursera.org/learn/" + d[0], d[6]));
            }
        }

        return fallback;
    }
}