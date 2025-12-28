package com.event.event_reservation.view.components;

import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.TextField;
import com.event.event_reservation.config.NavigationManager;
import com.event.event_reservation.config.VaadinSession;
import com.event.event_reservation.entity.User;
import com.event.event_reservation.entity.enums.UserRole;

public class VaadinAppLayout extends AppLayout {

    private boolean isDarkMode = false;
    private final String BRAND_COLOR = "#253366";
    private final String BRAND_VARIANT = "#435591"; // Couleur variante pour le profil
    private final String ICON_PATH = "images/events/icons/";
    private final VerticalLayout footerContainer = new VerticalLayout();

    public VaadinAppLayout() {
        createHeader();
        createDrawer();
        buildFooter();
    }

    @Override
    public void showRouterLayoutContent(HasElement content) {
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setPadding(false);
        mainLayout.setSpacing(false);
        mainLayout.setMargin(false);
        mainLayout.setWidthFull();
        mainLayout.getStyle().set("min-height", "100vh");

        Div pageView = new Div();
        pageView.setWidthFull();
        pageView.getStyle().set("flex-grow", "1");
        pageView.getElement().appendChild(content.getElement());

        // STRUCTURE : 1. Page | 2. Features Section (Bold 800) | 3. Footer
        mainLayout.add(pageView, buildFeaturesSection(), footerContainer);
        setContent(mainLayout);
    }

    /**
     * SECTION FONCTIONNALITÉS (Juste avant le footer)
     */
    private VerticalLayout buildFeaturesSection() {
        VerticalLayout section = new VerticalLayout();
        section.setWidthFull();
        section.setAlignItems(FlexComponent.Alignment.CENTER);
        section.getStyle().set("padding", "80px 10%").set("background-color", "#fcfdfe");

        // TITRE DE SECTION (BOLD 800)
        H2 sectionTitle = new H2("Fonctionnalités Clés de votre plateforme");
        sectionTitle.getStyle()
                .set("color", BRAND_COLOR)
                .set("font-weight", "800")
                .set("font-size", "2.2em")
                .set("margin-bottom", "50px");

        HorizontalLayout cardsContainer = new HorizontalLayout();
        cardsContainer.setWidthFull();
        cardsContainer.setSpacing(true);
        cardsContainer.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        cardsContainer.add(createFeatureCard(
                "monitor.svg",
                "Confort Multi-écran",
                "Profitez d’une ergonomie parfaite sur mobile, tablette et ordinateur. Notre plateforme s'adapte à vos outils pour vous offrir une navigation sans aucune friction."
        ));

        cardsContainer.add(createFeatureCard(
                "ticket.svg",
                "Passions sur Mesure",
                "Accédez à un large éventail d'activités triées par centres d'intérêt. Découvrez chaque jour de nouvelles opportunités qui correspondent précisément à vos goûts."
        ));

        section.add(sectionTitle, cardsContainer);
        return section;
    }

    private VerticalLayout createFeatureCard(String iconName, String title, String description) {
        VerticalLayout card = new VerticalLayout();
        card.setWidth("450px");
        card.setPadding(true);
        card.setAlignItems(FlexComponent.Alignment.CENTER);

        var s = card.getStyle();
        s.set("background-color", "white");
        s.set("border-radius", "25px");
        s.set("box-shadow", "0 15px 40px rgba(0, 0, 0, 0.06)");
        s.set("padding", "50px 35px");
        s.set("transition", "transform 0.3s ease");

        Image icon = new Image(ICON_PATH + iconName, "");
        icon.setHeight("70px");
        icon.getStyle().set("margin-bottom", "25px");

        // TITRE DE LA CARTE (BOLD 800)
        H3 h3 = new H3(title);
        h3.getStyle()
                .set("color", BRAND_COLOR)
                .set("font-weight", "800")
                .set("margin", "0 0 20px 0")
                .set("font-size", "1.5em");

        Paragraph p = new Paragraph(description);
        p.getStyle().set("text-align", "center").set("color", "#555").set("line-height", "1.7").set("font-size", "1.05em");

        card.add(icon, h3, p);

        card.getElement().executeJs("this.onmouseover = () => { this.style.transform = 'translateY(-12px)'; };" +
                "this.onmouseout = () => { this.style.transform = 'translateY(0)'; };");

        return card;
    }

