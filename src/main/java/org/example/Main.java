package org.example;

import org.example.enums.SalaireStatus;
import org.example.model.formation.Skill;
import org.example.model.formation.TrainingProgram;
import org.example.model.salaire.Salaire;
import org.example.services.formation.SkillService;
import org.example.services.formation.TrainingProgramService;
import org.example.services.salaire.BonusRuleService;
import org.example.services.salaire.SalaireService;
import org.example.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;


//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        SalaireService salaireService = new SalaireService();
        BonusRuleService bonusRuleService = new BonusRuleService();

        // test add usr

        /*String sqlUser = "INSERT INTO useraccount(name, role, email) VALUES (?, ?, ?)";

        // Ajouter un MANAGER
        try (PreparedStatement ps = conn.prepareStatement(sqlUser)) {
            ps.setString(1, "Mohamed Manager");
            ps.setString(2, "MANAGER");
            ps.setString(3, "Mohamed.manager@mail.com");
            ps.executeUpdate();
            System.out.println("✅ Utilisateur MANAGER ajouté avec succès");
        } catch (Exception e) {
            System.out.println("Erreur insertion MANAGER : " + e.getMessage());
        }

        // Ajouter un EMPLOYE
        try (PreparedStatement ps = conn.prepareStatement(sqlUser)) {
            ps.setString(1, "Rayen Employe");
            ps.setString(2, "EMPLOYE");
            ps.setString(3, "rayen.employe@mail.com");
            ps.executeUpdate();
            System.out.println("✅ Utilisateur EMPLOYE ajouté avec succès");
        } catch (Exception e) {
            System.out.println("Erreur insertion EMPLOYE : " + e.getMessage());
        }*/


        // add un salaire

        /*Salaire salaire = new Salaire(
            2,                       // userId
            3500,                    // baseAmount
            LocalDate.of(2026, 6, 11) // datePaiement
        );
        salaireService.create(salaire);
        Salaire salaire2 = new Salaire(
                1,                       // userId
                40000,                    // baseAmount
                LocalDate.of(2026, 7, 11) // datePaiement
        );
        salaireService.create(salaire2);*/


        // Lire tous les salaires
        /*List<Salaire> salaires = salaireService.getAll();
        System.out.println("===== LISTE DES SALAIRES =====");
        for (Salaire s : salaires) {
            System.out.println("------------------------");
            System.out.println(s); // utilise toString()
        }*/

        //Lire un salaire par ID
        /*Salaire salaire = salaireService.getById(1);
        if (salaire != null) {
            System.out.println("===== SALAIRE TROUVÉ =====");
            System.out.println(salaire);
        } else {
            System.out.println("❌ Aucun salaire trouvé pour l’ID 1");
        }*/

        // Mettre à jour un salaire
        // ==========================================
        /*Salaire salaire2 = salaireService.getById(2);
        if (salaire2 != null) {
            salaire2.setStatus(SalaireStatus.EN_COURS);
            salaire2.setDatePaiement(LocalDate.of(2026, 3, 1));
            salaireService.update(salaire2); // utilise GlobalInterface update
            System.out.println("✏️ Salaire mis à jour");
            System.out.println(salaire2);
        }*/
        // Supprimer un salaire
        // ==========================================
        //salaireService.delete(1);


// --- TEST SKILL ---
        System.out.println("📚 TEST SKILL\n");
        SkillService skillService = new SkillService();

        // CREATE (2 exemples)
        System.out.println("--- CREATE ---");
        Skill skill1 = new Skill("Java", "Langage de programmation", "technique");
        skill1.setLevelRequired(3);
        skillService.create(skill1);

        Skill skill2 = new Skill("Communication", "Compétence relationnelle", "soft");
        skill2.setLevelRequired(2);
        skillService.create(skill2);

        // UPDATE
        System.out.println("\n--- UPDATE ---");
        skill1.setLevelRequired(4);
        skill1.setDescription("Langage POO très populaire");
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