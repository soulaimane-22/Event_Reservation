package com.event.event_reservation.view.client;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.event.event_reservation.config.NavigationManager;
import com.event.event_reservation.config.VaadinSession;
import com.event.event_reservation.dto.UserStatisticsDTO;
import com.event.event_reservation.entity.User;
import com.event.event_reservation.service.UserService;
import com.event.event_reservation.view.components.VaadinAppLayout;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Page Mon Profil
 * URL: /profile
 */
@Route(value = "profile", layout = VaadinAppLayout.class)
@PageTitle("Mon Profil - Event Reservation")
public class ProfileView extends VerticalLayout {

    private final UserService userService;

    private User currentUser;

    private TextField nomField;
    private TextField prenomField;
    private TextField emailField;
    private TextField telephoneField;

    @Autowired
    public ProfileView(UserService userService) {
        this.userService = userService;

        currentUser = VaadinSession.getCurrentUser();
        if (currentUser == null) {
            NavigationManager.goToLogin();
            return;
        }

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        setMaxWidth("800px");
        getStyle().set("margin", "0 auto");

        createHeader();
        createProfileForm();
        createPasswordForm();
        createStatistics();
        createDangerZone();
    }

    /**
     * Créer le header
     */
    private void createHeader() {
        H1 title = new H1("👤 Mon Profil");
        title.getStyle().set("margin-bottom", "0");

        Span subtitle = new Span("Gérez vos informations personnelles");
        subtitle.getStyle().set("color", "var(--lumo-secondary-text-color)");

        add(title, subtitle);
    }

    /**
     * Créer le formulaire de profil
     */
    private void createProfileForm() {
        VerticalLayout card = createCard();

        H2 cardTitle = new H2("📝 Informations personnelles");
        cardTitle.getStyle().set("margin-top", "0");

        nomField = new TextField("Nom");
        nomField.setValue(currentUser.getNom());
        nomField.setWidthFull();

        prenomField = new TextField("Prénom");
        prenomField.setValue(currentUser.getPrenom());
        prenomField.setWidthFull();

        emailField = new TextField("Email");
        emailField.setValue(currentUser.getEmail());
        emailField.setWidthFull();
        emailField.setEnabled(false);
        emailField.setHelperText("L'email ne peut pas être modifié");

        telephoneField = new TextField("Téléphone");
        if (currentUser.getTelephone() != null) {
            telephoneField.setValue(currentUser.getTelephone());
        }
        telephoneField.setWidthFull();

        Button saveBtn = new Button("Sauvegarder", VaadinIcon.CHECK.create());
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveBtn.addClickListener(e -> handleSaveProfile());

        card.add(cardTitle, nomField, prenomField, emailField, telephoneField, saveBtn);
        add(card);
    }

    /**
     * Créer le formulaire de changement de mot de passe
     */
    private void createPasswordForm() {
        VerticalLayout card = createCard();

        H2 cardTitle = new H2("🔒 Changer le mot de passe");
        cardTitle.getStyle().set("margin-top", "0");

        PasswordField oldPasswordField = new PasswordField("Mot de passe actuel");
        oldPasswordField.setWidthFull();

        PasswordField newPasswordField = new PasswordField("Nouveau mot de passe");
        newPasswordField.setWidthFull();
        newPasswordField.setHelperText("Minimum 8 caractères");

        PasswordField confirmPasswordField = new PasswordField("Confirmer le mot de passe");
        confirmPasswordField.setWidthFull();

        Button changePasswordBtn = new Button("Changer le mot de passe", VaadinIcon.KEY.create());
        changePasswordBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        changePasswordBtn.addClickListener(e ->
                handleChangePassword(oldPasswordField, newPasswordField, confirmPasswordField)
        );

        card.add(cardTitle, oldPasswordField, newPasswordField, confirmPasswordField, changePasswordBtn);
        add(card);
    }

    /**
     * Créer les statistiques
     */
    private void createStatistics() {
        VerticalLayout card = createCard();

        H2 cardTitle = new H2("📊 Mes statistiques");
        cardTitle.getStyle().set("margin-top", "0");

        UserStatisticsDTO stats = userService.getUserStatistics(currentUser.getId());

        HorizontalLayout statsLayout = new HorizontalLayout();
        statsLayout.setWidthFull();
        statsLayout.setSpacing(true);
        statsLayout.getStyle().set("flex-wrap", "wrap");

        statsLayout.add(
                createStatItem("Réservations", String.valueOf(stats.getReservationsCount()), "#3b82f6"),
                createStatItem("Événements créés", String.valueOf(stats.getEventsCreated()), "#8b5cf6"),
                createStatItem("Dépensé", stats.getTotalSpent() + " DH", "#10b981")
        );

        card.add(cardTitle, statsLayout);
        add(card);
    }

