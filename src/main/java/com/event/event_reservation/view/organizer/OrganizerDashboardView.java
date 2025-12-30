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
import com.event.event_reservation.entity.Reservation;
import com.event.event_reservation.entity.User;
import com.event.event_reservation.entity.enums.EventCategory;
import com.event.event_reservation.entity.enums.EventStatus;
import com.event.event_reservation.entity.enums.ReservationStatus;
import com.event.event_reservation.entity.enums.UserRole;
import com.event.event_reservation.repository.EventRepository;
import com.event.event_reservation.repository.ReservationRepository;
import com.event.event_reservation.service.EventService;
import com.event.event_reservation.view.components.VaadinAppLayout;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Route(value = "organizer/dashboard", layout = VaadinAppLayout.class)
@PageTitle("Dashboard Organisateur - Event Reservation")
public class OrganizerDashboardView extends VerticalLayout implements BeforeEnterObserver {

    private final EventService eventService;
    private final EventRepository eventRepository;
    private final ReservationRepository reservationRepository;

    private final String BRAND_BLUE = "#253366";
    private final String ICON_PATH = "images/events/icons/";

    private User currentUser;
    private final VerticalLayout container = new VerticalLayout();

    @Autowired
    public OrganizerDashboardView(EventService eventService, EventRepository eventRepository, ReservationRepository reservationRepository) {
        this.eventService = eventService;
        this.eventRepository = eventRepository;
        this.reservationRepository = reservationRepository;

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
        createAnalyticsSection(); // Ajout des visualisations
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

        Span welcome = new Span("Vue d'ensemble de vos performances");
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
                createStatCard("event.svg", "Total Événements", String.valueOf(stats.getTotalEvents())),
                createStatCard("confirme.svg", "Événements Publiés", String.valueOf(stats.getPublishedEvents())),
                createStatCard("ticket.svg", "Réservations", String.valueOf(stats.getTotalReservations())),
                createStatCard("revenue.svg", "Chiffre d'Affaires", stats.getTotalRevenue() + " MAD")
        );

