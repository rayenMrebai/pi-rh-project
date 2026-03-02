package org.example.util;

import org.example.enums.UserRole;
import org.example.model.user.UserAccount;

public class SessionManager {

    private static UserAccount currentUser;

    /**
     * Enregistre l'utilisateur connecté
     */
    public static void setCurrentUser(UserAccount user) {
        currentUser = user;
    }

    /**
     * Récupère l'utilisateur connecté
     */
    public static UserAccount getCurrentUser() {
        return currentUser;
    }

    /**
     * Récupère le rôle de l'utilisateur connecté
     */
    public static UserRole getCurrentRole() {
        return currentUser != null ? currentUser.getRole() : null;
    }

    /**
     * Vérifie si l'utilisateur est admin
     */
    public static boolean isAdmin() {
        return currentUser != null && currentUser.getRole() == UserRole.ADMINISTRATEUR;
    }

    /**
     * Vérifie si l'utilisateur est employé
     */
    public static boolean isEmployee() {
        return currentUser != null && currentUser.getRole() == UserRole.EMPLOYE;
    }

    /**
     * Vérifie si l'utilisateur est manager
     */
    public static boolean isManager() {
        return currentUser != null && currentUser.getRole() == UserRole.MANAGER;
    }

    /**
     * Déconnexion
     */
    public static void logout() {
        currentUser = null;
    }

    /**
     * Vérifie si un utilisateur est connecté
     */
    public static boolean isLoggedIn() {
        return currentUser != null;
    }
}