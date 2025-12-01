package com.event.event_reservation.view.components;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.event.event_reservation.config.NavigationManager;
import com.event.event_reservation.config.VaadinSession;
import com.event.event_reservation.entity.User;
import com.event.event_reservation.entity.enums.UserRole;

/**
 * Layout principal de l'application avec menu de navigation
 * Ce layout sera réutilisé dans toutes les vues qui nécessitent un menu

public class VaadinAppLayout extends AppLayout {

    public VaadinAppLayout() {
        createHeader();
        createDrawer();
    }

    /**
     * Créer le header (barre supérieure)

    private void createHeader() {
        // Logo / Titre
        H1 logo = new H1("🎭 Event Reservation");
        logo.getStyle()
                .set("margin", "0")
                .set("font-size", "1.5em")
                .set("color", "var(--lumo-primary-text-color)");

        // Nom de l'utilisateur connecté
        Span userInfo = new Span();
        User currentUser = VaadinSession.getCurrentUser();
        if (currentUser != null) {
            userInfo.setText("👤 " + currentUser.getPrenom() + " " + currentUser.getNom());
            userInfo.getStyle()
                    .set("margin-left", "auto")
                    .set("margin-right", "1em");
        }

        // Bouton Déconnexion
        Button logoutButton = new Button("Déconnexion", VaadinIcon.SIGN_OUT.create());
        logoutButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        logoutButton.addClickListener(e -> handleLogout());

        // Layout du header
        HorizontalLayout header = new HorizontalLayout(
                new DrawerToggle(),
                logo,
                userInfo,
                logoutButton
        );
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setWidthFull();
        header.setPadding(true);
        header.setSpacing(true);

        addToNavbar(header);
    }

    /**
     * Créer le drawer (menu latéral)

    private void createDrawer() {
        VerticalLayout drawer = new VerticalLayout();
        drawer.setPadding(false);
        drawer.setSpacing(false);

        // Ajouter les menus selon le rôle
        User currentUser = VaadinSession.getCurrentUser();

        if (currentUser != null) {
            UserRole role = currentUser.getRole();

            // Menu CLIENT (pour CLIENT, ORGANIZER, ADMIN)
            if (role == UserRole.CLIENT || role == UserRole.ORGANIZER || role == UserRole.ADMIN) {
                drawer.add(createSectionTitle("👤 Client"));
                drawer.add(createClientMenu());
                drawer.add(new Hr());
            }

            // Menu ORGANIZER (pour ORGANIZER, ADMIN)
            if (role == UserRole.ORGANIZER || role == UserRole.ADMIN) {
                drawer.add(createSectionTitle("🎭 Organisateur"));
                drawer.add(createOrganizerMenu());
                drawer.add(new Hr());
            }

            // Menu ADMIN (pour ADMIN uniquement)
            if (role == UserRole.ADMIN) {
                drawer.add(createSectionTitle("🔐 Admin"));
                drawer.add(createAdminMenu());
                drawer.add(new Hr());
            }
        }

        // Menu PUBLIC (toujours visible)
        drawer.add(createSectionTitle("🌐 Public"));
        drawer.add(createPublicMenu());

        addToDrawer(drawer);
    }

    /**
     * Créer un titre de section dans le menu
     */
    private Span createSectionTitle(String title) {
        Span span = new Span(title);
        span.getStyle()
                .set("font-weight", "bold")
                .set("font-size", "0.9em")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("padding", "var(--lumo-space-s) var(--lumo-space-m)")
                .set("display", "block");
        return span;
    }

    /**
     * Créer le menu PUBLIC
     */
    private VerticalLayout createPublicMenu() {
        VerticalLayout menu = new VerticalLayout();
        menu.setPadding(false);
        menu.setSpacing(false);

        Button homeBtn = createMenuButton("Accueil", VaadinIcon.HOME,
                e -> NavigationManager.goToHome());

        Button eventsBtn = createMenuButton("Événements", VaadinIcon.CALENDAR,
                e -> NavigationManager.goToEventList());

        menu.add(homeBtn, eventsBtn);
        return menu;
    }

    /**
     * Créer le menu CLIENT
     */
    private VerticalLayout createClientMenu() {
        VerticalLayout menu = new VerticalLayout();
        menu.setPadding(false);
        menu.setSpacing(false);

        Button dashboardBtn = createMenuButton("Mon Dashboard", VaadinIcon.DASHBOARD,
                e -> NavigationManager.goToDashboard());

        Button reservationsBtn = createMenuButton("Mes Réservations", VaadinIcon.TICKET,
                e -> NavigationManager.goToMyReservations());

        Button profileBtn = createMenuButton("Mon Profil", VaadinIcon.USER,
                e -> NavigationManager.goToProfile());

        menu.add(dashboardBtn, reservationsBtn, profileBtn);
        return menu;
    }

    /**
     * Créer le menu ORGANIZER

    private VerticalLayout createOrganizerMenu() {
        VerticalLayout menu = new VerticalLayout();
        menu.setPadding(false);
        menu.setSpacing(false);

        Button orgDashboardBtn = createMenuButton("Dashboard Organisateur", VaadinIcon.CHART,
                e -> NavigationManager.goToOrganizerDashboard());

        Button myEventsBtn = createMenuButton("Mes Événements", VaadinIcon.LIST,
                e -> NavigationManager.goToMyEvents());

        Button createEventBtn = createMenuButton("Créer Événement", VaadinIcon.PLUS_CIRCLE,
                e -> NavigationManager.goToCreateEvent());

        menu.add(orgDashboardBtn, myEventsBtn, createEventBtn);
        return menu;
    }

    /**
     * Créer le menu ADMIN

    private VerticalLayout createAdminMenu() {
        VerticalLayout menu = new VerticalLayout();
        menu.setPadding(false);
        menu.setSpacing(false);

        Button adminDashboardBtn = createMenuButton("Dashboard Admin", VaadinIcon.COG,
                e -> NavigationManager.goToAdminDashboard());

        Button userMgmtBtn = createMenuButton("Gestion Utilisateurs", VaadinIcon.USERS,
                e -> NavigationManager.goToUserManagement());

        Button eventMgmtBtn = createMenuButton("Gestion Événements", VaadinIcon.CALENDAR_CLOCK,
                e -> NavigationManager.goToAllEventsManagement());

        Button reservationMgmtBtn = createMenuButton("Gestion Réservations", VaadinIcon.CLIPBOARD_TEXT,
                e -> NavigationManager.goToAllReservationsManagement());

        menu.add(adminDashboardBtn, userMgmtBtn, eventMgmtBtn, reservationMgmtBtn);
        return menu;
    }

    /**
     * Créer un bouton de menu stylisé
     */
    private Button createMenuButton(String text, VaadinIcon icon,
                                    com.vaadin.flow.component.ComponentEventListener<
                                            com.vaadin.flow.component.ClickEvent<Button>> listener) {
        Button button = new Button(text, icon.create());
        button.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        button.setWidthFull();
        button.getStyle()
                .set("justify-content", "flex-start")
                .set("padding", "var(--lumo-space-s) var(--lumo-space-m)");
        button.addClickListener(listener);
        return button;
    }

    /**
     * Gérer la déconnexion
     */
    private void handleLogout() {
        VaadinSession.logout();
        NavigationManager.goToLogin();
    }
}
