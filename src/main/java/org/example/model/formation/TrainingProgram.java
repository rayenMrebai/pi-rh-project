package org.example.model.formation;

import java.security.PrivateKey;
import java.util.Date;

public class TrainingProgram {
    //Attributs
    private int id;
    private String title;
    private String description;
    private int duration;
    private Date startDate;
    private Date endDate;
    private String type;
    //Constructeurs
    public TrainingProgram() {}
    public TrainingProgram(int id, String title, String description, int duration, Date startDate, Date endDate, String type) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.duration = duration;
        this.startDate = startDate;
        this.endDate = endDate;
        this.type = type;
    }
    public TrainingProgram( String title, String description, int duration, Date startDate, Date endDate, String type) {
        this.title = title;
        this.description = description;
        this.duration = duration;
        this.startDate = startDate;
        this.endDate = endDate;
        this.type = type;
    }
    //Getters
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getDuration() {
        return duration;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public String getType() {
        return type;
    }
    //Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public void setType(String type) {
        this.type = type;
    }
    //toString
    @Override
    public String toString() {
        return "TrainingProgram{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", duration=" + duration +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", type='" + type + '\'' +
                '}';
    }

}

