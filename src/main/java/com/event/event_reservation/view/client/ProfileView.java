package com.event.event_reservation.view.client;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.event.event_reservation.config.NavigationManager;
import com.event.event_reservation.config.VaadinSession;
import com.event.event_reservation.entity.User;
import com.event.event_reservation.entity.enums.UserRole;
import com.event.event_reservation.service.UserService;
import com.event.event_reservation.view.components.VaadinAppLayout;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "profile", layout = VaadinAppLayout.class)
@PageTitle("Mon Profil - Event Reservation")
public class ProfileView extends VerticalLayout {

    private final UserService userService;
    private final User currentUser;
    private final String BRAND_BLUE = "#253366";
    private final String ICON_PATH = "images/events/icons/";

    private TextField nomField, prenomField, emailField, telephoneField;

    @Autowired
    public ProfileView(UserService userService) {
        this.userService = userService;
        this.currentUser = VaadinSession.getCurrentUser();

        if (currentUser == null) {
            UI.getCurrent().navigate("login");
            return;
        }

        // Configuration Layout (Edge-to-Edge feel)
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().set("background-color", "#f8f9fa");

        // Container centré pour le contenu
        VerticalLayout content = new VerticalLayout();
        content.setWidthFull();
        content.setMaxWidth("1200px");
        content.getStyle().set("margin", "0 auto");
        content.setPadding(true);
        content.setSpacing(true);

        createHeader(content);
        // La section Statistiques a été supprimée d'ici

        // Grille pour les formulaires
        Div formsGrid = new Div();
        formsGrid.setWidthFull();
        formsGrid.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(auto-fit, minmax(400px, 1fr))")
                .set("gap", "30px")
                .set("margin-top", "20px");

        createProfileForm(formsGrid);
        createPasswordForm(formsGrid);

        content.add(formsGrid);
        createDangerZone(content);

        add(content);
    }

    private void createHeader(VerticalLayout container) {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);
        header.getStyle().set("margin", "30px 0");

        // --- LOGIQUE ICÔNE DYNAMIQUE MISE À JOUR (ADMIN INCLUS) ---
        String iconFile = "client.svg"; // Par défaut
        if (currentUser.getRole() == UserRole.ADMIN) {
            iconFile = "admin.svg";
        } else if (currentUser.getRole() == UserRole.ORGANIZER) {
            iconFile = "organizer.svg";
        }
        Image profileIcon = new Image(ICON_PATH + iconFile, "Profil");
        profileIcon.setWidth("60px");

        VerticalLayout textLayout = new VerticalLayout();
        textLayout.setPadding(false);
        textLayout.setSpacing(false);

        H1 title = new H1(currentUser.getPrenom() + " " + currentUser.getNom());
        title.getStyle()
                .set("color", BRAND_BLUE)
                .set("font-weight", "800")
                .set("margin", "0")
                .set("font-size", "2.5em");

        Span subtitle = new Span("Gestion de votre compte personnel");
        subtitle.getStyle().set("color", "#666").set("font-size", "1.1em");

