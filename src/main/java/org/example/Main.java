package org.example;

import org.example.model.formation.Skill;
import org.example.model.formation.TrainingProgram;
import org.example.services.formation.SkillService;
import org.example.services.formation.TrainingProgramService;
import org.example.util.DatabaseConnection;

import java.sql.Connection;
import java.util.Date;


//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        Connection conn = DatabaseConnection.getInstance().getConnection();

// --- TEST SKILL ---
        System.out.println("📚 TEST SKILL\n");
        SkillService skillService = new SkillService();

        // CREATE (2 exemples)
        System.out.println("--- CREATE ---");
        Skill skill1 = new Skill("symfony", "tech web", "technique",2);
        skill1.setLevelRequired(3);
        skillService.create(skill1);

        Skill skill2 = new Skill("Communication", "Compétence relationnelle", "soft",3);
        skill2.setLevelRequired(2);
        skillService.create(skill2);

        // UPDATE
        System.out.println("\n--- UPDATE ---");
        skill1.setLevelRequired(4);
        skill1.setDescription("technologie web");
        skillService.update(skill1);

        // DELETE
        System.out.println("\n--- DELETE ---");
        skillService.delete(skill2.getId());

        // Affichage final
        System.out.println("\n--- LISTE FINALE ---");
        skillService.displayAll();

        // --- TEST TRAINING PROGRAM ---
        System.out.println("🎓 TEST TRAINING PROGRAM\n");
        TrainingProgramService trainingService = new TrainingProgramService();

        // CREATE (2 exemples)
        System.out.println("--- CREATE ---");
        TrainingProgram training1 = new TrainingProgram(
                "Formation Spring Boot",
                "Développement avec Spring",
                40,
                new Date(),
                new Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000),
                "en ligne"
        );
        trainingService.create(training1);

        TrainingProgram training2 = new TrainingProgram(
                "Formation Leadership",
                "Management d'équipe",
                20,
                new Date(),
                new Date(System.currentTimeMillis() + 15L * 24 * 60 * 60 * 1000),
                "présentiel"
        );
        trainingService.create(training2);

        // UPDATE
        System.out.println("\n--- UPDATE ---");
        training1.setDuration(50);
        training1.setType("présentiel");
        trainingService.update(training1);

        // DELETE
        System.out.println("\n--- DELETE ---");
        trainingService.delete(training2.getId());

        // Affichage final
        System.out.println("\n--- LISTE FINALE ---");
        trainingService.displayAll();





    }
}