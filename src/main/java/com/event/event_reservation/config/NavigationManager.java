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
}