package org.example.services;

import com.google.gson.*;
import okhttp3.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class QuizApiService {

    // ✅ API Claude pour générer des questions sur la formation
    private static final String CLAUDE_URL = "https://api.anthropic.com/v1/messages";
    private static final String API_KEY    = "VOTRE_CLE_ANTHROPIC_ICI";
    private static final String MODEL      = "claude-3-haiku-20240307";

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    public static class QuizQuestion {
        private String question;
        private String correctAnswer;
        private List<String> allAnswers;
        private String difficulty;

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

    // ✅ Générer questions basées sur la formation
// Dans fetchQuestionsForTraining() — supprimer le paramètre amount
    public List<QuizQuestion> fetchQuestionsForTraining(
            String trainingTitle,
            String trainingDescription,
            String difficulty) {

        System.out.println("🤖 Génération quiz pour : " + trainingTitle);

        String prompt = buildPrompt(trainingTitle, trainingDescription, difficulty);

        List<QuizQuestion> questions = new ArrayList<>();

        try {
            JsonObject body = new JsonObject();
            body.addProperty("model", MODEL);
            body.addProperty("max_tokens", 2000);

            JsonArray messages = new JsonArray();
            JsonObject userMsg = new JsonObject();
            userMsg.addProperty("role", "user");
            userMsg.addProperty("content", prompt);
            messages.add(userMsg);
            body.add("messages", messages);

            Request request = new Request.Builder()
                    .url(CLAUDE_URL)
                    .addHeader("Content-Type",      "application/json")
                    .addHeader("x-api-key",         API_KEY)
                    .addHeader("anthropic-version", "2023-06-01")
                    .post(RequestBody.create(
                            body.toString(),
                            MediaType.get("application/json")))
                    .build();

            try (Response response = client.newCall(request).execute()) {
                System.out.println("📡 Claude Status : " + response.code());

                if (!response.isSuccessful() || response.body() == null) {
                    System.err.println("❌ Claude API erreur : " + response.code());
                    return getFallbackQuestions(trainingTitle, difficulty);
                }

                String json = response.body().string();
                questions = parseClaudeResponse(json, difficulty);
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur réseau Claude : " + e.getMessage());
            return getFallbackQuestions(trainingTitle, difficulty);
        }

        // ✅ Garantir exactement 10 questions
        if (questions.isEmpty()) {
            questions = new ArrayList<>(getFallbackQuestions(trainingTitle, difficulty));
        }
        while (questions.size() < 10) {
            questions.addAll(getFallbackQuestions(trainingTitle, difficulty));
        }
        if (questions.size() > 10) {
            questions = new ArrayList<>(questions.subList(0, 10));
        }

        System.out.println("✅ Total final : " + questions.size() + " questions");
        return questions;
    }

    // ✅ Construire le prompt pour Claude
    private String buildPrompt(String title, String description,
                               String difficulty) {
        return "Tu es un expert en formation professionnelle. " +
                "Génère EXACTEMENT 10 questions QCM (ni plus, ni moins) " +
                "de niveau " + difficulty + " sur la formation suivante :\n\n" +
                "Titre : " + title + "\n" +
                "Description : " + description + "\n\n" +
                "RÈGLES STRICTES :\n" +
                "- EXACTEMENT 10 questions, pas 8, pas 9, pas 11 — exactement 10\n" +
                "- Chaque question doit être directement liée au contenu de cette formation\n" +
                "- Chaque question a exactement 4 choix de réponse\n" +
                "- Une seule bonne réponse par question\n" +
                "- Réponds UNIQUEMENT en JSON valide, sans texte avant ou après\n\n" +
                "FORMAT JSON EXACT à respecter (10 objets dans le tableau) :\n" +
                "[\n" +
                "  {\n" +
                "    \"question\": \"texte de la question ?\",\n" +
                "    \"correct\": \"bonne réponse\",\n" +
                "    \"wrong1\": \"mauvaise réponse 1\",\n" +
                "    \"wrong2\": \"mauvaise réponse 2\",\n" +
                "    \"wrong3\": \"mauvaise réponse 3\"\n" +
                "  },\n" +
                "  ... (10 objets au total)\n" +
                "]";
    }

    // ✅ Parser la réponse de Claude
    private List<QuizQuestion> parseClaudeResponse(String json, String difficulty) {
        List<QuizQuestion> questions = new ArrayList<>();

        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();

            // Extraire le texte de la réponse Claude
            String content = root.getAsJsonArray("content")
                    .get(0).getAsJsonObject()
                    .get("text").getAsString();

            System.out.println("📦 Claude réponse reçue : " + content.substring(0, Math.min(content.length(), 100)));

            // Nettoyer le JSON (enlever les balises markdown si présentes)
            content = content.trim();
            if (content.startsWith("```json")) content = content.substring(7);
            if (content.startsWith("```"))     content = content.substring(3);
            if (content.endsWith("```"))       content = content.substring(0, content.length() - 3);
            content = content.trim();

            JsonArray arr = JsonParser.parseString(content).getAsJsonArray();

            for (JsonElement el : arr) {
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
            }

            System.out.println("✅ " + questions.size() + " questions générées");

        } catch (Exception e) {
            System.err.println("❌ Erreur parsing Claude : " + e.getMessage());
        }

        return questions;
    }

    // ✅ Fallback si Claude inaccessible
    private List<QuizQuestion> getFallbackQuestions(String title, String difficulty) {
        System.out.println("⚠️ Fallback questions pour : " + title);
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
                        "Copier le code des autres"},
                {"Quel type de compétence développe la formation '" + title + "' ?",
                        "Compétences techniques et pratiques",
                        "Compétences culinaires",
                        "Compétences sportives",
                        "Compétences musicales"},
        };

        for (String[] d : data) {
            List<String> answers = new ArrayList<>();
            answers.add(d[1]); answers.add(d[2]);
            answers.add(d[3]); answers.add(d[4]);
            Collections.shuffle(answers);
            fallback.add(new QuizQuestion(d[0], d[1], answers, difficulty));
        }

        return fallback;
    }
}