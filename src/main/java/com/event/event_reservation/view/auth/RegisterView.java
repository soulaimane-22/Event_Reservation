package com.event.event_reservation.view.auth;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.event.event_reservation.config.NavigationManager;
import com.event.event_reservation.entity.User;
import com.event.event_reservation.entity.enums.UserRole;
import com.event.event_reservation.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

@Route("register")
@PageTitle("Inscription - Event Reservation")
public class RegisterView extends VerticalLayout {

    private final UserService userService;
    private final String BRAND_BLUE = "#253366";
    private final String ICON_PATH = "images/events/icons/";

    private TextField nomField, prenomField, telephoneField;
    private EmailField emailField;
    private PasswordField passwordField, confirmPasswordField;
    private ComboBox<UserRole> roleComboBox;
    private Span passwordStrength;

    @Autowired
    public RegisterView(UserService userService) {
        this.userService = userService;

        // --- FIX BACKGROUND UNIQUE ---
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        setMargin(false);
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        // On applique le background sur le conteneur racine avec min-height pour éviter les coupures
        getStyle().set("background", "linear-gradient(135deg, " + BRAND_BLUE + " 0%, #435591 100%)");
        getStyle().set("background-attachment", "fixed");
        getStyle().set("min-height", "100vh");
        getStyle().set("height", "auto"); // Permet l'extension si le formulaire est long

        add(createRegisterCard());
    }

    private VerticalLayout createRegisterCard() {
        VerticalLayout card = new VerticalLayout();
        card.setWidth("600px");
        card.setPadding(true);
        card.setSpacing(true);
        card.getStyle()
                .set("background", "white")
                .set("border-radius", "20px")
                .set("box-shadow", "0 20px 40px rgba(0, 0, 0, 0.3)")
                .set("padding", "40px")
                .set("margin", "40px 0"); // Marge pour ne pas coller aux bords sur mobile

        // 1. LOGO XXL (CLIQUABLE)
        Image logo = new Image("images/events/logos/OCCASIO_EVENT.svg", "Occasio Event");
        logo.setWidth("220px");
        logo.getStyle().set("margin", "0 auto 10px auto");
        logo.getStyle().set("cursor", "pointer");
        logo.addClickListener(e -> UI.getCurrent().navigate(""));

        H2 subtitle = new H2("Créer un compte");
        subtitle.getStyle().set("color", BRAND_BLUE).set("margin", "0 auto 30px auto").set("font-weight", "800");

        // --- SECTION 1 : INFORMATIONS PERSONNELLES ---
        personalSection(card);

        card.add(new Hr());

        // --- SECTION 2 : SÉCURITÉ & COMPTE ---
        accountSection(card);

        // BOUTON INSCRIPTION
        Button registerButton = new Button("Créer mon compte");
        registerButton.setWidthFull();
        registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerButton.getStyle()
                .set("background-color", BRAND_BLUE)
                .set("height", "55px")
                .set("margin-top", "20px")
                .set("font-weight", "700");
        registerButton.addClickListener(e -> handleRegister());

        // LIEN CONNEXION
        Div footer = new Div(new Span("Déjà inscrit ? "), new Anchor("login", "Se connecter"));
        footer.getStyle().set("text-align", "center").set("margin-top", "20px");
        footer.getChildren().filter(c -> c instanceof Anchor).forEach(c ->
                ((Anchor)c).getStyle().set("color", BRAND_BLUE).set("font-weight", "700").set("text-decoration", "none")
        );

        card.add(registerButton, footer);
        card.setAlignItems(Alignment.CENTER);
        return card;
    }

    private void personalSection(VerticalLayout card) {
        card.add(createSectionHeader("Informations Personnelles"));

        nomField = new TextField("Nom");
        prenomField = new TextField("Prénom");
        HorizontalLayout nameLayout = new HorizontalLayout(nomField, prenomField);
        nameLayout.setWidthFull();
        nomField.setWidthFull();
        prenomField.setWidthFull();

        telephoneField = new TextField("Téléphone (optionnel)");
        telephoneField.setWidthFull();

        card.add(nameLayout, telephoneField);
    }

    private void accountSection(VerticalLayout card) {
        card.add(createSectionHeader("Sécurité & Compte"));

        emailField = new EmailField("Adresse Email");
        emailField.setWidthFull();

        // --- COMBOBOX AVEC ICÔNES SVG ---
        roleComboBox = new ComboBox<>("Type de profil");
        roleComboBox.setWidthFull();
        roleComboBox.setItems(UserRole.CLIENT, UserRole.ORGANIZER);
        roleComboBox.setValue(UserRole.CLIENT);

        // Rendu des icônes dans la liste
        roleComboBox.setRenderer(new ComponentRenderer<>(role -> {
            HorizontalLayout row = new HorizontalLayout();
            row.setAlignItems(Alignment.CENTER);

            // On utilise client.svg ou organizer.svg
            String iconFile = (role == UserRole.CLIENT) ? "client.svg" : "organizer.svg";
            Image icon = new Image(ICON_PATH + iconFile, "");
            icon.setWidth("20px");

            Span text = new Span(role == UserRole.CLIENT ? "Client (Acheteur)" : "Organisateur (Vendeur)");
            row.add(icon, text);
            return row;
        }));

        // Label pour l'élément sélectionné
        roleComboBox.setItemLabelGenerator(role -> role == UserRole.CLIENT ? "Client (Acheteur)" : "Organisateur (Vendeur)");

        passwordField = new PasswordField("Mot de passe");
        passwordField.setWidthFull();
        passwordStrength = new Span();
        passwordStrength.getStyle().set("font-size", "0.8em");
        passwordField.addValueChangeListener(e -> updatePasswordStrength(e.getValue()));

        confirmPasswordField = new PasswordField("Confirmer le mot de passe");
        confirmPasswordField.setWidthFull();

        card.add(emailField, roleComboBox, passwordField, passwordStrength, confirmPasswordField);
    }

    private Span createSectionHeader(String text) {
        Span header = new Span(text);
        header.getStyle()
                .set("color", BRAND_BLUE)
                .set("font-weight", "700")
                .set("font-size", "0.85em")
                .set("text-transform", "uppercase")
                .set("display", "block")
                .set("margin-top", "10px");
        return header;
    }

    private void updatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) { passwordStrength.setText(""); return; }
        int strength = password.length();
        if (strength < 8) {
            passwordStrength.setText("Sécurité : Faible");
            passwordStrength.getStyle().set("color", "#ef4444");
        } else {
            passwordStrength.setText("Sécurité : Optimale");
            passwordStrength.getStyle().set("color", "#10b981");
        }
    }

    private void handleRegister() {
        if (nomField.isEmpty() || prenomField.isEmpty() || emailField.isEmpty() || passwordField.isEmpty()) {
            showNotification("Tous les champs sont obligatoires", NotificationVariant.LUMO_ERROR);
            return;
        }
        if (!passwordField.getValue().equals(confirmPasswordField.getValue())) {
            showNotification("Les mots de passe ne correspondent pas", NotificationVariant.LUMO_ERROR);
            return;
        }
        try {
            userService.registerUser(nomField.getValue(), prenomField.getValue(),
                    emailField.getValue(), passwordField.getValue(),
                    roleComboBox.getValue());
            showNotification("Bienvenue ! Votre compte a été créé.", NotificationVariant.LUMO_SUCCESS);
            UI.getCurrent().navigate("login");
        } catch (Exception e) {
            showNotification(e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void showNotification(String text, NotificationVariant variant) {
        Notification n = Notification.show(text, 3000, Notification.Position.TOP_CENTER);
        n.addThemeVariants(variant);
    }
}