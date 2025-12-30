package com.event.event_reservation.view.organizer;

import com.event.event_reservation.config.NavigationManager;
import com.event.event_reservation.config.VaadinSession;
import com.event.event_reservation.entity.Event;
import com.event.event_reservation.entity.User;
import com.event.event_reservation.entity.enums.EventCategory;
import com.event.event_reservation.entity.enums.EventStatus;
import com.event.event_reservation.entity.enums.UserRole;
import com.event.event_reservation.repository.EventRepository;
import com.event.event_reservation.service.EventService;
import com.event.event_reservation.view.components.VaadinAppLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Optional;

@Route(value = "organizer/event/new", layout = VaadinAppLayout.class)
@RouteAlias(value = "organizer/event/edit", layout = VaadinAppLayout.class)
@PageTitle("Gestion Événement - Organizer")
public class EventFormView extends VerticalLayout implements HasUrlParameter<Long> {

    private final EventService eventService;
    private final EventRepository eventRepository;
    private final User currentUser;

    private final String BRAND_BLUE = "#253366";

    // Composants du formulaire
    private Long eventId;
    private final TextField titre = new TextField("Titre de l'événement");
    private final ComboBox<EventCategory> categorie = new ComboBox<>("Catégorie");
    private final DateTimePicker dateDebut = new DateTimePicker("Date de début");
    private final DateTimePicker dateFin = new DateTimePicker("Date de fin");
    private final TextField ville = new TextField("Ville");
    private final TextField lieu = new TextField("Lieu (Adresse)");
    private final NumberField latField = new NumberField("Latitude");
    private final NumberField lngField = new NumberField("Longitude");
    private final NumberField capacite = new NumberField("Capacité max");
    private final NumberField prix = new NumberField("Prix (MAD)");
    private final TextArea description = new TextArea("Description détaillée");

    private final VerticalLayout container = new VerticalLayout();

    @Autowired
    public EventFormView(EventService eventService, EventRepository eventRepository) {
        this.eventService = eventService;
        this.eventRepository = eventRepository;
        this.currentUser = VaadinSession.getCurrentUser();

        if (currentUser == null || (currentUser.getRole() != UserRole.ORGANIZER && currentUser.getRole() != UserRole.ADMIN)) {
            UI.getCurrent().navigate("login");
            return;
        }

        // INITIALISATION DES DONNÉES DU MENU
        categorie.setItems(EventCategory.values());

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().set("background-color", "#f8f9fa");

        container.setWidthFull();
        container.setMaxWidth("1100px");
        container.getStyle().set("margin", "0 auto");
        container.setPadding(true);
        add(container);
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter Long parameter) {
        this.eventId = parameter;
        buildUI();
        if (eventId != null) {
            loadEventData();
        }
    }

    private void buildUI() {
        container.removeAll();

        // 1. HEADER
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.getStyle().set("margin", "30px 0");

        H1 titleText = new H1(eventId == null ? "Créer un événement" : "Modifier l'événement");
        titleText.getStyle().set("color", BRAND_BLUE).set("font-weight", "800").set("margin", "0");

        Button cancelBtn = new Button("Retour", e -> NavigationManager.goToMyEvents());
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        header.add(titleText, cancelBtn);

        // 2. FORM CARD
        VerticalLayout card = new VerticalLayout();
        card.getStyle()
                .set("background-color", "white")
                .set("border-radius", "25px")
                .set("padding", "40px")
                .set("box-shadow", "0 15px 50px rgba(0,0,0,0.05)");

        FormLayout formLayout = new FormLayout();
        formLayout.setWidthFull();
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("600px", 2));

        // Configuration
        titre.setRequired(true);
        prix.setSuffixComponent(new Span("MAD"));

        // Ajout des champs dans la grille
        formLayout.add(titre, categorie, dateDebut, dateFin, ville, lieu, latField, lngField, capacite, prix);
        formLayout.setColspan(titre, 2);

        // DESCRIPTION EN PLEINE LARGEUR (Hors du FormLayout pour l'alignement)
        description.setWidthFull();
        description.setMinHeight("200px");
        description.getStyle().set("margin-top", "20px");

        card.add(formLayout, new Hr(), description);

        // 3. ACTIONS
        HorizontalLayout actions = new HorizontalLayout();
        actions.setWidthFull();
        actions.setJustifyContentMode(JustifyContentMode.END);
        actions.setSpacing(true);
        actions.getStyle().set("margin-top", "30px");

        Button draftBtn = new Button("Sauvegarder Brouillon", e -> handleSave(EventStatus.BROUILLON));
        draftBtn.getStyle().set("height", "55px").set("padding", "0 30px");

        Button publishBtn = new Button("Publier l'événement", e -> handleSave(EventStatus.PUBLIE));
        publishBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        publishBtn.getStyle().set("background-color", BRAND_BLUE).set("height", "55px").set("padding", "0 40px").set("font-weight", "800");

        actions.add(draftBtn, publishBtn);

        container.add(header, card, actions);
    }

    private void loadEventData() {
        eventRepository.findById(eventId).ifPresent(e -> {
            titre.setValue(e.getTitre());
            categorie.setValue(e.getCategorie());
            dateDebut.setValue(e.getDateDebut());
            dateFin.setValue(e.getDateFin());
            ville.setValue(e.getVille());
            lieu.setValue(e.getLieu());
            capacite.setValue(e.getCapaciteMax().doubleValue());
            prix.setValue(e.getPrixUnitaire().doubleValue());
            description.setValue(e.getDescription());
            latField.setValue(e.getLatitude());
            lngField.setValue(e.getLongitude());
        });
    }

    private void handleSave(EventStatus status) {
        if (titre.isEmpty() || categorie.isEmpty() || dateDebut.isEmpty() || prix.isEmpty()) {
            Notification.show("Veuillez remplir les champs obligatoires", 3000, Notification.Position.TOP_CENTER).addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            Event event;
            if (eventId == null) {
                // CREATION
                event = eventService.createEvent(
                        currentUser.getId(), titre.getValue(), description.getValue(),
                        categorie.getValue(), dateDebut.getValue(), dateFin.getValue(),
                        lieu.getValue(), ville.getValue(), capacite.getValue().intValue(),
                        BigDecimal.valueOf(prix.getValue()),
                        latField.getValue(), lngField.getValue()
                );
            } else {
                // UPDATE
                event = eventService.updateEvent(
                        currentUser.getId(), eventId, titre.getValue(), description.getValue(),
                        categorie.getValue(), dateDebut.getValue(), dateFin.getValue(),
                        lieu.getValue(), ville.getValue(), capacite.getValue().intValue(),
                        BigDecimal.valueOf(prix.getValue())
                );
                // Mise à jour manuelle des champs non présents dans updateEvent du service
                event.setLatitude(latField.getValue());
                event.setLongitude(lngField.getValue());
            }

            // Gestion du statut (Brouillon vs Publié)
            if (status == EventStatus.PUBLIE) {
                eventService.publishEvent(currentUser.getId(), event.getId());
            }

            Notification.show("Événement enregistré avec succès").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            NavigationManager.goToMyEvents();

        } catch (Exception e) {
            Notification.show("Erreur : " + e.getMessage()).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}