package org.example;

import org.example.enums.SalaireStatus;
import org.example.model.salaire.Salaire;
import org.example.services.salaire.BonusRuleService;
import org.example.services.salaire.SalaireService;
import org.example.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
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








    }
}