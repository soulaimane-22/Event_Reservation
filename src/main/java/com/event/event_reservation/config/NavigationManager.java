package com.event.event_reservation.config;

import com.vaadin.flow.component.UI;
import com.event.event_reservation.entity.enums.UserRole;

/**
 * Classe utilitaire pour gérer la navigation entre les pages
 */
public class NavigationManager {

    // ========== PAGES PUBLIQUES ==========

    /**
     * Naviguer vers la page d'accueil
     */
    public static void goToHome() {
        UI.getCurrent().navigate("");
    }

    /**
     * Naviguer vers la page de connexion
     */
    public static void goToLogin() {
        UI.getCurrent().navigate("login");
    }

    /**
     * Naviguer vers la page d'inscription
     */
    public static void goToRegister() {
        UI.getCurrent().navigate("register");
    }

    /**
     * Naviguer vers la liste des événements
     */
    public static void goToEventList() {
        UI.getCurrent().navigate("events");
    }

    /**
     * Naviguer vers les détails d'un événement
     * @param eventId ID de l'événement
     */
    public static void goToEventDetail(Long eventId) {
        UI.getCurrent().navigate("event/" + eventId);
    }

    // ========== PAGES CLIENT ==========

    /**
     * Naviguer vers le dashboard CLIENT
     */
    public static void goToDashboard() {
        UI.getCurrent().navigate("dashboard");
    }

    /**
     * Naviguer vers mes réservations
     */
    public static void goToMyReservations() {
        UI.getCurrent().navigate("my-reservations");
    }

    /**
     * Naviguer vers mon profil
     */
    public static void goToProfile() {
        UI.getCurrent().navigate("profile");
    }

    /**
     * Naviguer vers le formulaire de réservation
     * @param eventId ID de l'événement à réserver
     */
    public static void goToReservationForm(Long eventId) {
        UI.getCurrent().navigate("event/" + eventId + "/reserve");
    }

    // ========== PAGES ORGANIZER ==========

    /**
     * Naviguer vers le dashboard ORGANIZER
     */
    public static void goToOrganizerDashboard() {
        UI.getCurrent().navigate("organizer/dashboard");
    }

    /**
     * Naviguer vers mes événements
     */
    public static void goToMyEvents() {
        UI.getCurrent().navigate("organizer/events");
    }

    /**
     * Naviguer vers créer un événement
     */
    public static void goToCreateEvent() {
        UI.getCurrent().navigate("organizer/event/new");
    }

    /**
     * Naviguer vers modifier un événement
     * @param eventId ID de l'événement à modifier
     */
    public static void goToEditEvent(Long eventId) {
        UI.getCurrent().navigate("organizer/event/edit/" + eventId);
    }

    /**
     * Naviguer vers les réservations d'un événement
     * @param eventId ID de l'événement
     */
    public static void goToEventReservations(Long eventId) {
        UI.getCurrent().navigate("organizer/event/" + eventId + "/reservations");
    }

    // ========== PAGES ADMIN ==========

    /**
     * Naviguer vers le dashboard ADMIN
     */
    public static void goToAdminDashboard() {
        UI.getCurrent().navigate("admin/dashboard");
    }

    /**
     * Naviguer vers la gestion des utilisateurs
     */
    public static void goToUserManagement() {
        UI.getCurrent().navigate("admin/users");
    }

    /**
     * Naviguer vers la gestion de tous les événements
     */
    public static void goToAllEventsManagement() {
        UI.getCurrent().navigate("admin/events");
    }

    /**
     * Naviguer vers la gestion de toutes les réservations
     */
    public static void goToAllReservationsManagement() {
        UI.getCurrent().navigate("admin/reservations");
    }

    // ========== UTILITAIRES ==========

    /**
     * Rediriger automatiquement selon le rôle de l'utilisateur
     * @param role Rôle de l'utilisateur (CLIENT, ORGANIZER, ADMIN)
     */
    public static void redirectByRole(com.event.event_reservation.entity.enums.UserRole role) {
        if (role == null) {
            goToHome();
            return;
        }

        switch (role) {
            case CLIENT:
                goToDashboard();
                break;
            case ORGANIZER:
                goToOrganizerDashboard();
                break;
            case ADMIN:
                goToAdminDashboard();
                break;
            default:
                goToHome();
        }
    }

    /**
     * Naviguer en arrière (page précédente)
     */
    public static void goBack() {
        UI.getCurrent().getPage().executeJs("window.history.back()");
    }

    /**
     * Recharger la page courante
     */
    public static void refresh() {
        UI.getCurrent().getPage().reload();
    }

    /**
     * Naviguer vers une URL personnalisée
     * @param route Route/URL vers laquelle naviguer
     */
    public static void navigateTo(String route) {
        UI.getCurrent().navigate(route);
    }
}