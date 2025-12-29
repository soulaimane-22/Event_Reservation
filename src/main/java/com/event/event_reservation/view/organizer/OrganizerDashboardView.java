package com.event.event_reservation.view.organizer;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
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

@Route(value = "organizer/dashboard", layout = VaadinAppLayout.class)
@PageTitle("Dashboard Organisateur - Event Reservation")
public class OrganizerDashboardView extends VerticalLayout implements BeforeEnterObserver {

    private final EventService eventService;
    private final EventRepository eventRepository;
    private final String BRAND_BLUE = "#253366";
    private final String ICON_PATH = "images/events/icons/";

    private User currentUser;
    private final VerticalLayout container = new VerticalLayout();

    @Autowired
    public OrganizerDashboardView(EventService eventService, EventRepository eventRepository) {
        this.eventService = eventService;
        this.eventRepository = eventRepository;

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().set("background-color", "#f8f9fa");

        container.setWidthFull();
        container.setMaxWidth("1250px");
        container.getStyle().set("margin", "0 auto");
        container.setPadding(true);
        add(container);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        this.currentUser = VaadinSession.getCurrentUser();
        if (currentUser == null || (currentUser.getRole() != UserRole.ORGANIZER && currentUser.getRole() != UserRole.ADMIN)) {
            event.forwardTo("login");
            return;
        }
        container.removeAll();
        createHeader();
        createStatisticsCards();
        createRecentEventsTable();
    }

    private void createHeader() {
        HorizontalLayout headerRow = new HorizontalLayout();
        headerRow.setWidthFull();
        headerRow.setAlignItems(Alignment.CENTER);
        headerRow.getStyle().set("margin", "30px 0");

        Image dashIcon = new Image(ICON_PATH + "dashboard.svg", "");
        dashIcon.setWidth("50px");

        VerticalLayout titles = new VerticalLayout();
        titles.setPadding(false); titles.setSpacing(false);

        H1 title = new H1("Tableau de bord");
        title.getStyle().set("color", BRAND_BLUE).set("font-weight", "800").set("margin", "0").set("font-size", "2.5em");

        Span welcome = new Span("Gestion de votre activité organisateur");
        welcome.getStyle().set("color", "#666").set("font-size", "1.1em");

        titles.add(title, welcome);
        headerRow.add(dashIcon, titles);
        container.add(headerRow);
    }

    private void createStatisticsCards() {
        EventOrganizerStatisticsDTO stats = eventService.getOrganizerStatistics(currentUser.getId());

        Div grid = new Div();
        grid.setWidthFull();
        grid.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(auto-fit, minmax(240px, 1fr))")
                .set("gap", "20px")
                .set("margin-bottom", "40px");

        grid.add(
                createStatCard("statistics.svg", "Total Événements", String.valueOf(stats.getTotalEvents())),
                createStatCard("statistics.svg", "Événements Publiés", String.valueOf(stats.getPublishedEvents())),
                createStatCard("statistics.svg", "Réservations Totales", String.valueOf(stats.getTotalReservations())),
                createStatCard("statistics.svg", "Chiffre d'Affaires", stats.getTotalRevenue() + " MAD")
        );

        container.add(grid);
    }

