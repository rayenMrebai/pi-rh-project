package org.example.util;

import org.example.model.user.UserAccount;

/**
 * Classe singleton pour gérer la session utilisateur
 */
public class SessionManager {

    private static UserAccount currentUser;

    public static void setCurrentUser(UserAccount user) {
        currentUser = user;
        if (user != null) {
            System.out.println("✅ Session créée pour : " + user.getUsername());
        }
    }
    public static Integer getUserId() {
        return currentUser != null ? currentUser.getUserId() : null;
    }

    public static String getUsername() {
        return currentUser != null ? currentUser.getUsername() : null;
    }
    public static UserAccount getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static void clearSession() {
        if (currentUser != null) {
            System.out.println("🔒 Session fermée pour : " + currentUser.getUsername());
        }
        currentUser = null;
    }

    public static boolean hasRole(String role) {
        return currentUser != null &&
                currentUser.getRole().toString().equalsIgnoreCase(role);
    }
}