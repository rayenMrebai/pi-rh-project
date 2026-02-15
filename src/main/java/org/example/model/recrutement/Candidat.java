package org.example.model.recrutement;

public class Candidat {

    private int id;
    private String firstName;
    private String lastName;
    private String email;
    private int phone;
    private String educationLevel;
    private String skills;
    private String status;
    private JobPosition jobPosition;

    // ✅ Constructeur vide
    public Candidat() {
    }

    // ✅ Constructeur avec paramètres
    public Candidat(String firstName, String lastName, String email,
                    int phone, String educationLevel, String skills,
                    String status, JobPosition jobPosition) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.educationLevel = educationLevel;
        this.skills = skills;
        this.status = status;
        this.jobPosition = jobPosition;
    }


    // ✅ Getters & Setters
    public int getId() {
        return id;
    }
    public String getFullName() {
        return (firstName == null ? "" : firstName) + " " + (lastName == null ? "" : lastName);
    }


    public void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getPhone() {
        return phone;
    }

    public void setPhone(int phone) {
        this.phone = phone;
    }

    public String getEducationLevel() {
        return educationLevel;
    }

    public void setEducationLevel(String educationLevel) {
        this.educationLevel = educationLevel;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public JobPosition getJobPosition() {
        return jobPosition;
    }

    public void setJobPosition(JobPosition jobPosition) {
        this.jobPosition = jobPosition;
    }

    // ✅ toString
    @Override
    public String toString() {
        return "Candidat{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", phone=" + phone +
                ", educationLevel='" + educationLevel + '\'' +
                ", skills='" + skills + '\'' +
                ", status='" + status + '\'' + ", jobPositionId=" + (jobPosition != null ? jobPosition.getIdJob() : null) +
                '}';
    }
}
