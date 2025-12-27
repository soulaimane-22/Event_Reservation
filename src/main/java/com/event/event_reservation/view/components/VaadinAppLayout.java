package com.event.event_reservation.view.components;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.event.event_reservation.config.NavigationManager;
import com.event.event_reservation.config.VaadinSession;
import com.event.event_reservation.entity.User;
import com.event.event_reservation.entity.enums.UserRole;

public class VaadinAppLayout extends AppLayout {

    private boolean isDarkMode = false;
    private final String BRAND_COLOR = "#253366";

    public VaadinAppLayout() {
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        // --- 1. LOGO (XXL) ---
        Image logo = new Image("images/events/logos/OCCASIO_EVENT.svg", "Occasio Logo");
        logo.setHeight("125px");
        logo.getStyle().set("cursor", "pointer");
        logo.getStyle().set("margin-top", "-20px");
        logo.getStyle().set("margin-bottom", "-20px");
        logo.addClickListener(e -> NavigationManager.goToHome());

        // --- 2. NAVIGATION PUBLIQUE (CENTRE) ---
        HorizontalLayout publicNav = new HorizontalLayout();
        Button eventsBtn = new Button("Événements");
        eventsBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        eventsBtn.getStyle().set("color", BRAND_COLOR);
        eventsBtn.getStyle().set("font-weight", "700");
        eventsBtn.getStyle().set("font-size", "1.2em");
        eventsBtn.addClickListener(e -> NavigationManager.goToEventList());
        publicNav.add(eventsBtn);

        // --- 3. ACTIONS (DROITE) ---
        HorizontalLayout rightSide = new HorizontalLayout();
        rightSide.setAlignItems(FlexComponent.Alignment.CENTER);
        rightSide.setSpacing(true);

        HorizontalLayout authLayout = new HorizontalLayout();
        authLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        if (VaadinSession.isUserLoggedIn()) {
            User currentUser = VaadinSession.getCurrentUser();
            Span userName = new Span(currentUser.getPrenom());
            userName.getStyle().set("color", BRAND_COLOR).set("font-weight", "bold");

            Button logoutBtn = new Button("Déconnexion", VaadinIcon.SIGN_OUT.create());
            logoutBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            logoutBtn.getStyle().set("color", BRAND_COLOR);
            logoutBtn.addClickListener(e -> handleLogout());
            authLayout.add(userName, logoutBtn);
        } else {
            Image loginImg = new Image("images/events/icons/connexion.svg", "");
            loginImg.setHeight("24px");
            Button loginBtn = new Button("Connexion", loginImg);
            loginBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            loginBtn.getStyle().set("color", BRAND_COLOR).set("font-weight", "600");
            loginBtn.addClickListener(e -> NavigationManager.goToLogin());

            Image regImg = new Image("images/events/icons/s'inscrire.svg", "");
            regImg.setHeight("24px");
            Button registerBtn = new Button("S'inscrire", regImg);
            registerBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            registerBtn.addClickListener(e -> NavigationManager.goToRegister());

            authLayout.add(loginBtn, registerBtn);
        }

        Image themeIcon = new Image("images/events/icons/dark_mode.svg", "Theme");
        themeIcon.setHeight("28px");
        Button themeToggle = new Button(themeIcon);
        themeToggle.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        themeToggle.addClickListener(e -> toggleTheme(themeToggle));

        rightSide.add(authLayout, themeToggle);

        HorizontalLayout header = new HorizontalLayout();

        // Le Toggle n'apparaît que si l'utilisateur peut avoir un menu
        if (VaadinSession.isUserLoggedIn()) {
            DrawerToggle toggle = new DrawerToggle();
            toggle.getStyle().set("color", BRAND_COLOR);
            header.add(toggle);
        }

        header.add(logo, publicNav, rightSide);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setWidthFull();
        header.getStyle().set("padding", "0 30px");
        header.getStyle().set("background-color", "white");
        header.getStyle().set("border-bottom", "1px solid #e0e0e0");

        header.setFlexGrow(1, publicNav);
        publicNav.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        addToNavbar(header);
    }

    private void toggleTheme(Button btn) {
        isDarkMode = !isDarkMode;
        UI.getCurrent().getElement().setAttribute("theme", isDarkMode ? "dark" : "");
        String iconPath = isDarkMode ? "images/events/icons/light_mode.svg" : "images/events/icons/dark_mode.svg";
        Image newIcon = new Image(iconPath, "Theme");
        newIcon.setHeight("28px");
        btn.setIcon(newIcon);
    }

