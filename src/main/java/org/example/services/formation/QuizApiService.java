package org.example.services.formation;

import com.google.gson.*;
import okhttp3.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class QuizApiService {

    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String API_KEY = "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"; // ✅ Votre clé Groq ici
    private static final String MODEL = "llama-3.3-70b-versatile";

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    // ===== CLASSE QUESTION =====
    public static class QuizQuestion {
        private final String question;
        private final String correctAnswer;
        private final List<String> allAnswers;
        private final String difficulty;

        public QuizQuestion(String question, String correctAnswer,
                            List<String> allAnswers, String difficulty) {
            this.question      = question;
            this.correctAnswer = correctAnswer;
            this.allAnswers    = allAnswers;
            this.difficulty    = difficulty;
        }

        public String getQuestion()        { return question; }
        public String getCorrectAnswer()   { return correctAnswer; }
        public List<String> getAllAnswers() { return allAnswers; }
        public String getDifficulty()      { return difficulty; }
        public String getCategory()        { return "Formation"; }
    }

    // ===== MÉTHODE PRINCIPALE =====
    public List<QuizQuestion> fetchQuestionsForTraining(
            String trainingTitle,
            String trainingDescription,
            String difficulty) {

        System.out.println("\n========================================");
        System.out.println("🤖 Génération quiz via Groq pour : " + trainingTitle);
        System.out.println("📊 Difficulté : " + difficulty);
        System.out.println("========================================");

        if (API_KEY.equals("gsk_xxxxxxxxxxxxxxxx") || API_KEY.isEmpty()) {
            System.err.println("❌ Clé Groq non configurée ! Utilisation du fallback.");
            return getFallbackQuestions(trainingTitle, difficulty);
        }

        List<QuizQuestion> questions = new ArrayList<>();

        try {
            // ✅ Body JSON format OpenAI compatible (Groq utilise le même format)
            JsonObject body = new JsonObject();
            body.addProperty("model", MODEL);
            body.addProperty("max_tokens", 3000);
            body.addProperty("temperature", 0.7);

            JsonArray messages = new JsonArray();

            // System message
            JsonObject systemMsg = new JsonObject();
            systemMsg.addProperty("role", "system");
            systemMsg.addProperty("content",
                    "Tu es un expert en formation professionnelle. " +
                            "Tu génères des questions QCM pertinentes sur les formations. " +
                            "Tu réponds UNIQUEMENT avec du JSON valide, sans texte avant ou après.");
            messages.add(systemMsg);

            // User message
            JsonObject userMsg = new JsonObject();
            userMsg.addProperty("role", "user");
            userMsg.addProperty("content",
                    buildPrompt(trainingTitle, trainingDescription, difficulty));
            messages.add(userMsg);

            body.add("messages", messages);

            Request request = new Request.Builder()
                    .url(API_URL)
                    .addHeader("Content-Type",  "application/json")
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .post(RequestBody.create(
                            body.toString(),
                            MediaType.get("application/json; charset=utf-8")))
                    .build();

            try (Response response = client.newCall(request).execute()) {
                System.out.println("📡 Groq HTTP Status : " + response.code());

                if (response.body() == null) {
                    System.err.println("❌ Réponse vide");
                    return getFallbackQuestions(trainingTitle, difficulty);
                }

                String responseBody = response.body().string();

                if (!response.isSuccessful()) {
                    System.err.println("❌ Erreur Groq " + response.code()
                            + " : " + responseBody);
                    return getFallbackQuestions(trainingTitle, difficulty);
                }

                System.out.println("✅ Réponse Groq reçue");
                questions = parseGroqResponse(responseBody, difficulty);
            }

        } catch (Exception e) {
            System.err.println("❌ Exception : " + e.getClass().getSimpleName()
                    + " — " + e.getMessage());
            e.printStackTrace();
        }

        // ✅ Garantir exactement 10 questions
        if (questions.isEmpty()) {
            System.out.println("⚠️ Aucune question parsée → fallback");
            questions = new ArrayList<>(getFallbackQuestions(trainingTitle, difficulty));
        }
        while (questions.size() < 10) {
            questions.addAll(getFallbackQuestions(trainingTitle, difficulty));
        }
        if (questions.size() > 10) {
            questions = new ArrayList<>(questions.subList(0, 10));
        }

        System.out.println("✅ Total final : " + questions.size() + " questions");
        System.out.println("========================================\n");
        return questions;
    }

    // ===== PROMPT =====
    private String buildPrompt(String title, String description, String difficulty) {
        return "Génère EXACTEMENT 10 questions QCM de niveau " + difficulty +
                " en français sur cette formation :\n\n" +
                "Titre : " + title + "\n" +
                "Description : " + (description != null && !description.isEmpty()
                ? description : "Formation professionnelle sur " + title) + "\n\n" +
                "RÈGLES ABSOLUES :\n" +
                "- EXACTEMENT 10 questions sur le contenu de cette formation\n" +
                "- Chaque question a exactement 4 choix\n" +
                "- Une seule bonne réponse par question\n" +
                "- Questions en français\n" +
                "- Réponds UNIQUEMENT avec le tableau JSON, rien d'autre\n\n" +
                "FORMAT JSON (10 objets obligatoires) :\n" +
                "[\n" +
                "  {\"question\":\"Question 1 ?\",\"correct\":\"Bonne réponse\",\"wrong1\":\"Mauvais choix\",\"wrong2\":\"Mauvais choix\",\"wrong3\":\"Mauvais choix\"},\n" +
                "  {\"question\":\"Question 2 ?\",\"correct\":\"Bonne réponse\",\"wrong1\":\"Mauvais choix\",\"wrong2\":\"Mauvais choix\",\"wrong3\":\"Mauvais choix\"},\n" +
                "  {\"question\":\"Question 3 ?\",\"correct\":\"Bonne réponse\",\"wrong1\":\"Mauvais choix\",\"wrong2\":\"Mauvais choix\",\"wrong3\":\"Mauvais choix\"},\n" +
                "  {\"question\":\"Question 4 ?\",\"correct\":\"Bonne réponse\",\"wrong1\":\"Mauvais choix\",\"wrong2\":\"Mauvais choix\",\"wrong3\":\"Mauvais choix\"},\n" +
                "  {\"question\":\"Question 5 ?\",\"correct\":\"Bonne réponse\",\"wrong1\":\"Mauvais choix\",\"wrong2\":\"Mauvais choix\",\"wrong3\":\"Mauvais choix\"},\n" +
                "  {\"question\":\"Question 6 ?\",\"correct\":\"Bonne réponse\",\"wrong1\":\"Mauvais choix\",\"wrong2\":\"Mauvais choix\",\"wrong3\":\"Mauvais choix\"},\n" +
                "  {\"question\":\"Question 7 ?\",\"correct\":\"Bonne réponse\",\"wrong1\":\"Mauvais choix\",\"wrong2\":\"Mauvais choix\",\"wrong3\":\"Mauvais choix\"},\n" +
                "  {\"question\":\"Question 8 ?\",\"correct\":\"Bonne réponse\",\"wrong1\":\"Mauvais choix\",\"wrong2\":\"Mauvais choix\",\"wrong3\":\"Mauvais choix\"},\n" +
                "  {\"question\":\"Question 9 ?\",\"correct\":\"Bonne réponse\",\"wrong1\":\"Mauvais choix\",\"wrong2\":\"Mauvais choix\",\"wrong3\":\"Mauvais choix\"},\n" +
                "  {\"question\":\"Question 10 ?\",\"correct\":\"Bonne réponse\",\"wrong1\":\"Mauvais choix\",\"wrong2\":\"Mauvais choix\",\"wrong3\":\"Mauvais choix\"}\n" +
                "]";
    }

    // ===== PARSER RÉPONSE GROQ =====
    private List<QuizQuestion> parseGroqResponse(String json, String difficulty) {
        List<QuizQuestion> questions = new ArrayList<>();
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();

            // ✅ Groq utilise le même format qu'OpenAI
            String content = root.getAsJsonArray("choices")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content").getAsString();

            System.out.println("📦 Contenu Groq (100 chars) : "
                    + content.substring(0, Math.min(content.length(), 100)));

            // ✅ Nettoyer markdown
            content = content.trim();
            if (content.startsWith("```json")) content = content.substring(7).trim();
            else if (content.startsWith("```"))  content = content.substring(3).trim();
            if (content.endsWith("```"))         content = content.substring(0, content.length() - 3).trim();

            // ✅ Extraire le tableau JSON
            int start = content.indexOf('[');
            int end   = content.lastIndexOf(']');
            if (start != -1 && end != -1 && end > start) {
                content = content.substring(start, end + 1);
            }

            JsonArray arr = JsonParser.parseString(content).getAsJsonArray();
            System.out.println("📋 Objets JSON reçus : " + arr.size());

            for (JsonElement el : arr) {
                try {
                    JsonObject obj = el.getAsJsonObject();

                    String question = obj.get("question").getAsString();
                    String correct  = obj.get("correct").getAsString();
                    String wrong1   = obj.get("wrong1").getAsString();
                    String wrong2   = obj.get("wrong2").getAsString();
                    String wrong3   = obj.get("wrong3").getAsString();

                    List<String> answers = new ArrayList<>();
                    answers.add(correct);
                    answers.add(wrong1);
                    answers.add(wrong2);
                    answers.add(wrong3);
                    Collections.shuffle(answers);

                    questions.add(new QuizQuestion(question, correct, answers, difficulty));

                } catch (Exception ex) {
                    System.err.println("⚠️ Erreur parsing question : " + ex.getMessage());
                }
            }

            System.out.println("✅ Questions parsées : " + questions.size());

        } catch (Exception e) {
            System.err.println("❌ Erreur parsing : " + e.getMessage());
            e.printStackTrace();
        }
        return questions;
    }

    // ===== FALLBACK 10 QUESTIONS =====
    private List<QuizQuestion> getFallbackQuestions(String title, String difficulty) {
        System.out.println("⚠️ Fallback pour : " + title);
        List<QuizQuestion> fallback = new ArrayList<>();

        String[][] data = {
                {"Quel est l'objectif principal de la formation '" + title + "' ?",
                        "Acquérir des compétences spécifiques au domaine",
                        "Obtenir un diplôme universitaire",
                        "Apprendre une langue étrangère",
                        "Gérer un projet informatique"},
                {"Quelle est la meilleure approche pour maîtriser '" + title + "' ?",
                        "Pratiquer régulièrement et appliquer les concepts",
                        "Lire uniquement la théorie",
                        "Regarder des vidéos sans pratiquer",
                        "Copier le travail des autres"},
                {"Quel type de compétence développe '" + title + "' ?",
                        "Compétences techniques et pratiques",
                        "Compétences culinaires",
                        "Compétences sportives",
                        "Compétences musicales"},
                {"Comment évalue-t-on les acquis de '" + title + "' ?",
                        "Par des exercices pratiques et des évaluations",
                        "Uniquement par un examen écrit",
                        "Par un entretien oral uniquement",
                        "Aucune évaluation nécessaire"},
                {"Quel format est recommandé pour suivre '" + title + "' ?",
                        "Combiner théorie et pratique progressivement",
                        "Tout apprendre en une journée",
                        "Se concentrer sur les exercices uniquement",
                        "Ignorer la documentation officielle"},
                {"Quelle attitude est essentielle pour réussir '" + title + "' ?",
                        "Curiosité et persévérance",
                        "Impatience et précipitation",
                        "Passivité et attente",
                        "Éviter les difficultés rencontrées"},
                {"Quel est le meilleur moyen de consolider '" + title + "' ?",
                        "Réaliser des projets concrets liés à la formation",
                        "Mémoriser sans comprendre",
                        "Éviter les exercices complexes",
                        "Ne jamais poser de questions"},
                {"Comment '" + title + "' contribue-t-elle au développement professionnel ?",
                        "En renforçant les compétences métier et l'employabilité",
                        "En remplaçant l'expérience professionnelle",
                        "En offrant uniquement un certificat",
                        "En limitant les opportunités de carrière"},
                {"Quel est le rôle du formateur dans '" + title + "' ?",
                        "Guider, accompagner et évaluer les participants",
                        "Faire le travail à la place des participants",
                        "Fournir uniquement les supports de cours",
                        "Ignorer les questions des participants"},
                {"Quelle est la durée idéale pour assimiler '" + title + "' ?",
                        "Progressivement sur toute la durée de la formation",
                        "En une seule session intensive",
                        "Uniquement pendant les pauses",
                        "La durée n'a aucune importance"}
        };

        for (String[] d : data) {
            List<String> answers = new ArrayList<>();
            answers.add(d[1]);
            answers.add(d[2]);
            answers.add(d[3]);
            answers.add(d[4]);
            Collections.shuffle(answers);
            fallback.add(new QuizQuestion(d[0], d[1], answers, difficulty));
        }

        return fallback; // ✅ exactement 10
    }
}