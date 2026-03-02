package org.example.test;

import org.example.services.user.UserAccountService;
import org.example.model.user.UserAccount;

public class Main {
    public static void main(String[] args) {
        // Exemple de test : afficher tous les utilisateurs
        UserAccountService userService = new UserAccountService();
        System.out.println("Liste des utilisateurs :");
        for (UserAccount user : userService.getAll()) {
            System.out.println(user.getUsername() + " - " + user.getEmail());
        }
    }
}