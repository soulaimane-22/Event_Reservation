package com.event.event_reservation.view.organizer;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.event.event_reservation.config.NavigationManager;
import com.event.event_reservation.config.VaadinSession;
import com.event.event_reservation.dto.EventOrganizerStatisticsDTO;
import com.event.event_reservation.entity.Event;
import com.event.event_reservation.entity.User;
import com.event.event_reservation.entity.enums.UserRole;
import com.event.event_reservation.repository.EventRepository;
import com.event.event_reservation.service.EventService;
import com.event.event_reservation.view.components.VaadinAppLayout;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Dashboard ORGANISATEUR
 * URL: /organizer/dashboard
 */
@Route(value = "organizer/dashboard", layout = VaadinAppLayout.class)
@PageTitle("Dashboard Organisateur - Event Reservation")
public class OrganizerDashboardView extends VerticalLayout {

    private final EventService eventService;
    private final EventRepository eventRepository;

    private User currentUser;

    @Autowired
    public OrganizerDashboardView(EventService eventService, EventRepository eventRepository) {
        this.eventService = eventService;
        this.eventRepository = eventRepository;

        currentUser = VaadinSession.getCurrentUser();
        if (currentUser == null ||
                (currentUser.getRole() != UserRole.ORGANIZER && currentUser.getRole() != UserRole.ADMIN)) {
            NavigationManager.goToLogin();
            return;
        }

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        createHeader();
        createStatisticsCards();
        createRecentEvents();
        createQuickActions();
    }

    /**
     * Créer le header
     */
    private void createHeader() {
        H1 title = new H1("🎭 Dashboard Organisateur");
        title.getStyle().set("margin-bottom", "0");

        Span subtitle = new Span("Bienvenue, " + currentUser.getPrenom() + " " + currentUser.getNom());
        subtitle.getStyle().set("color", "var(--lumo-secondary-text-color)");

        add(title, subtitle);
    }

    /**
     * Créer les cartes de statistiques
     */
    private void createStatisticsCards() {
        EventOrganizerStatisticsDTO stats = eventService.getOrganizerStatistics(currentUser.getId());

        HorizontalLayout cards = new HorizontalLayout();
        cards.setWidthFull();
        cards.setSpacing(true);
        cards.getStyle().set("flex-wrap", "wrap");

        // Carte 1: Total événements
        Div totalEventsCard = createStatCard(
                VaadinIcon.CALENDAR,
                "Total Événements",
                String.valueOf(stats.getTotalEvents()),
                "#3b82f6"
        );

        // Carte 2: Événements publiés
        Div publishedCard = createStatCard(
                VaadinIcon.CHECK_CIRCLE,
                "Événements Publiés",
                String.valueOf(stats.getPublishedEvents()),
                "#10b981"
        );

        // Carte 3: Total réservations
        Div reservationsCard = createStatCard(
                VaadinIcon.TICKET,
                "Total Réservations",
                String.valueOf(stats.getTotalReservations()),
                "#8b5cf6"
        );

        // Carte 4: Revenu total
        Div revenueCard = createStatCard(
                VaadinIcon.DOLLAR,
                "Revenu Total",
                stats.getTotalRevenue() + " DH",
                "#f59e0b"
        );

        cards.add(totalEventsCard, publishedCard, reservationsCard, revenueCard);
        add(cards);
    }

    /**
     * Créer une carte de statistique
     */
    private Div createStatCard(VaadinIcon icon, String label, String value, String color) {
        Div card = new Div();
        card.setWidth("280px");
        card.getStyle()
                .set("background", "white")
                .set("border-radius", "10px")
                .set("padding", "1.5em")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.1)")
                .set("border-left", "4px solid " + color);

        // Icône
        icon.create().setSize("32px");
        icon.create().setColor(color);

        // Label
        Span labelSpan = new Span(label);
        labelSpan.getStyle()
                .set("color", "#666")
                .set("font-size", "0.9em")
                .set("display", "block")
                .set("margin-top", "0.5em");

