package org.example;

import org.example.model.recrutement.Candidat;
import org.example.model.recrutement.JobPosition;
import org.example.services.recrutement.CandidatService;
import org.example.services.recrutement.JobPositionService;

import java.time.LocalDate;
import java.util.List;

public class TestRecrutement {

    public static void main(String[] args) {

        CandidatService cs = new CandidatService();
        JobPositionService js = new JobPositionService();

        // =============================
        // 1️⃣ CREATE JOB POSITION
        // =============================
        JobPosition job = new JobPosition(
                "Dev Java",
                "IT",
                "CDI",
                "Backend Java Developer",
                "Open",
                LocalDate.now()
        );

        js.create(job);

        System.out.println("JobPosition ajouté ✅");

        // =============================
        // 2️⃣ RÉCUPÉRER UN JOB EXISTANT
        // =============================
        List<JobPosition> jobs = js.getAll();
        JobPosition existingJob = jobs.get(jobs.size() - 1); // dernier ajouté

        System.out.println("Job récupéré : " + existingJob);

        // =============================
        // 3️⃣ CREATE CANDIDAT LIÉ AU JOB
        // =============================
        Candidat candidat = new Candidat(
                "Youssef",
                "Hammami",
                "youssef@mail.com",
                12345678,
                "Licence",
                "Java, SQL",
                "New",
                existingJob // objet contenant l'idJob
        );

        cs.create(candidat);

        System.out.println("Candidat ajouté ✅");

        // =============================
        // 4️⃣ AFFICHAGE FINAL
        // =============================
        System.out.println("===== LISTE DES JOBS =====");
        js.getAll().forEach(System.out::println);

        System.out.println("===== LISTE DES CANDIDATS =====");
        cs.getAll().forEach(System.out::println);
    }
}