        container.add(grid);
    }

    private VerticalLayout createStatCard(String iconName, String label, String value) {
        VerticalLayout card = new VerticalLayout();
        card.setAlignItems(Alignment.CENTER);
        card.setJustifyContentMode(JustifyContentMode.CENTER);
        card.setWidth("260px");

        card.getStyle()
                .set("background-color", "white")
                .set("border-radius", "15px")
                .set("border-top", "6px solid " + BRAND_BLUE)
                .set("padding", "25px 20px")
                .set("box-shadow", "0 8px 25px rgba(0,0,0,0.04)")
                .set("transition", "transform 0.3s ease");

        Image icon = new Image(ICON_PATH + iconName, "");
        icon.setHeight("28px");

        Span val = new Span(value);
        val.getStyle().set("font-size", "1.8em").set("font-weight", "800").set("color", BRAND_BLUE).set("margin-top", "10px");

        Span lbl = new Span(label);
        lbl.getStyle().set("color", BRAND_BLUE).set("opacity", "0.7").set("text-transform", "uppercase").set("font-size", "0.75em").set("font-weight", "700");

        card.add(icon, val, lbl);
        return card;
    }

    /**
     * NOUVELLE SECTION ANALYTIQUE : 2 Visualisations réelles
     */
    private void createAnalyticsSection() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.setSpacing(true);
        layout.getStyle().set("margin-bottom", "40px");

        // Récupération des données réelles des réservations pour cet organisateur
        List<Reservation> organizerReservations = reservationRepository.findAll().stream()
                .filter(r -> r.getEvenement().getOrganisateur().getId().equals(currentUser.getId()))
                .filter(r -> r.getStatut() == ReservationStatus.CONFIRMEE)
                .collect(Collectors.toList());

        // 1. REVENUS PAR CATÉGORIE
        VerticalLayout categoryChart = createChartCard("Revenus par Catégorie");
        Map<EventCategory, Double> revenueByCat = organizerReservations.stream()
                .collect(Collectors.groupingBy(r -> r.getEvenement().getCategorie(),
                        Collectors.summingDouble(r -> r.getMontantTotal().doubleValue())));

        double maxRevenue = revenueByCat.values().stream().max(Double::compare).orElse(1.0);

        for (EventCategory cat : EventCategory.values()) {
            double val = revenueByCat.getOrDefault(cat, 0.0);
            categoryChart.add(createHorizontalBar(cat.name(), val, maxRevenue));
        }

        // 2. RÉSERVATIONS MENSUELLES (6 derniers mois)
        VerticalLayout monthlyChart = createChartCard("Réservations (6 derniers mois)");
        HorizontalLayout barsArea = new HorizontalLayout();
        barsArea.setWidthFull();
        barsArea.setHeight("200px");
        barsArea.setAlignItems(Alignment.END);
        barsArea.setJustifyContentMode(FlexComponent.JustifyContentMode.AROUND);

        Map<String, Long> resByMonth = organizerReservations.stream()
                .collect(Collectors.groupingBy(r -> r.getDateReservation().getMonth().getDisplayName(TextStyle.SHORT, Locale.FRENCH),
                        Collectors.counting()));

        long maxCount = resByMonth.values().stream().max(Long::compare).orElse(1L);

        for (int i = 5; i >= 0; i--) {
            String month = LocalDate.now().minusMonths(i).getMonth().getDisplayName(TextStyle.SHORT, Locale.FRENCH);
            long count = resByMonth.getOrDefault(month, 0L);
            barsArea.add(createVerticalBar(month, count, maxCount));
        }
        monthlyChart.add(barsArea);

        layout.add(categoryChart, monthlyChart);
        container.add(layout);
    }

    private VerticalLayout createChartCard(String title) {
        VerticalLayout card = new VerticalLayout();
        card.getStyle().set("background-color", "white").set("border-radius", "20px").set("padding", "30px").set("box-shadow", "0 8px 25px rgba(0,0,0,0.03)");
        card.setWidth("50%");
        H3 h3 = new H3(title);
        h3.getStyle().set("color", BRAND_BLUE).set("font-weight", "800").set("margin-bottom", "20px").set("font-size", "1.1em");
        card.add(h3);
        return card;
    }

    private VerticalLayout createHorizontalBar(String label, double value, double maxValue) {
        VerticalLayout barLayout = new VerticalLayout();
        barLayout.setPadding(false); barLayout.setSpacing(false); barLayout.setWidthFull();

        HorizontalLayout labels = new HorizontalLayout(new Span(label), new Span(String.format("%.0f MAD", value)));
        labels.setWidthFull(); labels.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        labels.getStyle().set("font-size", "0.8em").set("font-weight", "600").set("color", BRAND_BLUE);

        Div track = new Div(); track.setWidthFull(); track.setHeight("8px");
        track.getStyle().set("background-color", "#f1f3f9").set("border-radius", "4px").set("margin", "5px 0 15px 0");

        Div fill = new Div();
        double percent = (value / maxValue) * 100;
        fill.setWidth(percent + "%"); fill.setHeightFull();
        fill.getStyle().set("background-color", BRAND_BLUE).set("border-radius", "4px");

        track.add(fill);
        barLayout.add(labels, track);
        return barLayout;
    }

    private VerticalLayout createVerticalBar(String label, long value, long maxValue) {
        VerticalLayout col = new VerticalLayout();
        col.setAlignItems(Alignment.CENTER); col.setPadding(false); col.setSpacing(false);

        Div bar = new Div(); bar.setWidth("20px");
        double height = ((double)value / maxValue) * 140;
        bar.setHeight(height + "px");
        bar.getStyle().set("background", BRAND_BLUE).set("border-radius", "4px 4px 0 0");

        Span valLabel = new Span(String.valueOf(value));
        valLabel.getStyle().set("font-size", "0.7em").set("font-weight", "bold").set("color", BRAND_BLUE);

        Span monthLabel = new Span(label);
        monthLabel.getStyle().set("font-size", "0.75em").set("color", "#888").set("margin-top", "5px");

        col.add(valLabel, bar, monthLabel);
        return col;
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
                viewBtn.getStyle().set("color", BRAND_BLUE).set("font-weight", "700");
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