        textLayout.add(title, subtitle);
        header.add(profileIcon, textLayout);
        container.add(header);
    }

    private void createProfileForm(Div parent) {
        VerticalLayout card = createStyledCard();
        card.add(createSectionHeader("information.svg", "Informations personnelles"));

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

        telephoneField = new TextField("Téléphone");
        telephoneField.setValue(currentUser.getTelephone() != null ? currentUser.getTelephone() : "");
        telephoneField.setWidthFull();

        Button saveBtn = new Button("Sauvegarder les modifications");
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveBtn.getStyle().set("background-color", BRAND_BLUE).set("margin-top", "20px");
        saveBtn.setWidthFull();
        saveBtn.addClickListener(e -> handleSaveProfile());

        card.add(nomField, prenomField, emailField, telephoneField, saveBtn);
        parent.add(card);
    }

    private void createPasswordForm(Div parent) {
        VerticalLayout card = createStyledCard();
        card.add(createSectionHeader("password.svg", "Sécurité du compte"));

        PasswordField oldP = new PasswordField("Mot de passe actuel");
        PasswordField newP = new PasswordField("Nouveau mot de passe");
        PasswordField confP = new PasswordField("Confirmer le mot de passe");
        oldP.setWidthFull(); newP.setWidthFull(); confP.setWidthFull();

        Button changeBtn = new Button("Mettre à jour le mot de passe");
        changeBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        changeBtn.getStyle().set("background-color", BRAND_BLUE).set("margin-top", "20px");
        changeBtn.setWidthFull();
        changeBtn.addClickListener(e -> handleChangePassword(oldP, newP, confP));

        card.add(oldP, newP, confP, changeBtn);
        parent.add(card);
    }

    private void createDangerZone(VerticalLayout container) {
        VerticalLayout card = createStyledCard();
        card.getStyle()
                .set("border", "1px solid #ffcccb")
                .set("background-color", "#fff5f5")
                .set("margin-top", "40px");

        card.add(createSectionHeader("danger.svg", "Zone de danger"));

        Span warning = new Span("La désactivation est définitive. Vous perdrez l'accès à vos billets et à l'historique.");
        warning.getStyle().set("color", "#c53030").set("font-weight", "500");

        Button deactivateBtn = new Button("Désactiver mon compte");
        deactivateBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deactivateBtn.getStyle().set("margin-top", "15px");
        deactivateBtn.addClickListener(e -> confirmDeactivation());

        card.add(warning, deactivateBtn);
        container.add(card);
    }

    // --- HELPERS STYLE 100% JAVA ---

    private HorizontalLayout createSectionHeader(String svgName, String title) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setAlignItems(Alignment.CENTER);
        layout.getStyle().set("margin-bottom", "20px");

        Image icon = new Image(ICON_PATH + svgName, "");
        icon.setWidth("24px");

        H2 h2 = new H2(title);
        h2.getStyle()
                .set("color", BRAND_BLUE)
                .set("margin", "0")
                .set("font-weight", "800")
                .set("font-size", "1.3em");

        layout.add(icon, h2);
        return layout;
    }

    private VerticalLayout createStyledCard() {
        VerticalLayout card = new VerticalLayout();
        card.setPadding(true);
        card.getStyle()
                .set("background-color", "white")
                .set("border-radius", "25px")
                .set("box-shadow", "0 10px 30px rgba(0,0,0,0.06)")
                .set("padding", "40px");
        return card;
    }

    private void handleSaveProfile() {
        try {
            userService.updateProfile(currentUser.getId(), nomField.getValue(), prenomField.getValue(), telephoneField.getValue());
            currentUser.setNom(nomField.getValue());
            currentUser.setPrenom(prenomField.getValue());
            VaadinSession.setCurrentUser(currentUser);
            Notification.show("Profil mis à jour", 3000, Notification.Position.TOP_CENTER).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            UI.getCurrent().getPage().reload();
        } catch (Exception e) {
            Notification.show("Erreur : " + e.getMessage(), 3000, Notification.Position.TOP_CENTER).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void handleChangePassword(PasswordField oldF, PasswordField newF, PasswordField confF) {
        if (!newF.getValue().equals(confF.getValue())) {
            Notification.show("Les mots de passe ne correspondent pas", 3000, Notification.Position.TOP_CENTER).addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }
        try {
            userService.changePassword(currentUser.getId(), oldF.getValue(), newF.getValue());
            Notification.show("Mot de passe modifié", 3000, Notification.Position.TOP_CENTER).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            oldF.clear(); newF.clear(); confF.clear();
        } catch (Exception e) {
            Notification.show(e.getMessage(), 3000, Notification.Position.TOP_CENTER).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void confirmDeactivation() {
        ConfirmDialog dialog = new ConfirmDialog("Confirmation", "Voulez-vous désactiver votre compte ?", "Désactiver", e -> handleDeactivation(), "Annuler", e -> {});
        dialog.setConfirmButtonTheme("error primary");
        dialog.open();
    }

    private void handleDeactivation() {
        try {
            userService.toggleUserActive(currentUser.getId(), false);
            VaadinSession.logout();
            UI.getCurrent().navigate("");
        } catch (Exception e) {
            Notification.show(e.getMessage());
        }
    }
}