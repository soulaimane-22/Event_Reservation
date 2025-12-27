package com.event.event_reservation.view.auth;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
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

        // Configuration du conteneur racine
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        setMargin(false);
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        // BACKGROUND : UNE SEULE COULEUR UNIE (JAVA ONLY)
        getStyle().set("background-color", BRAND_BLUE);
        getStyle().set("min-height", "100vh");
        getStyle().set("height", "auto");

        add(createRegisterCard());
    }

    private VerticalLayout createRegisterCard() {
        VerticalLayout card = new VerticalLayout();
        card.setWidth("950px");
        card.setSpacing(false);
        card.setPadding(false);
        card.setAlignItems(Alignment.CENTER);

        // Style de la carte
        var s = card.getStyle();
        s.set("background-color", "white");
        s.set("border-radius", "25px");
        s.set("box-shadow", "0 25px 50px rgba(0, 0, 0, 0.3)");
        s.set("padding", "20px 50px 30px 50px"); // Padding réduit en haut pour remonter le contenu
        s.set("margin", "20px 0");

        // 1. Logo (Cliquable, marge minimale)
        Image logo = new Image("images/events/logos/OCCASIO_EVENT.svg", "Occasio");
        logo.setWidth("200px");
        logo.getStyle().set("cursor", "pointer");
        logo.getStyle().set("margin-bottom", "0px");
        logo.addClickListener(e -> UI.getCurrent().navigate(""));

        // 2. Titre (Marge réduite)
        H2 subtitle = new H2("Création de votre compte");
        subtitle.getStyle().set("color", BRAND_BLUE);
        subtitle.getStyle().set("margin", "0 0 20px 0");
        subtitle.getStyle().set("font-weight", "800");

        // 3. Conteneur des deux colonnes (Alignement START pour remonter les champs)
        HorizontalLayout columnsContainer = new HorizontalLayout();
        columnsContainer.setWidthFull();
        columnsContainer.setSpacing(false);
        columnsContainer.setAlignItems(Alignment.START);
        columnsContainer.setJustifyContentMode(JustifyContentMode.BETWEEN);

        // --- COLONNE GAUCHE ---
        VerticalLayout leftCol = new VerticalLayout();
        leftCol.setPadding(false);
        leftCol.setSpacing(true);
        leftCol.setWidth("45%");
        buildLeftColumn(leftCol);

        // --- SÉPARATEUR VERTICAL ---
        Span separator = new Span();
        separator.setWidth("1px");
        separator.setHeight("320px");
        separator.getStyle().set("background-color", "#E0E0E0");
        separator.getStyle().set("margin", "10px 20px 0 20px");

        // --- COLONNE DROITE ---
        VerticalLayout rightCol = new VerticalLayout();
        rightCol.setPadding(false);
        rightCol.setSpacing(true);
        rightCol.setWidth("45%");
        buildRightColumn(rightCol);

        columnsContainer.add(leftCol, separator, rightCol);

        // 4. Bouton de validation (Remonté)
        Button regBtn = new Button("Finaliser mon inscription", e -> handleRegister());
        regBtn.setWidth("400px");
        regBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        regBtn.getStyle().set("background-color", BRAND_BLUE);
        regBtn.getStyle().set("height", "55px");
        regBtn.getStyle().set("margin-top", "25px");
        regBtn.getStyle().set("font-weight", "700");

        // 5. Footer (Lien connexion)
        Div footer = new Div(new Span("Déjà inscrit ? "), new Anchor("login", "Se connecter"));
        footer.getStyle().set("margin-top", "15px");
        footer.getStyle().set("font-size", "0.95em");
        // Style du lien via Java
        footer.getChildren().filter(c -> c instanceof Anchor).forEach(c -> {
            var anchorStyle = ((Anchor)c).getStyle();
            anchorStyle.set("color", BRAND_BLUE);
            anchorStyle.set("font-weight", "700");
            anchorStyle.set("text-decoration", "none");
        });

        card.add(logo, subtitle, columnsContainer, regBtn, footer);
        return card;
    }

    private void buildLeftColumn(VerticalLayout col) {
        col.add(createHeader("Informations Personnelles"));

        nomField = new TextField("Nom");
        nomField.setWidthFull();
        nomField.setPlaceholder("Ex: Bennani");

        prenomField = new TextField("Prénom");
        prenomField.setWidthFull();
        prenomField.setPlaceholder("Ex: Youssef");

        telephoneField = new TextField("Téléphone");
        telephoneField.setWidthFull();
        telephoneField.setPlaceholder("06... ou 07...");

        col.add(nomField, prenomField, telephoneField);
    }

    private void buildRightColumn(VerticalLayout col) {
        col.add(createHeader("Sécurité & Compte"));

        emailField = new EmailField("Adresse Email");
        emailField.setWidthFull();
        emailField.setPlaceholder("votre@email.com");

        roleComboBox = new ComboBox<>("Type de profil");
        roleComboBox.setWidthFull();
        roleComboBox.setItems(UserRole.CLIENT, UserRole.ORGANIZER);
        roleComboBox.setValue(UserRole.CLIENT);
        roleComboBox.setRenderer(new ComponentRenderer<>(role -> {
            HorizontalLayout row = new HorizontalLayout();
            Image icon = new Image(ICON_PATH + (role == UserRole.CLIENT ? "client.svg" : "organizer.svg"), "");
            icon.setWidth("18px");
            row.add(icon, new Span(role == UserRole.CLIENT ? "Client (Acheteur)" : "Organisateur (Vendeur)"));
            row.setAlignItems(Alignment.CENTER);
            return row;
        }));
        roleComboBox.setItemLabelGenerator(r -> r == UserRole.CLIENT ? "Client" : "Organisateur");

        passwordField = new PasswordField("Mot de passe");
        passwordField.setWidthFull();

        passwordStrength = new Span();
        passwordStrength.getStyle().set("font-size", "0.8em").set("margin-top", "-5px");
        passwordField.addValueChangeListener(e -> updateStrength(e.getValue()));

        confirmPasswordField = new PasswordField("Confirmer le mot de passe");
        confirmPasswordField.setWidthFull();

        col.add(emailField, roleComboBox, passwordField, passwordStrength, confirmPasswordField);
    }

    private Span createHeader(String text) {
        Span h = new Span(text);
        var s = h.getStyle();
        s.set("color", BRAND_BLUE);
        s.set("font-weight", "800");
        s.set("font-size", "0.82em");
        s.set("text-transform", "uppercase");
        s.set("letter-spacing", "1px");
        s.set("margin-bottom", "5px");
        return h;
    }

    private void updateStrength(String p) {
        if (p == null || p.length() < 8) {
            passwordStrength.setText("Force : Faible");
            passwordStrength.getStyle().set("color", "#ef4444");
        } else {
            passwordStrength.setText("Force : Optimale");
            passwordStrength.getStyle().set("color", "#10b981");
        }
    }

    private void handleRegister() {
        if (nomField.isEmpty() || prenomField.isEmpty() || emailField.isEmpty() || passwordField.isEmpty()) {
            Notification n = Notification.show("Veuillez remplir les champs obligatoires", 3000, Notification.Position.TOP_CENTER);
            n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }
        try {
            userService.registerUser(nomField.getValue(), prenomField.getValue(), emailField.getValue(), passwordField.getValue(), roleComboBox.getValue());
            Notification.show("Inscription réussie !", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            UI.getCurrent().navigate("login");
        } catch (Exception e) {
            Notification.show(e.getMessage()).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}