    private void createHeader() {
        Image logo = new Image("images/events/logos/OCCASIO_EVENT.svg", "Logo");
        logo.setHeight("125px");
        logo.getStyle().set("cursor", "pointer").set("margin-top", "-20px").set("margin-bottom", "-20px");
        logo.addClickListener(e -> NavigationManager.goToHome());

        // --- 2. NAVIGATION CENTRALE (DÉPLACEMENT DES LIENS CLIENT ICI) ---
        HorizontalLayout navMenu = new HorizontalLayout();
        navMenu.setSpacing(true);
        navMenu.setAlignItems(FlexComponent.Alignment.CENTER);

        // Bouton Événements (Toujours là)
        navMenu.add(createNavButton("Événements", e -> NavigationManager.goToEventList()));

        // Si connecté, on ajoute le Dashboard, les Réservations et le Profil
        if (VaadinSession.isUserLoggedIn()) {
            navMenu.add(
                    createNavButton("Tableau de bord", e -> NavigationManager.goToDashboard()),
                    createNavButton("Mes Réservations", e -> NavigationManager.goToMyReservations()),
                    createNavButton("Mon Profil", e -> NavigationManager.goToProfile())
            );
        }

        // --- 3. ACTIONS DROITE ---
        HorizontalLayout rightSide = new HorizontalLayout();
        rightSide.setAlignItems(FlexComponent.Alignment.CENTER);
        rightSide.setSpacing(true);

        HorizontalLayout authLayout = new HorizontalLayout();
        authLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        if (VaadinSession.isUserLoggedIn()) {
            User currentUser = VaadinSession.getCurrentUser();
            // AFFICHAGE NOM/PRENOM EN COULEUR VARIANTE
            Span userName = new Span(currentUser.getPrenom() + " " + currentUser.getNom());
            userName.getStyle()
                    .set("color", BRAND_VARIANT)
                    .set("font-weight", "bold")
                    .set("margin-right", "15px");

            Button logoutBtn = new Button("Déconnexion", VaadinIcon.SIGN_OUT.create());
            logoutBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            logoutBtn.getStyle().set("color", BRAND_COLOR);
            logoutBtn.addClickListener(e -> handleLogout());
            authLayout.add(userName, logoutBtn);
        } else {
            Button loginBtn = new Button("Connexion", new Image(ICON_PATH + "connexion.svg", ""));
            loginBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            loginBtn.getStyle().set("color", BRAND_COLOR).set("font-weight", "600");
            loginBtn.addClickListener(e -> NavigationManager.goToLogin());

            Button registerBtn = new Button("S'inscrire", new Image(ICON_PATH + "s'inscrire.svg", ""));
            registerBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            registerBtn.addClickListener(e -> NavigationManager.goToRegister());
            authLayout.add(loginBtn, registerBtn);
        }

        // RECHERCHE INTERACTIVE
        TextField searchInput = new TextField();
        searchInput.setPlaceholder("Rechercher...");
        searchInput.setVisible(false);
        searchInput.setWidth("0px");
        searchInput.getStyle().set("transition", "all 0.3s ease");

        Button searchBtn = new Button(new Image(ICON_PATH + "recherche.svg", ""));
        searchBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        searchBtn.addClickListener(e -> {
            if (!searchInput.isVisible()) {
                searchInput.setVisible(true); searchInput.setWidth("200px"); searchInput.focus();
            } else {
                if (!searchInput.getValue().isEmpty()) UI.getCurrent().navigate("events");
                searchInput.setVisible(false); searchInput.setWidth("0px");
            }
        });

        // DARK MODE
        Button themeToggle = new Button(new Image(ICON_PATH + "dark_mode.svg", ""));
        themeToggle.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        themeToggle.addClickListener(e -> toggleTheme(themeToggle));

        rightSide.add(authLayout, searchInput, searchBtn, themeToggle);

        HorizontalLayout header = new HorizontalLayout();
        User user = VaadinSession.getCurrentUser();
        if (user != null && (user.getRole() == UserRole.ORGANIZER || user.getRole() == UserRole.ADMIN)) {
            header.add(new DrawerToggle());
        }

        header.add(logo, navMenu, rightSide);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setWidthFull();
        header.getStyle().set("padding", "0 30px").set("background-color", "white").set("border-bottom", "1px solid #eee");
        header.setFlexGrow(1, navMenu);
        navMenu.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        addToNavbar(header);
    }

