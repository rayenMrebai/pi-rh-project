package org.example.services;

import com.google.gson.*;
import okhttp3.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class QuizApiService {

    // ✅ Open Trivia DB — 100% gratuite, sans clé API
    private static final String BASE_URL = "https://opentdb.com/api.php";

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build();

    public static class QuizQuestion {
        private String question;
        private String correctAnswer;
        private List<String> allAnswers; // mélangées
        private String difficulty;
        private String category;

        public QuizQuestion(String question, String correctAnswer,
                            List<String> allAnswers, String difficulty, String category) {
            this.question      = question;
            this.correctAnswer = correctAnswer;
            this.allAnswers    = allAnswers;
            this.difficulty    = difficulty;
            this.category      = category;
        }

        public String getQuestion()         { return question; }
        public String getCorrectAnswer()    { return correctAnswer; }
        public List<String> getAllAnswers()  { return allAnswers; }
        public String getDifficulty()       { return difficulty; }
        public String getCategory()         { return category; }
    }

    // ✅ Récupérer questions depuis Open Trivia DB
    public List<QuizQuestion> fetchQuestions(int amount, String difficulty) {
        List<QuizQuestion> questions = new ArrayList<>();

        // category=18 = Computers, difficulty = easy/medium/hard
        String url = BASE_URL
                + "?amount=" + amount
                + "&category=18"
                + "&difficulty=" + difficulty
                + "&type=multiple"
                + "&encode=base64";

        System.out.println("🎯 Quiz API URL: " + url);

        try {
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Accept", "application/json")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                System.out.println("📡 Status: " + response.code());
                if (!response.isSuccessful() || response.body() == null)
                    return getFallbackQuestions(difficulty);

                String json = response.body().string();
                JsonObject root = JsonParser.parseString(json).getAsJsonObject();

                int responseCode = root.get("response_code").getAsInt();
                if (responseCode != 0) {
                    System.out.println("⚠️ API response_code: " + responseCode + " → fallback");
                    return getFallbackQuestions(difficulty);
                }

                JsonArray results = root.getAsJsonArray("results");
                for (JsonElement el : results) {
                    JsonObject obj = el.getAsJsonObject();

                    // Base64 decode
                    String question       = decode(obj.get("question").getAsString());
                    String correctAnswer  = decode(obj.get("correct_answer").getAsString());
                    String difficulty2    = decode(obj.get("difficulty").getAsString());
                    String category       = decode(obj.get("category").getAsString());

                    List<String> answers = new ArrayList<>();
                    answers.add(correctAnswer);
                    for (JsonElement inc : obj.getAsJsonArray("incorrect_answers")) {
                        answers.add(decode(inc.getAsString()));
                    }
                    Collections.shuffle(answers);

                    questions.add(new QuizQuestion(question, correctAnswer, answers, difficulty2, category));
                }

                System.out.println("✅ Questions reçues: " + questions.size());

            }
        } catch (Exception e) {
            System.err.println("❌ Erreur QuizAPI: " + e.getMessage());
            return getFallbackQuestions(difficulty);
        }

        return questions.isEmpty() ? getFallbackQuestions(difficulty) : questions;
    }

    private String decode(String base64) {
        try {
            return new String(java.util.Base64.getDecoder().decode(base64));
        } catch (Exception e) {
            return base64;
        }
    }

    // ✅ Questions locales de secours
    private List<QuizQuestion> getFallbackQuestions(String difficulty) {
        List<QuizQuestion> fallback = new ArrayList<>();

        String[][][] data = {
                {
                        {"What does HTML stand for?"},
                        {"HyperText Markup Language"},
                        {"HyperText Markup Language", "High Tech Modern Language", "HyperTransfer Markup Language", "Home Tool Markup Language"},
                        {"easy"}, {"Web"}
                },
                {
                        {"Which language is used for styling web pages?"},
                        {"CSS"},
                        {"CSS", "Java", "Python", "C++"},
                        {"easy"}, {"Web"}
                },
                {
                        {"What does OOP stand for?"},
                        {"Object-Oriented Programming"},
                        {"Object-Oriented Programming", "Open Object Protocol", "Oriented Object Procedure", "Object Operating Process"},
                        {"easy"}, {"Programming"}
                },
                {
                        {"Which of these is a Java framework?"},
                        {"Spring"},
                        {"Spring", "Django", "Laravel", "Rails"},
                        {"medium"}, {"Java"}
                },
                {
                        {"What is a REST API?"},
                        {"Representational State Transfer"},
                        {"Representational State Transfer", "Remote Server Transfer", "Rapid State Technology", "Real-time Server Transport"},
                        {"medium"}, {"API"}
                },
                {
                        {"What does SQL stand for?"},
                        {"Structured Query Language"},
                        {"Structured Query Language", "Simple Question Language", "Sequential Query Logic", "System Query Language"},
                        {"easy"}, {"Database"}
                },
                {
                        {"Which HTTP method is used to update a resource?"},
                        {"PUT"},
                        {"PUT", "GET", "POST", "DELETE"},
                        {"medium"}, {"HTTP"}
                },
                {
                        {"What is the time complexity of binary search?"},
                        {"O(log n)"},
                        {"O(log n)", "O(n)", "O(n²)", "O(1)"},
                        {"hard"}, {"Algorithms"}
                },
                {
                        {"Which design pattern ensures only one instance of a class?"},
                        {"Singleton"},
                        {"Singleton", "Factory", "Observer", "Decorator"},
                        {"hard"}, {"Design Patterns"}
                },
                {
                        {"What is Docker used for?"},
                        {"Containerization"},
                        {"Containerization", "Database management", "Code compilation", "Network routing"},
                        {"medium"}, {"DevOps"}
                },
        };

        for (String[][] q : data) {
            List<String> answers = new ArrayList<>();
            for (String a : q[2]) answers.add(a);
            Collections.shuffle(answers);
            fallback.add(new QuizQuestion(q[0][0], q[1][0], answers, q[3][0], q[4][0]));
        }

        return fallback;
    }
}