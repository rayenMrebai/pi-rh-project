package org.example;

import org.example.model.salaire.Salaire;
import org.example.services.salaire.BonusRuleService;
import org.example.services.salaire.SalaireService;
import org.example.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;

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

        Salaire salaire = new Salaire(
            2,                       // userId
            3500,                    // baseAmount
            LocalDate.of(2026, 6, 11) // datePaiement
        );
        salaireService.create(salaire);





    }
}