    private Button createNavButton(String text, com.vaadin.flow.component.ComponentEventListener<com.vaadin.flow.component.ClickEvent<Button>> listener) {
        Button btn = new Button(text);
        btn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btn.getStyle()
                .set("color", BRAND_COLOR)
                .set("font-weight", "700")
                .set("font-size", "1.1em");
        btn.addClickListener(listener);
        return btn;
    }

    private void buildFooter() {
        footerContainer.setWidthFull();
        footerContainer.setPadding(false);
        footerContainer.setSpacing(false);
        footerContainer.getStyle()
                .set("background-color", "#f1f5f9")
                .set("border-top", "1px solid #e2e8f0");

        HorizontalLayout footerBody = new HorizontalLayout();
        footerBody.setWidthFull();
        footerBody.getStyle().set("padding", "60px 10%");
        footerBody.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        footerBody.setAlignItems(FlexComponent.Alignment.START);

        VerticalLayout brandCol = new VerticalLayout();
        brandCol.setPadding(false);
        brandCol.setWidth("350px");
        Image footerLogo = new Image("images/events/logos/OCCASIO_EVENT.svg", "");
        footerLogo.setHeight("100px");
        brandCol.add(footerLogo);

        VerticalLayout linksCol = new VerticalLayout();
        linksCol.setPadding(false);
        linksCol.setSpacing(false);
        linksCol.setWidth("200px");
        Span linksTitle = new Span("Navigation");
        linksTitle.getStyle().set("font-weight", "800").set("color", BRAND_COLOR).set("margin-bottom", "15px").set("text-transform", "uppercase").set("font-size", "0.9em");
        linksCol.add(linksTitle, createFooterLink("Accueil", ""), createFooterLink("Événements", "events"), createFooterLink("Connexion", "login"), createFooterLink("S'inscrire", "register"));

        VerticalLayout socialCol = new VerticalLayout();
        socialCol.setPadding(false);
        socialCol.setAlignItems(FlexComponent.Alignment.END);
        socialCol.setWidth("300px");
        Span socialTitle = new Span("Suivez l'actualité");
        socialTitle.getStyle().set("font-weight", "800").set("color", BRAND_COLOR).set("margin-bottom", "20px").set("text-transform", "uppercase").set("font-size", "0.9em");

        HorizontalLayout iconsRow = new HorizontalLayout();
        iconsRow.setSpacing(true);
        iconsRow.add(createSocialIcon("fcebook.svg"), createSocialIcon("twitter.svg"), createSocialIcon("instagram.svg"), createSocialIcon("youtube.svg"), createSocialIcon("linkedin.svg"), createSocialIcon("pinterest.svg"));
        socialCol.add(socialTitle, iconsRow);

        footerBody.add(brandCol, linksCol, socialCol);

        Div copyrightStrip = new Div();
        copyrightStrip.setWidthFull();
        copyrightStrip.getStyle().set("background-color", "#e2e8f0").set("padding", "20px 0").set("text-align", "center");
        Span copyrightText = new Span("© 2025, EVENTS RESERVATION . All right reserved.");
        copyrightText.getStyle().set("font-size", "0.85em").set("color", "#475569").set("font-weight", "500");
        copyrightStrip.add(copyrightText);

        footerContainer.add(footerBody, copyrightStrip);
    }

