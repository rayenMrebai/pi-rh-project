package org.example.model.formation;

import java.time.LocalDateTime;

public class Quiz {
    private int id;
    private int userId;
    private int trainingId;
    private int score;
    private int totalQuestions;
    private double percentage;
    private boolean passed;
    private LocalDateTime completedAt;

    public Quiz() {}

    public int getId()                    { return id; }
    public int getUserId()                { return userId; }
    public int getTrainingId()            { return trainingId; }
    public int getScore()                 { return score; }
    public int getTotalQuestions()        { return totalQuestions; }
    public double getPercentage()         { return percentage; }
    public boolean isPassed()             { return passed; }
    public LocalDateTime getCompletedAt() { return completedAt; }

    public void setId(int id)                          { this.id = id; }
    public void setUserId(int userId)                  { this.userId = userId; }
    public void setTrainingId(int trainingId)          { this.trainingId = trainingId; }
    public void setScore(int score)                    { this.score = score; }
    public void setTotalQuestions(int t)               { this.totalQuestions = t; }
    public void setPercentage(double percentage)       { this.percentage = percentage; }
    public void setPassed(boolean passed)              { this.passed = passed; }
    public void setCompletedAt(LocalDateTime c)        { this.completedAt = c; }
}