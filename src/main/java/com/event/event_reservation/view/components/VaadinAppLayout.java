package com.event.event_reservation.view.components;

import com.event.event_reservation.chat.service.ChatAiService;
import com.event.event_reservation.chat.view.ChatWidget;
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
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.event.event_reservation.config.NavigationManager;
import com.event.event_reservation.config.VaadinSession;
import com.event.event_reservation.entity.User;
import com.event.event_reservation.entity.enums.UserRole;
import org.springframework.beans.factory.annotation.Autowired;

public class VaadinAppLayout extends AppLayout implements BeforeEnterObserver {

    private boolean isDarkMode = false;
    private final String BRAND_COLOR = "#253366";
    private final String BRAND_VARIANT = "#435591";
    private final String ICON_PATH = "images/events/icons/";
    private final VerticalLayout footerContainer = new VerticalLayout();

    // --- AJOUT CHATBOT ---
    private final ChatAiService chatAiService;
    private final ChatWidget chatWidget;
    private boolean isChatVisible = false;

    @Autowired
    public VaadinAppLayout(ChatAiService chatAiService) {
        this.chatAiService = chatAiService;
        this.chatWidget = new ChatWidget(chatAiService);

        createHeader();
        createDrawer();
        buildFooter();
    }

    /**
     * REDIRECTION AUTOMATIQUE : Gère l'Admin et l'Organisateur
     */
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        User user = VaadinSession.getCurrentUser();
        if (user != null) {
            String path = event.getLocation().getPath();
            boolean isRoot = path.isEmpty() || path.equals("/");

            if (isRoot) {
                if (user.getRole() == UserRole.ADMIN) {
                    event.forwardTo("admin/dashboard");
                } else if (user.getRole() == UserRole.ORGANIZER) {
                    event.forwardTo("organizer/dashboard");
                }
            }
        }
    }

    /**
     * AFFICHAGE DU CONTENU : Masque le marketing pour Admin et Organisateur
     * Ajout de l'overlay du Chatbot
     */
    @Override
    public void showRouterLayoutContent(HasElement content) {
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setPadding(false);
        mainLayout.setSpacing(false);
        mainLayout.setMargin(false);
        mainLayout.setWidthFull();
        mainLayout.getStyle().set("min-height", "100vh").set("position", "relative");

        Div pageView = new Div();
        pageView.setWidthFull();
        pageView.getStyle().set("flex-grow", "1");
        pageView.getElement().appendChild(content.getElement());

        mainLayout.add(pageView);

        User user = VaadinSession.getCurrentUser();
        boolean isStaff = (user != null && (user.getRole() == UserRole.ORGANIZER || user.getRole() == UserRole.ADMIN));

        // Si ce n'est pas un membre du staff (Admin/Org), on affiche le contenu marketing
        if (!isStaff) {
            mainLayout.add(buildFeaturesSection(), footerContainer);
        } else {
            mainLayout.getStyle().set("background-color", "#fcfdfe");
        }

        // --- INJECTION DU CHATBOT DANS LE LAYOUT ---
        mainLayout.add(this.chatWidget, createChatFloatingButton());

        setContent(mainLayout);
    }

    /**
     * BOUTON FLOTTANT DU CHATBOT
     */
    private Button createChatFloatingButton() {
        Button chatFab = new Button(VaadinIcon.CHAT.create());
        chatFab.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);

        var s = chatFab.getStyle();
        s.set("position", "fixed");
        s.set("bottom", "30px");
        s.set("right", "30px");
        s.set("width", "65px");
        s.set("height", "65px");
        s.set("border-radius", "50%");
        s.set("background-color", BRAND_COLOR);
        s.set("box-shadow", "0 10px 25px rgba(0,0,0,0.3)");
        s.set("z-index", "10000");
        s.set("cursor", "pointer");

        // Positionnement de la fenêtre de chat
        this.chatWidget.getStyle().set("position", "fixed");
        this.chatWidget.getStyle().set("bottom", "110px");
        this.chatWidget.getStyle().set("right", "30px");
        this.chatWidget.getStyle().set("z-index", "9999");
        this.chatWidget.setVisible(isChatVisible);

        chatFab.addClickListener(e -> {
            isChatVisible = !isChatVisible;
            chatWidget.setVisible(isChatVisible);
            chatFab.setIcon(isChatVisible ? VaadinIcon.CLOSE.create() : VaadinIcon.CHAT.create());
        });

        return chatFab;
    }

    /**
     * SECTION FONCTIONNALITÉS (BOLD 800)
     */
    private VerticalLayout buildFeaturesSection() {
        VerticalLayout section = new VerticalLayout();
        section.setWidthFull();
        section.setAlignItems(FlexComponent.Alignment.CENTER);
        section.getStyle().set("padding", "80px 10%").set("background-color", "#fcfdfe");

        H2 sectionTitle = new H2("Fonctionnalités Clés de notre plateforme");
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
        card.getStyle()
                .set("background-color", "#EFF1FC")
                .set("border-radius", "25px")
                .set("box-shadow", "0px 7px 29px 0px rgba(37, 51, 102, 0.4)")
                .set("transition", "transform 0.3s ease")
                .set("cursor", "pointer")
                .set("padding", "50px 35px");

        Image icon = new Image(ICON_PATH + iconName, "");
        icon.setHeight("70px");
        icon.getStyle().set("margin-bottom", "25px");

        H3 h3 = new H3(title);
        h3.getStyle().set("color", BRAND_COLOR).set("font-weight", "800").set("font-size", "1.5em");

        Paragraph p = new Paragraph(description);
        p.getStyle().set("text-align", "center").set("color", "#555").set("line-height", "1.7");

        card.add(icon, h3, p);
        return card;
    }

    private void createHeader() {
        Image logo = new Image("images/events/logos/OCCASIO_EVENT.svg", "Logo");
        logo.setHeight("125px");
        logo.getStyle().set("cursor", "pointer").set("margin-top", "-20px").set("margin-bottom", "-20px");
        logo.addClickListener(e -> NavigationManager.goToHome());

        HorizontalLayout navMenu = new HorizontalLayout();
        navMenu.setSpacing(true);
        navMenu.setAlignItems(FlexComponent.Alignment.CENTER);

        User user = VaadinSession.getCurrentUser();

        // --- NAVIGATION HEADER DYNAMIQUE ---
        if (user != null && user.getRole() == UserRole.ADMIN) {
            navMenu.add(
                    createNavButton("Tableau de bord", e -> NavigationManager.goToAdminDashboard()),
                    createNavButton("Utilisateurs", e -> NavigationManager.goToUserManagement()),
                    createNavButton("Réservations", e -> NavigationManager.goToAllReservationsManagement()),
                    createNavButton("Événements", e -> NavigationManager.goToAllEventsManagement()),
                    createNavButton("Profil", e -> NavigationManager.goToProfile())
            );
        } else if (user != null && user.getRole() == UserRole.ORGANIZER) {
            navMenu.add(
                    createNavButton("Tableau de bord", e -> NavigationManager.goToOrganizerDashboard()),
                    createNavButton("Événements", e -> NavigationManager.goToMyEvents()),
                    createNavButton("Gestion des événements", e -> NavigationManager.goToMyEvents()),
                    createNavButton("Profil", e -> NavigationManager.goToProfile())
            );
        } else if (user != null && user.getRole() == UserRole.CLIENT) {
            navMenu.add(
                    createNavButton("Événements", e -> NavigationManager.goToEventList()),
                    createNavButton("Tableau de bord", e -> NavigationManager.goToDashboard()),
                    createNavButton("Mes Réservations", e -> NavigationManager.goToMyReservations()),
                    createNavButton("Mon Profil", e -> NavigationManager.goToProfile())
            );
        } else {
            navMenu.add(createNavButton("Événements", e -> NavigationManager.goToEventList()));
        }

        HorizontalLayout rightSide = new HorizontalLayout();
        rightSide.setAlignItems(FlexComponent.Alignment.CENTER);
        rightSide.setSpacing(true);

        HorizontalLayout authLayout = new HorizontalLayout();
        if (VaadinSession.isUserLoggedIn()) {
            Span userName = new Span(user.getPrenom() + " " + user.getNom());
            userName.getStyle().set("color", BRAND_VARIANT).set("font-weight", "bold").set("margin-right", "15px");
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
            registerBtn.getStyle().set("background-color", BRAND_COLOR);
            registerBtn.getStyle().set("color", "white");
            registerBtn.addClickListener(e -> NavigationManager.goToRegister());
            authLayout.add(loginBtn, registerBtn);
        }

        // RECHERCHE
        TextField searchInput = new TextField();
        searchInput.setPlaceholder("Rechercher...");
        searchInput.setVisible(false); searchInput.setWidth("0px");
        searchInput.getStyle().set("transition", "all 0.3s ease");

        Button searchBtn = new Button(new Image(ICON_PATH + "recherche.svg", ""));
        searchBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        searchBtn.addClickListener(e -> {
            if (!searchInput.isVisible()) {
                searchInput.setVisible(true); searchInput.setWidth("200px"); searchInput.focus();
            } else {
                searchInput.setVisible(false); searchInput.setWidth("0px");
            }
        });

        Button themeToggle = new Button(new Image(ICON_PATH + "dark_mode.svg", ""));
        themeToggle.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        themeToggle.addClickListener(e -> toggleTheme(themeToggle));

        rightSide.add(authLayout, searchInput, searchBtn, themeToggle);

        HorizontalLayout header = new HorizontalLayout();
        header.add(logo, navMenu, rightSide);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setWidthFull();
        header.getStyle().set("padding", "0 30px").set("background-color", "white").set("border-bottom", "1px solid #eee");
        header.setFlexGrow(1, navMenu);
        navMenu.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        addToNavbar(header);
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

    private Button createNavButton(String text, com.vaadin.flow.component.ComponentEventListener<com.vaadin.flow.component.ClickEvent<Button>> listener) {
        Button btn = new Button(text);
        btn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btn.getStyle().set("color", BRAND_COLOR).set("font-weight", "700").set("font-size", "1.1em");
        btn.addClickListener(listener);
        return btn;
    }

    private Anchor createFooterLink(String text, String route) {
        Anchor a = new Anchor(route, text);
        a.getStyle().set("color", "#475569").set("text-decoration", "none").set("font-size", "0.95em").set("margin-bottom", "10px").set("font-weight", "500");
        return a;
    }

    private Image createSocialIcon(String fileName) {
        Image img = new Image(ICON_PATH + fileName, "");
        img.setHeight("28px"); img.getStyle().set("cursor", "pointer");
        return img;
    }

    private void toggleTheme(Button btn) {
        isDarkMode = !isDarkMode;
        UI.getCurrent().getElement().setAttribute("theme", isDarkMode ? "dark" : "");
        btn.setIcon(new Image(ICON_PATH + (isDarkMode ? "light_mode.svg" : "dark_mode.svg"), ""));
    }

    private void createDrawer() {
        setDrawerOpened(false);
    }

    private void handleLogout() {
        VaadinSession.logout();
        NavigationManager.goToLogin();
    }
}