    private Anchor createFooterLink(String text, String route) {
        Anchor a = new Anchor(route, text);
        a.getStyle().set("color", "#475569").set("text-decoration", "none").set("font-size", "0.95em").set("margin-bottom", "10px").set("font-weight", "500");
        return a;
    }

    private Image createSocialIcon(String fileName) {
        Image img = new Image(ICON_PATH + fileName, "");
        img.setHeight("28px"); img.getStyle().set("cursor", "pointer").set("transition", "0.2s");
        return img;
    }

    private void toggleTheme(Button btn) {
        isDarkMode = !isDarkMode;
        UI.getCurrent().getElement().setAttribute("theme", isDarkMode ? "dark" : "");
        String iconPath = isDarkMode ? ICON_PATH + "light_mode.svg" : ICON_PATH + "dark_mode.svg";
        Image newIcon = new Image(iconPath, "");
        newIcon.setHeight("28px");
        if (isDarkMode) {
            newIcon.getStyle().set("filter", "invert(1)");
            footerContainer.getStyle().set("background-color", "#1a1a1a");
        } else {
            footerContainer.getStyle().set("background-color", "#f1f5f9");
        }
        btn.setIcon(newIcon);
    }

    private void createDrawer() {
        User currentUser = VaadinSession.getCurrentUser();
        if (currentUser == null) { setDrawerOpened(false); return; }

        VerticalLayout drawer = new VerticalLayout();
        drawer.setPadding(false); drawer.setSpacing(false);
        UserRole role = currentUser.getRole();

        // PLUS DE SECTION CLIENT ICI -> ELLE EST DANS LE HEADER
        if (role == UserRole.ORGANIZER || role == UserRole.ADMIN) {
            drawer.add(createSectionTitle("Espace Organisateur"), createOrganizerMenu());
        }
        if (role == UserRole.ADMIN) {
            drawer.add(new Hr(), createSectionTitle("Outils Administration"), createAdminMenu());
        }

        if (role == UserRole.CLIENT) {
            setDrawerOpened(false);
        } else {
            addToDrawer(drawer);
            setDrawerOpened(false);
        }
    }

    private Span createSectionTitle(String title) {
        Span span = new Span(title);
        span.getStyle().set("font-weight", "bold").set("font-size", "0.8em").set("color", BRAND_COLOR).set("padding", "20px 25px 5px 25px").set("text-transform", "uppercase");
        return span;
    }

    private VerticalLayout createOrganizerMenu() {
        VerticalLayout m = new VerticalLayout(); m.setPadding(false); m.setSpacing(false);
        m.add(createMenuButton("Console", VaadinIcon.CHART, e -> NavigationManager.goToOrganizerDashboard()),
                createMenuButton("Mes Événements", VaadinIcon.LIST, e -> NavigationManager.goToMyEvents()));
        return m;
    }

    private VerticalLayout createAdminMenu() {
        VerticalLayout m = new VerticalLayout(); m.setPadding(false); m.setSpacing(false);
        m.add(createMenuButton("Utilisateurs", VaadinIcon.USERS, e -> NavigationManager.goToUserManagement()),
                createMenuButton("Événements", VaadinIcon.CALENDAR_CLOCK, e -> NavigationManager.goToAllEventsManagement()));
        return m;
    }

    private Button createMenuButton(String text, VaadinIcon icon, com.vaadin.flow.component.ComponentEventListener<com.vaadin.flow.component.ClickEvent<Button>> listener) {
        Button b = new Button(text, icon.create());
        b.addThemeVariants(ButtonVariant.LUMO_TERTIARY); b.setWidthFull();
        b.getStyle().set("justify-content", "flex-start").set("padding-left", "25px").set("color", BRAND_COLOR);
        b.addClickListener(listener);
        return b;
    }

    private void handleLogout() {
        VaadinSession.logout();
        NavigationManager.goToLogin(); // REDIRECTION VERS ACCUEIL
    }
}