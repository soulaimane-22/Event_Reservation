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

        // Configuration du conteneur racine
        setSizeFull();
        setPadding(false);
        setMargin(false);
        setSpacing(false);
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        // BACKGROUND UNI VIA JAVA
        getStyle().set("background-color", BRAND_BLUE);

        add(createLoginCard());
    }

    private VerticalLayout createLoginCard() {
        VerticalLayout card = new VerticalLayout();
        card.setWidth("450px");
        card.setSpacing(false);
        card.setPadding(false);
        card.setAlignItems(Alignment.CENTER);

        // Style de la carte via Java Style API
        var s = card.getStyle();
        s.set("background-color", "white");
        s.set("border-radius", "20px");
        s.set("box-shadow", "0 20px 40px rgba(0, 0, 0, 0.4)");
        s.set("padding", "20px 45px 40px 45px"); // Padding réduit en haut

        // 1. Logo cliquable (Très haut)
        Image logo = new Image("images/events/logos/OCCASIO_EVENT.svg", "Occasio");
        logo.setWidth("220px");
        logo.getStyle().set("cursor", "pointer");
        logo.getStyle().set("margin-bottom", "10px");
        logo.addClickListener(e -> UI.getCurrent().navigate(""));

        // 2. Textes
        Span welcomeText = new Span("Ravi de vous revoir");
        welcomeText.getStyle().set("color", BRAND_BLUE).set("font-size", "1.6em").set("font-weight", "800");

        Span instructions = new Span("Identifiez-vous pour continuer");
        instructions.getStyle().set("color", "#666").set("margin-bottom", "20px").set("font-size", "0.9em");

        // 3. Formulaire
        emailField = new EmailField("Adresse Email");
        emailField.setWidthFull();
        emailField.getStyle().set("margin-bottom", "10px");

        passwordField = new PasswordField("Mot de passe");
        passwordField.setWidthFull();
        passwordField.getStyle().set("margin-bottom", "20px");
        passwordField.addKeyPressListener(com.vaadin.flow.component.Key.ENTER, e -> handleLogin());

        // 4. Bouton
        Button loginButton = new Button("Se connecter", e -> handleLogin());
        loginButton.setWidthFull();
        loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        loginButton.getStyle().set("background-color", BRAND_BLUE).set("height", "50px").set("font-weight", "700");

        // 5. Lien inscription
        Div footer = new Div(new Span("Nouveau ici ? "), new Anchor("register", "Créer un compte"));
        footer.getStyle().set("margin-top", "20px").set("font-size", "0.9em");
        footer.getChildren().filter(c -> c instanceof Anchor).forEach(c -> {
            ((Anchor)c).getStyle().set("color", BRAND_BLUE).set("font-weight", "700").set("text-decoration", "none");
        });

        card.add(logo, welcomeText, instructions, emailField, passwordField, loginButton, footer);
        return card;
    }

    private void handleLogin() {
        if (emailField.isEmpty() || passwordField.isEmpty()) return;
        try {
            Optional<User> user = userService.authenticate(emailField.getValue(), passwordField.getValue());
            if (user.isPresent()) {
                VaadinSession.setCurrentUser(user.get());
                UI.getCurrent().navigate("");
            } else {
                Notification.show("Identifiants incorrects", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        } catch (Exception e) {
            Notification.show("Erreur de connexion").addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}