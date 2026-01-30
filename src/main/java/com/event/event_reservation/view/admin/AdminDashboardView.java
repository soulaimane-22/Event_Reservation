package com.event.event_reservation.view.admin;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.event.event_reservation.config.VaadinSession;
import com.event.event_reservation.entity.Reservation;
import com.event.event_reservation.entity.User;
import com.event.event_reservation.entity.enums.EventCategory;
import com.event.event_reservation.entity.enums.EventStatus;
import com.event.event_reservation.entity.enums.ReservationStatus;
import com.event.event_reservation.entity.enums.UserRole;
import com.event.event_reservation.repository.EventRepository;
import com.event.event_reservation.repository.ReservationRepository;
import com.event.event_reservation.repository.UserRepository;
import com.event.event_reservation.view.components.VaadinAppLayout;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.format.TextStyle;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Route(value = "admin/dashboard", layout = VaadinAppLayout.class)
@PageTitle("Tableau de bord Admin - Event Reservation")
public class AdminDashboardView extends VerticalLayout implements BeforeEnterObserver {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final ReservationRepository reservationRepository;

    private final String BRAND_BLUE = "#253366";
    private final String ICON_PATH = "images/events/icons/";
    private final VerticalLayout container = new VerticalLayout();

    @Autowired
    public AdminDashboardView(UserRepository userRepository,
                              EventRepository eventRepository,
                              ReservationRepository reservationRepository) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.reservationRepository = reservationRepository;

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().set("background-color", "#f8f9fa");

        container.setWidthFull();
        container.setMaxWidth("1300px");
        container.getStyle().set("margin", "0 auto");
        container.setPadding(true);
        add(container);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        User currentUser = VaadinSession.getCurrentUser();
        if (currentUser == null || currentUser.getRole() != UserRole.ADMIN) {
            event.forwardTo("login");
            return;
        }