        // Valeur
        Span valueSpan = new Span(value);
        valueSpan.getStyle()
                .set("color", "#333")
                .set("font-size", "2em")
                .set("font-weight", "bold")
                .set("display", "block");

        VerticalLayout content = new VerticalLayout(icon.create(), labelSpan, valueSpan);
        content.setPadding(false);
        content.setSpacing(false);

        card.add(content);
        return card;
    }

    /**
     * Créer la section événements récents
     */
    private void createRecentEvents() {
        H2 sectionTitle = new H2("📋 Mes Événements Récents");
        sectionTitle.getStyle().set("margin-top", "2em");

        List<Event> recentEvents = eventRepository.findByOrganisateurId(currentUser.getId())
                .stream()
                .limit(5)
                .toList();

        if (recentEvents.isEmpty()) {
            Span emptyMessage = new Span("Vous n'avez pas encore créé d'événements");
            emptyMessage.getStyle().set("color", "var(--lumo-secondary-text-color)");
            add(sectionTitle, emptyMessage);
            return;
        }

        Grid<Event> grid = new Grid<>(Event.class, false);
        grid.setHeight("300px");

        // Colonne Titre
        grid.addColumn(Event::getTitre)
                .setHeader("Événement")
                .setAutoWidth(true)
                .setFlexGrow(1);

        // Colonne Date
        grid.addColumn(e -> e.getDateDebut().format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy")
        )).setHeader("Date").setAutoWidth(true);

        // Colonne Statut
        grid.addComponentColumn(this::createStatusBadge)
                .setHeader("Statut")
                .setAutoWidth(true);

        // Colonne Places
        grid.addComponentColumn(this::createPlacesIndicator)
                .setHeader("Places")
                .setAutoWidth(true);

        // Colonne Actions
        grid.addComponentColumn(event -> {
            Button viewBtn = new Button("Voir", VaadinIcon.EYE.create());
            viewBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
            viewBtn.addClickListener(e -> NavigationManager.goToEventDetail(event.getId()));
            return viewBtn;
        }).setHeader("Action").setAutoWidth(true);

        grid.setItems(recentEvents);

        add(sectionTitle, grid);
    }

    /**
     * Créer le badge de statut
     */
    private Span createStatusBadge(Event event) {
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
                .set("padding", "4px 8px")
                .set("border-radius", "4px")
                .set("font-size", "0.85em");

        return badge;
    }

    /**
     * Créer l'indicateur de places
     */
    private Span createPlacesIndicator(Event event) {
        int reserved = event.getCapaciteMax() - event.getCapaciteRestante();
        Span places = new Span(reserved + " / " + event.getCapaciteMax());

        double fillRate = (double) reserved / event.getCapaciteMax();

        if (fillRate >= 0.9) {
            places.getStyle().set("color", "#10b981"); // Vert (presque plein)
        } else if (fillRate >= 0.5) {
            places.getStyle().set("color", "#f59e0b"); // Orange (moitié)
        } else {
            places.getStyle().set("color", "#6b7280"); // Gris (peu rempli)
        }

        return places;
    }

    /**
     * Créer les actions rapides
     */
    private void createQuickActions() {
        H2 actionsTitle = new H2("🚀 Actions Rapides");
        actionsTitle.getStyle().set("margin-top", "2em");

        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);
        actions.getStyle().set("flex-wrap", "wrap");

        // Bouton: Créer un événement
        Button createBtn = new Button("Créer un événement", VaadinIcon.PLUS_CIRCLE.create());
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        createBtn.addClickListener(e -> NavigationManager.goToCreateEvent());

        // Bouton: Voir tous mes événements
        Button allEventsBtn = new Button("Mes événements", VaadinIcon.LIST.create());
        allEventsBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_LARGE);
        allEventsBtn.addClickListener(e -> NavigationManager.goToMyEvents());

        actions.add(createBtn, allEventsBtn);
        add(actionsTitle, actions);
    }
}
