package com.event.event_reservation.view.auth;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.event.event_reservation.config.NavigationManager;
import com.event.event_reservation.config.VaadinSession;
import com.event.event_reservation.entity.User;
import com.event.event_reservation.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

@Route("login")
@PageTitle("Connexion - Event Reservation")
@AnonymousAllowed
public class LoginView extends VerticalLayout {

    private final UserService userService;
    private final String BRAND_BLUE = "#253366";

    private EmailField emailField;
    private PasswordField passwordField;

    @Autowired
    public LoginView(UserService userService) {
        this.userService = userService;

        // --- FIX BACKGROUND UNIQUE (PLEIN ÉCRAN) ---
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        setMargin(false);
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        getStyle().set("background", "linear-gradient(135deg, " + BRAND_BLUE + " 0%, #435591 100%)");
        getStyle().set("background-attachment", "fixed");
        getStyle().set("min-height", "100vh");
        getStyle().set("height", "auto");

        add(createLoginCard());
    }

    private VerticalLayout createLoginCard() {
        VerticalLayout card = new VerticalLayout();
        card.setWidth("450px");
        card.setPadding(true);

        // --- FIX ESPACEMENT ---
        card.setSpacing(false); // Désactive l'espace automatique entre TOUS les éléments

        card.getStyle()
                .set("background", "white")
                .set("border-radius", "20px")
                .set("box-shadow", "0 20px 40px rgba(0, 0, 0, 0.3)")
                .set("padding", "50px")
                .set("margin", "20px 0");

        // 1. LOGO (CLIQUABLE ET MARGE RÉDUITE)
        Image logo = new Image("images/events/logos/OCCASIO_EVENT.svg", "Occasio Event");
        logo.setWidth("240px");
        logo.getStyle()
                .set("margin", "0 auto 10px auto") // Marge réduite de 30px à 10px
                .set("cursor", "pointer");
        logo.addClickListener(e -> UI.getCurrent().navigate(""));

        // 2. TITRE (RAVI DE VOUS REVOIR)
        Span welcomeText = new Span("Ravi de vous revoir");
        welcomeText.getStyle()
                .set("color", BRAND_BLUE)
                .set("font-size", "1.8em")
                .set("font-weight", "800")
                .set("margin", "0 0 5px 0"); // Marge contrôlée

        // 3. SOUS-TITRE
        Span instructions = new Span("Veuillez saisir vos identifiants pour accéder à votre espace.");
        instructions.getStyle()
                .set("color", "#666")
                .set("font-size", "0.95em")
                .set("text-align", "center")
                .set("margin", "0 0 25px 0"); // Espace avant les champs de texte

        // 4. FORMULAIRE
        emailField = new EmailField("Adresse Email");
        emailField.setWidthFull();
        emailField.getStyle().set("margin-bottom", "15px");

        passwordField = new PasswordField("Mot de passe");
        passwordField.setWidthFull();
        passwordField.getStyle().set("margin-bottom", "20px");

        passwordField.addKeyPressListener(e -> {
            if (e.getKey().getKeys().contains("Enter")) handleLogin();
        });

        // 5. BOUTON
        Button loginButton = new Button("Se connecter");
        loginButton.setWidthFull();
        loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        loginButton.getStyle()
                .set("background-color", BRAND_BLUE)
                .set("height", "55px")
                .set("font-weight", "600")
                .set("font-size", "1.1em");
        loginButton.addClickListener(e -> handleLogin());

        // 6. FOOTER
        Div footer = new Div();
        footer.getStyle()
                .set("text-align", "center")
                .set("margin-top", "25px")
                .set("width", "100%");

        Span noAccount = new Span("Pas encore de compte ? ");
        noAccount.getStyle().set("color", "#666").set("font-size", "0.9em");

        Anchor registerLink = new Anchor("register", "S'inscrire gratuitement");
        registerLink.getStyle()
                .set("color", BRAND_BLUE)
                .set("font-weight", "700")
                .set("text-decoration", "none")
                .set("font-size", "0.9em");

        footer.add(noAccount, registerLink);

        // Assemblage
        card.add(logo, welcomeText, instructions, emailField, passwordField, loginButton, footer);
        card.setAlignItems(Alignment.CENTER);

        return card;
    }

    private void handleLogin() {
        String email = emailField.getValue().trim();
        String password = passwordField.getValue();

        if (email.isEmpty() || password.isEmpty()) {
            showNotification("Veuillez remplir tous les champs", NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            Optional<User> userOptional = userService.authenticate(email, password);
            if (userOptional.isPresent()) {
                VaadinSession.setCurrentUser(userOptional.get());
                showNotification("Connexion réussie", NotificationVariant.LUMO_SUCCESS);
                UI.getCurrent().navigate("");
            } else {
                showNotification("Identifiants incorrects", NotificationVariant.LUMO_ERROR);
                passwordField.clear();
            }
        } catch (Exception e) {
            showNotification("Erreur de connexion", NotificationVariant.LUMO_ERROR);
        }
    }

    private void showNotification(String text, NotificationVariant variant) {
        Notification n = Notification.show(text, 3000, Notification.Position.TOP_CENTER);
        n.addThemeVariants(variant);
    }
}