        container.removeAll();
        createHeader();
        createMainStatsGrid();
        createAnalyticsSection(); // Nouvelle section visuelle
        createDetailedBreakdownSection();
    }

    private void createHeader() {
        HorizontalLayout headerRow = new HorizontalLayout();
        headerRow.setWidthFull();
        headerRow.setAlignItems(Alignment.CENTER);
        headerRow.getStyle().set("margin", "40px 0");

        Image adminIcon = new Image(ICON_PATH + "dashboard.svg", "");
        adminIcon.setWidth("60px");

        VerticalLayout titles = new VerticalLayout();
        titles.setPadding(false); titles.setSpacing(false);

        H1 title = new H1("Tableau de bord Administrateur");
        title.getStyle().set("color", BRAND_BLUE).set("font-weight", "800").set("margin", "0").set("font-size", "2.5em");

        Span subtitle = new Span("Analyses temps réel de la plateforme");
        subtitle.getStyle().set("color", "#666").set("font-size", "1.1em");

        titles.add(title, subtitle);
        headerRow.add(adminIcon, titles);
        container.add(headerRow);
    }

    private void createMainStatsGrid() {
        BigDecimal totalRevenue = reservationRepository.findAll().stream()
                .filter(r -> r.getStatut() == ReservationStatus.CONFIRMEE)
                .map(r -> r.getMontantTotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Div grid = new Div();
        grid.setWidthFull();
        grid.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(auto-fit, minmax(240px, 1fr))")
                .set("gap", "20px")
                .set("margin-bottom", "40px");

        grid.add(
                createMainStatCard("people.svg", "Clients", String.valueOf(userRepository.countByRole(UserRole.CLIENT))),
                createMainStatCard("event.svg", "Événements", String.valueOf(eventRepository.count())),
                createMainStatCard("ticket.svg", "Réservations", String.valueOf(reservationRepository.count())),
                createMainStatCard("revenue.svg", "Revenus Totaux", String.format("%.0f MAD", totalRevenue))
        );

        container.add(grid);
    }

    /**
     * NOUVELLE SECTION ANALYTIQUE (Graphiques 100% Java)
     */
    private void createAnalyticsSection() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.setSpacing(true);
        layout.getStyle().set("margin-bottom", "40px");

        // 1. REVENUS PAR CATÉGORIE (Barres Horizontales)
        VerticalLayout catCard = createAnalyticsCard("Répartition des Revenus par Catégorie");

        List<Reservation> confirmedRes = reservationRepository.findAll().stream()
                .filter(r -> r.getStatut() == ReservationStatus.CONFIRMEE).toList();

        Map<EventCategory, Double> revenueByCat = confirmedRes.stream()
                .collect(Collectors.groupingBy(r -> r.getEvenement().getCategorie(),
                        Collectors.summingDouble(r -> r.getMontantTotal().doubleValue())));

        double maxRev = revenueByCat.values().stream().max(Double::compare).orElse(1.0);

        for (EventCategory cat : EventCategory.values()) {
            double val = revenueByCat.getOrDefault(cat, 0.0);
            catCard.add(createHorizontalBar(cat.name(), val, maxRev));
        }

        // 2. VOLUME DE RÉSERVATIONS MENSUEL (Barres Verticales)
        VerticalLayout timeCard = createAnalyticsCard("Activité des 6 derniers mois");
        HorizontalLayout chartArea = new HorizontalLayout();
        chartArea.setWidthFull();
        chartArea.setHeight("250px");
        chartArea.setAlignItems(Alignment.END);
        chartArea.setJustifyContentMode(FlexComponent.JustifyContentMode.AROUND);

        Map<String, Long> resByMonth = confirmedRes.stream()
                .collect(Collectors.groupingBy(r -> r.getDateReservation().getMonth().getDisplayName(TextStyle.SHORT, Locale.FRENCH),
                        Collectors.counting()));

        long maxCount = resByMonth.values().stream().max(Long::compare).orElse(1L);

        // On affiche les 6 derniers mois
        for (int i = 5; i >= 0; i--) {
            String month = java.time.LocalDate.now().minusMonths(i).getMonth().getDisplayName(TextStyle.SHORT, Locale.FRENCH);
            long count = resByMonth.getOrDefault(month, 0L);
            chartArea.add(createVerticalBar(month, count, maxCount));
        }
        timeCard.add(chartArea);

        layout.add(catCard, timeCard);
        container.add(layout);
    }

    private VerticalLayout createHorizontalBar(String label, double value, double maxValue) {
        VerticalLayout barLayout = new VerticalLayout();
        barLayout.setPadding(false); barLayout.setSpacing(false);
        barLayout.setWidthFull();

        HorizontalLayout labels = new HorizontalLayout(new Span(label), new Span(String.format("%.0f MAD", value)));
        labels.setWidthFull();
        labels.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        labels.getStyle().set("font-size", "0.85em").set("font-weight", "600").set("color", BRAND_BLUE);

        Div track = new Div();
        track.setWidthFull(); track.setHeight("12px");
        track.getStyle().set("background-color", "#EFF1FC").set("border-radius", "6px").set("margin-top", "5px").set("transition", "transform 0.3s ease").set("box-shadow", "0px 7px 29px 0px rgba(37, 51, 102, 0.4)");

        Div fill = new Div();
        double percent = (value / maxValue) * 100;
        fill.setWidth(percent + "%"); fill.setHeightFull();
        fill.getStyle().set("background-color", BRAND_BLUE).set("border-radius", "6px");

        track.add(fill);
        barLayout.add(labels, track);
        return barLayout;
    }

    private VerticalLayout createVerticalBar(String label, long value, long maxValue) {
        VerticalLayout col = new VerticalLayout();
        col.setAlignItems(Alignment.CENTER); col.setPadding(false); col.setSpacing(false);

        Div bar = new Div();
        bar.setWidth("25px");
        double height = ((double)value / maxValue) * 150;
        bar.setHeight(height + "px");
        bar.getStyle().set("background", "linear-gradient(to top, " + BRAND_BLUE + ", #435591)").set("border-radius", "5px 5px 0 0");

        Span valLabel = new Span(String.valueOf(value));
        valLabel.getStyle().set("font-size", "0.75em").set("font-weight", "bold");

        Span monthLabel = new Span(label);
        monthLabel.getStyle().set("font-size", "0.8em").set("color", "#888");

        col.add(valLabel, bar, monthLabel);
        return col;
    }

    private VerticalLayout createAnalyticsCard(String title) {
        VerticalLayout card = new VerticalLayout();
        card.getStyle().set("background-color", "#EFF1FC").set("border-radius", "20px").set("padding", "30px").set("box-shadow", "0px 7px 29px 0px rgba(37, 51, 102, 0.4)").set("transition", "transform 0.3s ease");
        card.setWidth("50%");
        H3 h3 = new H3(title);
        h3.getStyle().set("color", BRAND_BLUE).set("font-weight", "800").set("margin-bottom", "25px").set("font-size", "1.1em");
        card.add(h3);
        return card;
    }

    private VerticalLayout createMainStatCard(String iconName, String label, String value) {
        VerticalLayout card = new VerticalLayout();
        card.setAlignItems(Alignment.CENTER);
        card.setJustifyContentMode(JustifyContentMode.CENTER);
        card.getStyle()
                .set("background-color", "#EFF1FC")
                .set("border-radius", "15px")
                .set("border-top", "6px solid " + BRAND_BLUE)
                .set("padding", "25px 15px")
                .set("box-shadow", "0px 7px 29px 0px rgba(37, 51, 102, 0.4)")
                .set("transition", "transform 0.3s ease");

        Image icon = new Image(ICON_PATH + iconName, "");
        icon.setHeight("30px");

        Span val = new Span(value);
        val.getStyle().set("font-size", "1.8em").set("font-weight", "800").set("color", BRAND_BLUE).set("margin-top", "10px");

        Span lbl = new Span(label);
        lbl.getStyle().set("color", "#888").set("text-transform", "uppercase").set("font-size", "0.75em").set("font-weight", "700").set("letter-spacing", "0.5px");

        card.add(icon, val, lbl);
        return card;
    }

    private void createDetailedBreakdownSection() {
        H2 sectionTitle = new H2("Répartition des données");
        sectionTitle.getStyle().set("color", BRAND_BLUE).set("font-weight", "800").set("margin-bottom", "30px");
        container.add(sectionTitle);

        Div grid = new Div();
        grid.setWidthFull();
        grid.getStyle().set("display", "grid").set("grid-template-columns", "repeat(auto-fit, minmax(350px, 1fr))").set("gap", "30px");

        grid.add(createBreakdownCard("Utilisateurs par Rôle",
                new String[]{"ADMIN", "ORGANISATEUR", "CLIENT"},
                new String[]{ String.valueOf(userRepository.countByRole(UserRole.ADMIN)), String.valueOf(userRepository.countByRole(UserRole.ORGANIZER)), String.valueOf(userRepository.countByRole(UserRole.CLIENT)) }));

        grid.add(createBreakdownCard("État des Événements",
                new String[]{"Publiés", "Brouillons", "Annulés"},
                new String[]{ String.valueOf(eventRepository.findByStatut(EventStatus.PUBLIE).size()), String.valueOf(eventRepository.findByStatut(EventStatus.BROUILLON).size()), String.valueOf(eventRepository.findByStatut(EventStatus.ANNULE).size()) }));

        grid.add(createBreakdownCard("Suivi des Réservations",
                new String[]{"Confirmées", "En Attente", "Annulées"},
                new String[]{ String.valueOf(reservationRepository.findByStatut(ReservationStatus.CONFIRMEE).size()), String.valueOf(reservationRepository.findByStatut(ReservationStatus.EN_ATTENTE).size()), String.valueOf(reservationRepository.findByStatut(ReservationStatus.ANNULEE).size()) }));

        container.add(grid);
    }

    private VerticalLayout createBreakdownCard(String title, String[] labels, String[] values) {
        VerticalLayout card = new VerticalLayout();
        card.getStyle().set("background-color", "#EFF1FC").set("border-radius", "20px").set("padding", "30px").set("box-shadow", "0px 7px 29px 0px rgba(37, 51, 102, 0.4)").set("transition", "transform 0.3s ease");
        H3 h3 = new H3(title);
        h3.getStyle().set("color", BRAND_BLUE).set("font-weight", "800").set("margin-bottom", "20px");
        card.add(h3);

        for (int i = 0; i < labels.length; i++) {
            HorizontalLayout row = new HorizontalLayout();
            row.setWidthFull();
            row.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
            row.getStyle().set("padding", "10px 0").set("border-bottom", "1px solid #f0f0f0");
            Span val = new Span(values[i]);
            val.getStyle().set("background-color", "#f1f3f9").set("color", BRAND_BLUE).set("padding", "3px 12px").set("border-radius", "8px").set("font-weight", "800");
            row.add(new Span(labels[i]), val);
            card.add(row);
        }
        return card;
    }
}