    /**
     * CARTE MINIMISÉE : Fond Blanc, Border-Top Bleu, Contenu Bleu
     */
    private VerticalLayout createStatCard(String iconName, String label, String value) {
        VerticalLayout card = new VerticalLayout();
        card.setAlignItems(Alignment.CENTER);
        card.setJustifyContentMode(JustifyContentMode.CENTER);
        card.setWidth("260px"); // Taille minimisée

        card.getStyle()
                .set("background-color", "white")
                .set("border-radius", "15px")
                .set("border-top", "6px solid " + BRAND_BLUE) // Bordure demandée
                .set("padding", "25px 20px")
                .set("box-shadow", "0 8px 25px rgba(0,0,0,0.04)")
                .set("transition", "transform 0.3s ease");

        Image icon = new Image(ICON_PATH + iconName, "");
        icon.setHeight("28px");
        // Pas d'invert ici car on veut la couleur d'origine ou une couleur sombre

        Span val = new Span(value);
        val.getStyle()
                .set("font-size", "1.8em")
                .set("font-weight", "800")
                .set("color", BRAND_BLUE)
                .set("margin-top", "10px");

        Span lbl = new Span(label);
        lbl.getStyle()
                .set("color", BRAND_BLUE)
                .set("opacity", "0.7")
                .set("text-transform", "uppercase")
                .set("font-size", "0.75em")
                .set("font-weight", "700")
                .set("letter-spacing", "0.5px");

        card.add(icon, val, lbl);

        card.getElement().executeJs("this.onmouseover = () => { this.style.transform = 'translateY(-5px)'; };" +
                "this.onmouseout = () => { this.style.transform = 'translateY(0)'; };");

        return card;
    }

    private void createRecentEventsTable() {
        VerticalLayout section = new VerticalLayout();
        section.getStyle()
                .set("background-color", "white")
                .set("border-radius", "20px")
                .set("box-shadow", "0 10px 40px rgba(0,0,0,0.03)")
                .set("padding", "35px");

        H2 title = new H2("Événements récents");
        title.getStyle().set("color", BRAND_BLUE).set("font-weight", "800").set("margin-bottom", "25px");

        List<Event> recentEvents = eventRepository.findByOrganisateurId(currentUser.getId())
                .stream()
                .limit(5)
                .toList();

        if (recentEvents.isEmpty()) {
            section.add(new Span("Aucun événement à afficher."));
        } else {
            Grid<Event> grid = new Grid<>(Event.class, false);
            grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);

            grid.addColumn(Event::getTitre).setHeader("ÉVÉNEMENT").setSortable(true).setFlexGrow(1);
            grid.addColumn(e -> e.getDateDebut().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).setHeader("DATE").setAutoWidth(true);
            grid.addComponentColumn(this::createStatusBadge).setHeader("STATUT").setAutoWidth(true);
            grid.addComponentColumn(this::createPlacesIndicator).setHeader("REMPLISSAGE").setAutoWidth(true);

            grid.addComponentColumn(event -> {
                Button viewBtn = new Button("Gérer");
                viewBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
                viewBtn.getStyle().set("color", BRAND_BLUE).set("font-weight", "700").set("cursor", "pointer");
                viewBtn.addClickListener(e -> NavigationManager.goToMyEvents());
                return viewBtn;
            }).setHeader("ACTION").setAutoWidth(true);

            grid.setItems(recentEvents);
            grid.setAllRowsVisible(true);
            section.add(grid);
        }
        container.add(title, section);
    }

    private Span createStatusBadge(Event event) {
        Span badge = new Span(event.getStatut().toString());
        var s = badge.getStyle();
        s.set("padding", "4px 10px").set("border-radius", "15px").set("font-size", "0.7em").set("font-weight", "bold").set("color", "white");

        String color = switch (event.getStatut()) {
            case PUBLIE -> "#10b981";
            case BROUILLON -> "#6b7280";
            case ANNULE -> "#ef4444";
            case TERMINE -> "#3b82f6";
        };
        s.set("background-color", color);
        return badge;
    }

    private Span createPlacesIndicator(Event event) {
        int reserved = event.getCapaciteMax() - event.getCapaciteRestante();
        double rate = (double) reserved / event.getCapaciteMax();
        Span span = new Span(reserved + " / " + event.getCapaciteMax());
        span.getStyle().set("font-weight", "600").set("font-size", "0.85em");
        if (rate >= 0.8) span.getStyle().set("color", "#ef4444");
        else if (rate >= 0.5) span.getStyle().set("color", "#f59e0b");
        else span.getStyle().set("color", "#10b981");
        return span;
    }
}