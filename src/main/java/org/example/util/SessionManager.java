package org.example.util;

import org.example.model.user.UserAccount;
import org.example.enums.UserRole;

public class SessionManager {

    private static UserAccount currentUser;

    public static void setCurrentUser(UserAccount user) { currentUser = user; }
    public static UserAccount getCurrentUser()          { return currentUser; }
    public static void clear()                          { currentUser = null; }

    public static boolean isAdmin() {
        return currentUser != null &&
                (currentUser.getRole() == UserRole.ADMINISTRATEUR
                        || currentUser.getRole() == UserRole.MANAGER);
    }

    public static boolean isUser() {
        return currentUser != null &&
                currentUser.getRole() == UserRole.EMPLOYE;
    }

    public static int getUserId() {
        return currentUser != null ? currentUser.getUserId() : -1;
    }

    public static String getUsername() {
        return currentUser != null ? currentUser.getUsername() : "Utilisateur";
    }
}