package com.event.event_reservation.view.auth;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Anchor; // <--- Import ajouté
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AnonymousAllowed; // <--- Import ajouté
import com.event.event_reservation.config.NavigationManager;
import com.event.event_reservation.config.VaadinSession;
import com.event.event_reservation.entity.User;
import com.event.event_reservation.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

/**
 * Page de connexion
 * URL: /login
 */
@Route("login")
@PageTitle("Connexion - Event Reservation")
@AnonymousAllowed // <--- Autorise l'accès sans être connecté
public class LoginView extends VerticalLayout {

    private final UserService userService;

    private EmailField emailField;
    private PasswordField passwordField;
    private Button loginButton;
    // private Button registerLink; // Inutilisé, je l'ai retiré

    @Autowired
    public LoginView(UserService userService) {
        this.userService = userService;

        // Configuration du layout
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        getStyle().set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)");

        // Créer le formulaire
        VerticalLayout loginForm = createLoginForm();
        add(loginForm);
    }

    /**
     * Créer le formulaire de connexion
     */
    private VerticalLayout createLoginForm() {
        VerticalLayout form = new VerticalLayout();
        form.setWidth("400px");
        form.setPadding(true);
        form.setSpacing(true);
        form.getStyle()
                .set("background", "white")
                .set("border-radius", "10px")
                .set("box-shadow", "0 4px 6px rgba(0, 0, 0, 0.1)");

        // Titre
        H1 title = new H1("🎭 Event Reservation");
        title.getStyle()
                .set("margin", "0")
                .set("color", "#667eea")
                .set("text-align", "center");

        H2 subtitle = new H2("Connexion");
        subtitle.getStyle()
                .set("margin-top", "0")
                .set("text-align", "center")
                .set("color", "#333");

        // Champ Email
        emailField = new EmailField("Email");
        emailField.setWidthFull();
        emailField.setPlaceholder("exemple@email.com");
        emailField.setRequired(true);
        emailField.setErrorMessage("Email invalide");

        // Champ Mot de passe
        passwordField = new PasswordField("Mot de passe");
        passwordField.setWidthFull();
        passwordField.setPlaceholder("Entrez votre mot de passe");
        passwordField.setRequired(true);

        // Bouton Connexion
        loginButton = new Button("Se connecter");
        loginButton.setWidthFull();
        loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        loginButton.addClickListener(e -> handleLogin());

        // Permettre la connexion avec la touche Entrée
        passwordField.addKeyPressListener(e -> {
            if (e.getKey().getKeys().contains("Enter")) {
                handleLogin();
            }
        });

        // Lien vers inscription
        Paragraph registerText = new Paragraph("Pas encore de compte ?");
        registerText.getStyle().set("text-align", "center").set("margin-top", "1em");

        // --- CORRECTION 1 : Sécurisation du lien Register ---
        // J'utilise un Anchor (lien HTML) au lieu de RouterLink pour éviter le crash
        // si la classe RegisterView n'est pas encore prête.
        Anchor registerLink = new Anchor("register", "Créer un compte");
        // Si ta vue s'appelle RegisterView, assure-toi qu'elle a bien @Route("register")
        registerLink.getStyle()
                .set("color", "#667eea")
                .set("font-weight", "bold")
                .set("text-decoration", "none");

        VerticalLayout registerSection = new VerticalLayout(registerText, registerLink);
        registerSection.setAlignItems(Alignment.CENTER);
        registerSection.setPadding(false);
        registerSection.setSpacing(false);

        // --- CORRECTION 2 : Le lien Accueil (Source du crash NullPointerException) ---
        // Au lieu de new RouterLink(..., null), on utilise un Anchor vers la racine "/"
        Anchor homeLink = new Anchor("/", "← Retour à l'accueil");
        homeLink.getStyle()
                .set("color", "#666")
                .set("text-decoration", "none")
                .set("margin-top", "1em");

        // Ajouter tous les composants au formulaire
        form.add(
                title,
                subtitle,
                emailField,
                passwordField,
                loginButton,
                registerSection,
                homeLink
        );

        form.setAlignItems(Alignment.CENTER);

        return form;
    }

    /**
     * Gérer la connexion
     */
    private void handleLogin() {
        // Validation des champs
        String email = emailField.getValue().trim();
        String password = passwordField.getValue();

        if (email.isEmpty()) {
            showError("Veuillez entrer votre email");
            emailField.focus();
            return;
        }

        if (password.isEmpty()) {
            showError("Veuillez entrer votre mot de passe");
            passwordField.focus();
            return;
        }

        // Authentification
        try {
            Optional<User> userOptional = userService.authenticate(email, password);

            if (userOptional.isPresent()) {
                User user = userOptional.get();

                // Stocker l'utilisateur dans la session
                VaadinSession.setCurrentUser(user);

                // Afficher message de succès
                showSuccess("Connexion réussie ! Bienvenue " + user.getPrenom());

                // Rediriger selon le rôle
                NavigationManager.redirectByRole(user.getRole());
            } else {
                showError("Email ou mot de passe incorrect");
                passwordField.clear();
                passwordField.focus();
            }
        } catch (Exception e) {
            showError("Erreur lors de la connexion : " + e.getMessage());
        }
    }

    private void showError(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    private void showSuccess(String message) {
        Notification notification = Notification.show(message, 2000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }
}