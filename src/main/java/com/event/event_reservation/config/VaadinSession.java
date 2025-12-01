package com.event.event_reservation.config;

import com.event.event_reservation.entity.User;
import com.event.event_reservation.entity.enums.UserRole;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Classe utilitaire pour gérer l'utilisateur connecté dans la session Vaadin
 */
public class VaadinSession {

    private static final String USER_SESSION_KEY = "current_user";

    /**
     * Récupérer l'utilisateur connecté depuis la session
     * @return User connecté ou null si non connecté
     */
    public static User getCurrentUser() {
        try {
            com.vaadin.flow.server.VaadinSession session =
                    com.vaadin.flow.server.VaadinSession.getCurrent();

            if (session != null) {
                Object userObj = session.getAttribute(USER_SESSION_KEY);
                if (userObj instanceof User) {
                    return (User) userObj;
                }
            }
        } catch (Exception e) {
            // Si pas de session Vaadin (contexte non-web), retourner null
            return null;
        }

        return null;
    }

    /**
     * Stocker l'utilisateur connecté dans la session
     * @param user Utilisateur à stocker
     */
    public static void setCurrentUser(User user) {
        try {
            com.vaadin.flow.server.VaadinSession session =
                    com.vaadin.flow.server.VaadinSession.getCurrent();

            if (session != null) {
                session.setAttribute(USER_SESSION_KEY, user);
            }
        } catch (Exception e) {
            // Ignorer si pas de session Vaadin
        }
    }

    /**
     * Vérifier si un utilisateur est connecté
     * @return true si connecté, false sinon
     */
    public static boolean isUserLoggedIn() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() &&
                !authentication.getPrincipal().equals("anonymousUser")) {
            return true;
        }

        return getCurrentUser() != null;
    }

    /**
     * Déconnecter l'utilisateur (supprimer de la session)
     */
    public static void logout() {
        try {
            com.vaadin.flow.server.VaadinSession session =
                    com.vaadin.flow.server.VaadinSession.getCurrent();

            if (session != null) {
                session.setAttribute(USER_SESSION_KEY, null);
            }
        } catch (Exception e) {
            // Ignorer si pas de session Vaadin
        }
    }

    /**
     * Vérifier si l'utilisateur a un rôle spécifique
     * @param role Nom du rôle (ex: "ADMIN", "CLIENT", "ORGANIZER")
     * @return true si l'utilisateur a ce rôle
     */
    public static boolean hasRole(String role) {
        User user = getCurrentUser();
        if (user == null) {
            return false;
        }

        return user.getRole().toString().equals(role);
    }

    /**
     * Vérifier si l'utilisateur a un des rôles spécifiés
     * @param roles Tableau de rôles
     * @return true si l'utilisateur a au moins un de ces rôles
     */
    public static boolean hasAnyRole(String... roles) {
        User user = getCurrentUser();
        if (user == null) {
            return false;
        }

        String userRole = user.getRole().toString();
        for (String role : roles) {
            if (userRole.equals(role)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Vérifier si l'utilisateur est ADMIN
     * @return true si ADMIN
     */
    public static boolean isAdmin() {
        return hasRole("ADMIN");
    }

    /**
     * Vérifier si l'utilisateur est ORGANIZER
     * @return true si ORGANIZER
     */
    public static boolean isOrganizer() {
        return hasRole("ORGANIZER");
    }

    /**
     * Vérifier si l'utilisateur est CLIENT
     * @return true si CLIENT
     */
    public static boolean isClient() {
        return hasRole("CLIENT");
    }

    /**
     * Récupérer l'ID de l'utilisateur connecté
     * @return ID utilisateur ou null
     */
    public static Long getCurrentUserId() {
        User user = getCurrentUser();
        return user != null ? user.getId() : null;
    }

    /**
     * Récupérer le nom complet de l'utilisateur connecté
     * @return Nom complet (prénom + nom)
     */
    public static String getCurrentUserFullName() {
        User user = getCurrentUser();
        if (user != null) {
            return user.getPrenom() + " " + user.getNom();
        }
        return "Utilisateur";
    }
}