    /**
     * Créer un item de statistique
     */
    private VerticalLayout createStatItem(String label, String value, String color) {
        VerticalLayout item = new VerticalLayout();
        item.setPadding(true);
        item.setSpacing(false);
        item.getStyle()
                .set("background", "#f9fafb")
                .set("border-radius", "8px")
                .set("min-width", "150px");

        Span valueSpan = new Span(value);
        valueSpan.getStyle()
                .set("font-size", "2em")
                .set("font-weight", "bold")
                .set("color", color);

        Span labelSpan = new Span(label);
        labelSpan.getStyle().set("color", "#666");

        item.add(valueSpan, labelSpan);
        return item;
    }

    /**
     * Créer la zone dangereuse
     */
    private void createDangerZone() {
        VerticalLayout card = createCard();
        card.getStyle().set("border", "2px solid #ef4444");

        H2 cardTitle = new H2("⚠️ Zone dangereuse");
        cardTitle.getStyle().set("margin-top", "0").set("color", "#ef4444");

        Span warning = new Span("La désactivation de votre compte entraînera la perte d'accès à toutes vos réservations.");
        warning.getStyle().set("color", "#666");

        Button deactivateBtn = new Button("Désactiver mon compte", VaadinIcon.BAN.create());
        deactivateBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deactivateBtn.addClickListener(e -> confirmDeactivation());

        card.add(cardTitle, warning, deactivateBtn);
        add(card);
    }

    /**
     * Créer une card
     */
    private VerticalLayout createCard() {
        VerticalLayout card = new VerticalLayout();
        card.setPadding(true);
        card.setSpacing(true);
        card.getStyle()
                .set("background", "white")
                .set("border-radius", "10px")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.1)")
                .set("margin-top", "1.5em");
        return card;
    }

    /**
     * Gérer la sauvegarde du profil
     */
    private void handleSaveProfile() {
        String nom = nomField.getValue();
        String prenom = prenomField.getValue();
        String telephone = telephoneField.getValue();

        if (nom.trim().isEmpty() || prenom.trim().isEmpty()) {
            showError("Le nom et le prénom sont obligatoires");
            return;
        }

        try {
            userService.updateProfile(currentUser.getId(), nom, prenom, telephone);

            // Mettre à jour l'utilisateur en session
            currentUser.setNom(nom);
            currentUser.setPrenom(prenom);
            currentUser.setTelephone(telephone);
            VaadinSession.setCurrentUser(currentUser);

            showSuccess("Profil mis à jour avec succès");

        } catch (Exception e) {
            showError("Erreur lors de la mise à jour : " + e.getMessage());
        }
    }

    /**
     * Gérer le changement de mot de passe
     */
    private void handleChangePassword(PasswordField oldField, PasswordField newField, PasswordField confirmField) {
        String oldPassword = oldField.getValue();
        String newPassword = newField.getValue();
        String confirmPassword = confirmField.getValue();

        if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showError("Tous les champs sont obligatoires");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showError("Les mots de passe ne correspondent pas");
            return;
        }

        if (newPassword.length() < 8) {
            showError("Le mot de passe doit contenir au moins 8 caractères");
            return;
        }

        try {
            userService.changePassword(currentUser.getId(), oldPassword, newPassword);

            showSuccess("Mot de passe changé avec succès");

            oldField.clear();
            newField.clear();
            confirmField.clear();

        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }

    /**
     * Confirmer la désactivation
     */
    private void confirmDeactivation() {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("⚠️ Désactiver le compte ?");
        dialog.setText("Êtes-vous sûr de vouloir désactiver votre compte ? Cette action peut être annulée ultérieurement par un administrateur.");

        dialog.setCancelable(true);
        dialog.setCancelText("Annuler");

        dialog.setConfirmText("Oui, désactiver");
        dialog.setConfirmButtonTheme("error primary");

        dialog.addConfirmListener(e -> handleDeactivation());

        dialog.open();
    }

    /**
     * Gérer la désactivation
     */
    private void handleDeactivation() {
        try {
            userService.toggleUserActive(currentUser.getId(), false);

            showSuccess("Compte désactivé. Vous allez être déconnecté.");

            // Déconnexion après 2 secondes
            getUI().ifPresent(ui -> ui.access(() -> {
                try {
                    Thread.sleep(2000);
                    VaadinSession.logout();
                    NavigationManager.goToLogin();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }));

        } catch (Exception e) {
            showError("Erreur lors de la désactivation : " + e.getMessage());
        }
    }

    /**
     * Afficher un message de succès
     */
    private void showSuccess(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    /**
     * Afficher un message d'erreur
     */
    private void showError(String message) {
        Notification notification = Notification.show(message, 4000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}