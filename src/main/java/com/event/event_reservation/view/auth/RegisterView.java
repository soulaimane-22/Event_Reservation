package com.event.event_reservation.view.auth;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.event.event_reservation.config.NavigationManager;
import com.event.event_reservation.entity.User;
import com.event.event_reservation.entity.enums.UserRole;
import com.event.event_reservation.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Page d'inscription
 * URL: /register
 */
@Route("register")
@PageTitle("Inscription - Event Reservation")
public class RegisterView extends VerticalLayout {

    private final UserService userService;

    private TextField nomField;
    private TextField prenomField;
    private EmailField emailField;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;
    private TextField telephoneField;
    private ComboBox<UserRole> roleComboBox;
    private Button registerButton;
    private Span passwordStrength;

    @Autowired
    public RegisterView(UserService userService) {
        this.userService = userService;

        // Configuration du layout
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        getStyle().set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)");

        // Créer le formulaire
        VerticalLayout registerForm = createRegisterForm();
        add(registerForm);
    }

    /**
     * Créer le formulaire d'inscription
     */
    private VerticalLayout createRegisterForm() {
        VerticalLayout form = new VerticalLayout();
        form.setWidth("450px");
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

        H2 subtitle = new H2("Créer un compte");
        subtitle.getStyle()
                .set("margin-top", "0")
                .set("text-align", "center")
                .set("color", "#333");

        // Champ Nom
        nomField = new TextField("Nom");
        nomField.setWidthFull();
        nomField.setPlaceholder("Dupont");
        nomField.setRequired(true);
        nomField.setMinLength(2);
        nomField.setMaxLength(50);

        // Champ Prénom
        prenomField = new TextField("Prénom");
        prenomField.setWidthFull();
        prenomField.setPlaceholder("Jean");
        prenomField.setRequired(true);
        prenomField.setMinLength(2);
        prenomField.setMaxLength(50);

        // Champ Email
        emailField = new EmailField("Email");
        emailField.setWidthFull();
        emailField.setPlaceholder("jean.dupont@email.com");
        emailField.setRequired(true);
        emailField.setErrorMessage("Format email invalide");

        // Champ Téléphone (optionnel)
        telephoneField = new TextField("Téléphone (optionnel)");
        telephoneField.setWidthFull();
        telephoneField.setPlaceholder("0612345678");
        telephoneField.setMaxLength(20);

        // Champ Rôle
        roleComboBox = new ComboBox<>("Rôle");
        roleComboBox.setWidthFull();
        roleComboBox.setItems(UserRole.CLIENT, UserRole.ORGANIZER);
        roleComboBox.setValue(UserRole.CLIENT);
        roleComboBox.setRequired(true);
        roleComboBox.setItemLabelGenerator(role -> {
            switch (role) {
                case CLIENT: return "👤 Client (réserver des événements)";
                case ORGANIZER: return "🎭 Organisateur (créer des événements)";
                default: return role.toString();
            }
        });

        // Champ Mot de passe
        passwordField = new PasswordField("Mot de passe");
        passwordField.setWidthFull();
        passwordField.setPlaceholder("Minimum 8 caractères");
        passwordField.setRequired(true);
        passwordField.setMinLength(8);

        // Indicateur de force du mot de passe
        passwordStrength = new Span();
        passwordStrength.getStyle().set("font-size", "0.85em");
        passwordField.addValueChangeListener(e -> updatePasswordStrength(e.getValue()));

        // Champ Confirmation mot de passe
        confirmPasswordField = new PasswordField("Confirmer le mot de passe");
        confirmPasswordField.setWidthFull();
        confirmPasswordField.setPlaceholder("Retapez votre mot de passe");
        confirmPasswordField.setRequired(true);

        // Bouton Inscription
        registerButton = new Button("S'inscrire");
        registerButton.setWidthFull();
        registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerButton.addClickListener(e -> handleRegister());

        // Lien vers connexion
        Paragraph loginText = new Paragraph("Déjà un compte ?");
        loginText.getStyle().set("text-align", "center").set("margin-top", "1em");

        RouterLink loginLink = new RouterLink("Se connecter", LoginView.class);
        loginLink.getStyle()
                .set("color", "#667eea")
                .set("font-weight", "bold")
                .set("text-decoration", "none");

        VerticalLayout loginSection = new VerticalLayout(loginText, loginLink);
        loginSection.setAlignItems(Alignment.CENTER);
        loginSection.setPadding(false);
        loginSection.setSpacing(false);

        // Ajouter tous les composants
        form.add(
                title,
                subtitle,
                nomField,
                prenomField,
                emailField,
                telephoneField,
                roleComboBox,
                passwordField,
                passwordStrength,
                confirmPasswordField,
                registerButton,
                loginSection
        );

        form.setAlignItems(Alignment.STRETCH);

        return form;
    }

    /**
     * Mettre à jour l'indicateur de force du mot de passe
     */
    private void updatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            passwordStrength.setText("");
            return;
        }

        int strength = calculatePasswordStrength(password);

        if (strength < 8) {
            passwordStrength.setText("⚠️ Trop faible");
            passwordStrength.getStyle().set("color", "#ef4444");
        } else if (strength < 12) {
            passwordStrength.setText("✓ Moyen");
            passwordStrength.getStyle().set("color", "#f59e0b");
        } else {
            passwordStrength.setText("✓✓ Fort");
            passwordStrength.getStyle().set("color", "#10b981");
        }
    }

    /**
     * Calculer la force du mot de passe
     */
    private int calculatePasswordStrength(String password) {
        int strength = password.length();

        if (password.matches(".*[A-Z].*")) strength += 2; // Majuscule
        if (password.matches(".*[a-z].*")) strength += 1; // Minuscule
        if (password.matches(".*[0-9].*")) strength += 2; // Chiffre
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) strength += 3; // Caractère spécial

        return strength;
    }

    /**
     * Gérer l'inscription
     */
    private void handleRegister() {
        // Récupérer les valeurs
        String nom = nomField.getValue().trim();
        String prenom = prenomField.getValue().trim();
        String email = emailField.getValue().trim();
        String telephone = telephoneField.getValue().trim();
        UserRole role = roleComboBox.getValue();
        String password = passwordField.getValue();
        String confirmPassword = confirmPasswordField.getValue();

        // Validation
        if (nom.isEmpty()) {
            showError("Le nom est obligatoire");
            nomField.focus();
            return;
        }

        if (prenom.isEmpty()) {
            showError("Le prénom est obligatoire");
            prenomField.focus();
            return;
        }

        if (email.isEmpty()) {
            showError("L'email est obligatoire");
            emailField.focus();
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            showError("Format d'email invalide");
            emailField.focus();
            return;
        }

        if (role == null) {
            showError("Veuillez sélectionner un rôle");
            roleComboBox.focus();
            return;
        }

        if (password.isEmpty()) {
            showError("Le mot de passe est obligatoire");
            passwordField.focus();
            return;
        }

        if (password.length() < 8) {
            showError("Le mot de passe doit contenir au moins 8 caractères");
            passwordField.focus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Les mots de passe ne correspondent pas");
            confirmPasswordField.focus();
            confirmPasswordField.clear();
            return;
        }

        // Inscription
        try {
            User user = userService.registerUser(nom, prenom, email, password, role);

            showSuccess("Inscription réussie ! Vous pouvez maintenant vous connecter.");

            // Rediriger vers login après 2 secondes
            getUI().ifPresent(ui -> ui.access(() -> {
                try {
                    Thread.sleep(2000);
                    NavigationManager.goToLogin();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }));

        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            showError("Erreur lors de l'inscription : " + e.getMessage());
        }
    }

    /**
     * Afficher un message d'erreur
     */
    private void showError(String message) {
        Notification notification = Notification.show(message, 4000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    /**
     * Afficher un message de succès
     */
    private void showSuccess(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }
}