    /**
     * MODIFICATION ICI : On empêche l'espace vide à gauche
     */
    private void createDrawer() {
        User currentUser = VaadinSession.getCurrentUser();

        // FIX : Si pas d'utilisateur, on ferme le drawer et on sort.
        // Cela supprime la barre blanche à gauche pour les visiteurs.
        if (currentUser == null) {
            setDrawerOpened(false);
            return;
        }

        VerticalLayout drawer = new VerticalLayout();
        drawer.setPadding(false);
        drawer.setSpacing(false);

        UserRole role = currentUser.getRole();
        if (role == UserRole.CLIENT || role == UserRole.ORGANIZER || role == UserRole.ADMIN) {
            drawer.add(createSectionTitle("👤 Mon Espace"));
            drawer.add(createClientMenu());
            drawer.add(new Hr());
        }
        if (role == UserRole.ORGANIZER || role == UserRole.ADMIN) {
            drawer.add(createSectionTitle("🎭 Organisateur"));
            drawer.add(createOrganizerMenu());
            drawer.add(new Hr());
        }
        if (role == UserRole.ADMIN) {
            drawer.add(createSectionTitle("🔐 Administration"));
            drawer.add(createAdminMenu());
            drawer.add(new Hr());
        }

        addToDrawer(drawer);

        // On s'assure que le drawer est fermé au démarrage pour ne pas pousser le contenu
        setDrawerOpened(false);
    }

    private Span createSectionTitle(String title) {
        Span span = new Span(title);
        span.getStyle()
                .set("font-weight", "bold")
                .set("font-size", "0.85em")
                .set("color", BRAND_COLOR)
                .set("padding", "20px 20px 5px 20px")
                .set("text-transform", "uppercase");
        return span;
    }

    private VerticalLayout createClientMenu() {
        VerticalLayout menu = new VerticalLayout();
        menu.setPadding(false); menu.setSpacing(false);
        menu.add(
                createMenuButton("Mon Dashboard", VaadinIcon.DASHBOARD, e -> NavigationManager.goToDashboard()),
                createMenuButton("Mes Réservations", VaadinIcon.TICKET, e -> NavigationManager.goToMyReservations()),
                createMenuButton("Mon Profil", VaadinIcon.USER, e -> NavigationManager.goToProfile())
        );
        return menu;
    }

    private VerticalLayout createOrganizerMenu() {
        VerticalLayout menu = new VerticalLayout();
        menu.setPadding(false); menu.setSpacing(false);
        menu.add(
                createMenuButton("Console Organisateur", VaadinIcon.CHART, e -> NavigationManager.goToOrganizerDashboard()),
                createMenuButton("Mes Événements", VaadinIcon.LIST, e -> NavigationManager.goToMyEvents()),
                createMenuButton("Créer Événement", VaadinIcon.PLUS_CIRCLE, e -> NavigationManager.goToCreateEvent())
        );
        return menu;
    }

    private VerticalLayout createAdminMenu() {
        VerticalLayout menu = new VerticalLayout();
        menu.setPadding(false); menu.setSpacing(false);
        menu.add(
                createMenuButton("Gestion Utilisateurs", VaadinIcon.USERS, e -> NavigationManager.goToUserManagement()),
                createMenuButton("Gestion Événements", VaadinIcon.CALENDAR_CLOCK, e -> NavigationManager.goToAllEventsManagement()),
                createMenuButton("Gestion Réservations", VaadinIcon.CLIPBOARD_TEXT, e -> NavigationManager.goToAllReservationsManagement())
        );
        return menu;
    }

    private Button createMenuButton(String text, VaadinIcon icon, com.vaadin.flow.component.ComponentEventListener<com.vaadin.flow.component.ClickEvent<Button>> listener) {
        Button button = new Button(text, icon.create());
        button.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        button.setWidthFull();
        button.getStyle().set("justify-content", "flex-start").set("padding-left", "25px").set("color", BRAND_COLOR);
        button.addClickListener(listener);
        return button;
    }

    private void handleLogout() {
        VaadinSession.logout();
        NavigationManager.goToLogin();
    }
}