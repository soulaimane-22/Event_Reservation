package com.event.event_reservation.view.publics;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.event.event_reservation.config.NavigationManager;
import com.event.event_reservation.config.VaadinSession;
import com.event.event_reservation.entity.Event;
import com.event.event_reservation.repository.EventRepository;
import com.event.event_reservation.view.components.VaadinAppLayout;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Page de détails d'un événement
 * URL: /event/{id}
 */
@Route(value = "event/:eventId", layout = VaadinAppLayout.class)
@PageTitle("Détails Événement - Event Reservation")
public class EventDetailView extends VerticalLayout implements HasUrlParameter<Long> {

    private final EventRepository eventRepository;

    private Event event;
    private Long eventId;

    @Autowired
    public EventDetailView(EventRepository eventRepository) {
        this.eventRepository = eventRepository;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        setMaxWidth("1000px");
        getStyle().set("margin", "0 auto");
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, @OptionalParameter Long parameter) {
        if (parameter == null) {
            showError("Événement non trouvé");
            NavigationManager.goToEventList();
            return;
        }

        this.eventId = parameter;
        loadEvent();
    }

    /**
     * Charger l'événement
     */
    private void loadEvent() {
        Optional<Event> eventOptional = eventRepository.findById(eventId);

        if (eventOptional.isEmpty()) {
            showError("Événement non trouvé");
            NavigationManager.goToEventList();
            return;
        }

        this.event = eventOptional.get();
        createContent();
    }

    /**
     * Créer le contenu de la page
     */
    private void createContent() {
        // Bouton retour
        Button backBtn = new Button("← Retour aux événements", e -> NavigationManager.goToEventList());
        backBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        add(backBtn);

        // Card principale
        VerticalLayout card = new VerticalLayout();
        card.setPadding(true);
        card.setSpacing(true);
        card.getStyle()
                .set("background", "white")
                .set("border-radius", "10px")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.1)");

        // En-tête avec badge statut
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);

        H1 title = new H1(event.getTitre());
        title.getStyle().set("margin", "0");

        Span statusBadge = createStatusBadge();
        statusBadge.getStyle().set("margin-left", "auto");

        header.add(title, statusBadge);

        // Badge catégorie
        Span categoryBadge = new Span(event.getCategorie().toString());
        categoryBadge.getElement().getThemeList().add("badge");
        categoryBadge.getStyle()
                .set("background", getCategoryColor(event.getCategorie()))
                .set("color", "white")
                .set("padding", "6px 12px")
                .set("border-radius", "6px")
                .set("display", "inline-block");

        // Informations principales
        VerticalLayout infoSection = new VerticalLayout();
        infoSection.setPadding(false);
        infoSection.setSpacing(true);

        infoSection.add(
                createInfoRow(VaadinIcon.CALENDAR, "Date de début",
                        event.getDateDebut().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm"))),
                createInfoRow(VaadinIcon.CALENDAR_CLOCK, "Date de fin",
                        event.getDateFin().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm"))),
                createInfoRow(VaadinIcon.MAP_MARKER, "Lieu", event.getLieu()),
                createInfoRow(VaadinIcon.LOCATION_ARROW, "Ville", event.getVille()),
                createInfoRow(VaadinIcon.DOLLAR, "Prix", event.getPrixUnitaire() + " DH"),
                createInfoRow(VaadinIcon.USERS, "Places disponibles",
                        event.getCapaciteRestante() + " / " + event.getCapaciteMax())
        );

        // Description
        H2 descTitle = new H2("📝 Description");
        descTitle.getStyle().set("margin-top", "2em");

        Paragraph description = new Paragraph(event.getDescription());
        description.getStyle()
                .set("color", "#666")
                .set("line-height", "1.6")
                .set("white-space", "pre-wrap");

        // Organisateur
        H3 orgTitle = new H3("👤 Organisé par");
        Paragraph organizer = new Paragraph(
                event.getOrganisateur().getPrenom() + " " + event.getOrganisateur().getNom()
        );
        organizer.getStyle().set("color", "#666");

        // Bouton de réservation
        HorizontalLayout actionSection = new HorizontalLayout();
        actionSection.setWidthFull();
        actionSection.setJustifyContentMode(JustifyContentMode.CENTER);
        actionSection.setPadding(true);

        if (event.getCapaciteRestante() > 0) {
            Button reserveBtn = new Button("Réserver des places", VaadinIcon.TICKET.create());
            reserveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
            reserveBtn.addClickListener(e -> handleReservation());
            actionSection.add(reserveBtn);
        } else {
            Span soldOut = new Span("❌ Complet");
            soldOut.getStyle()
                    .set("color", "var(--lumo-error-color)")
                    .set("font-size", "1.2em")
                    .set("font-weight", "bold");
            actionSection.add(soldOut);
        }

        // Ajouter tous les composants à la card
        card.add(
                header,
                categoryBadge,
                new Hr(),
                infoSection,
                descTitle,
                description,
                orgTitle,
                organizer,
                new Hr(),
                actionSection
        );

        add(card);
    }

    /**
     * Créer un badge de statut
     */
    private Span createStatusBadge() {
        Span badge = new Span(event.getStatut().toString());
        badge.getElement().getThemeList().add("badge");

        String color = switch (event.getStatut()) {
            case PUBLIE -> "#10b981";
            case BROUILLON -> "#6b7280";
            case ANNULE -> "#ef4444";
            case TERMINE -> "#3b82f6";
        };

        badge.getStyle()
                .set("background", color)
                .set("color", "white")
                .set("padding", "6px 12px")
                .set("border-radius", "6px");

        return badge;
    }

    /**
     * Créer une ligne d'information
     */
    private HorizontalLayout createInfoRow(VaadinIcon icon, String label, String value) {
        HorizontalLayout row = new HorizontalLayout();
        row.setSpacing(true);
        row.setAlignItems(Alignment.CENTER);

        icon.create().setColor("#667eea");
        icon.create().setSize("20px");

        Span labelSpan = new Span(label + ":");
        labelSpan.getStyle()
                .set("font-weight", "bold")
                .set("min-width", "150px")
                .set("color", "#333");

        Span valueSpan = new Span(value);
        valueSpan.getStyle().set("color", "#666");

        row.add(icon.create(), labelSpan, valueSpan);
        return row;
    }

    /**
     * Gérer la réservation
     */
    private void handleReservation() {
        if (VaadinSession.isUserLoggedIn()) {
            // Rediriger vers le formulaire de réservation
            NavigationManager.goToReservationForm(eventId);
        } else {
            // Demander de se connecter
            Notification notification = Notification.show(
                    "Vous devez être connecté pour réserver",
                    3000,
                    Notification.Position.TOP_CENTER
            );
            notification.addThemeVariants(NotificationVariant.LUMO_PRIMARY);

            // Rediriger vers login après 2 secondes
            getUI().ifPresent(ui -> ui.access(() -> {
                try {
                    Thread.sleep(2000);
                    NavigationManager.goToLogin();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }));
        }
    }

    /**
     * Obtenir la couleur selon la catégorie
     */
    private String getCategoryColor(com.event.event_reservation.entity.enums.EventCategory category) {
        return switch (category) {
            case CONCERT -> "#8b5cf6";
            case THEATRE -> "#ec4899";
            case CONFERENCE -> "#3b82f6";
            case SPORT -> "#10b981";
            case AUTRE -> "#6b7280";
        };
    }

    /**
     * Afficher un message d'erreur
     */
    private void showError(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}