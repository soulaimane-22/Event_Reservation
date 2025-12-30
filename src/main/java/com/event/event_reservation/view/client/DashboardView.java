package com.event.event_reservation.view.client;

import com.event.event_reservation.config.VaadinSession;
import com.event.event_reservation.dto.UserStatisticsDTO;
import com.event.event_reservation.entity.Reservation;
import com.event.event_reservation.entity.User;
import com.event.event_reservation.entity.enums.EventCategory;
import com.event.event_reservation.entity.enums.ReservationStatus;
import com.event.event_reservation.service.ReservationService;
import com.event.event_reservation.service.UserService;
import com.event.event_reservation.view.components.VaadinAppLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Route(value = "dashboard", layout = VaadinAppLayout.class)
@PageTitle("Tableau de bord - Event Reservation")
public class DashboardView extends VerticalLayout implements BeforeEnterObserver {

    private final UserService userService;
    private final ReservationService reservationService;
    private final String BRAND_BLUE = "#253366";
    private final String ICON_PATH = "images/events/icons/";

    private final VerticalLayout container = new VerticalLayout();

    @Autowired
    public DashboardView(UserService userService, ReservationService reservationService) {
        this.userService = userService;
        this.reservationService = reservationService;

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().set("background-color", "#f8f9fa");

        container.setWidthFull();
        container.setMaxWidth("1200px");
        container.getStyle().set("margin", "0 auto");
        container.setPadding(true);
        add(container);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        User currentUser = VaadinSession.getCurrentUser();
        if (currentUser == null) {
            event.forwardTo("login");
            return;
        }

        container.removeAll();

        // Récupération des données réelles
        List<Reservation> userReservations = reservationService.getUserReservations(currentUser.getId());
        UserStatisticsDTO stats = userService.getUserStatistics(currentUser.getId());

        createHeader(currentUser);
        createStatisticsGrid(userReservations, stats);
        createAnalyticsSection(userReservations);
    }

    private void createHeader(User user) {
        VerticalLayout header = new VerticalLayout();
        header.setPadding(false);
        header.setSpacing(false);
        header.getStyle().set("margin", "40px 0");

        H1 welcome = new H1("Bonjour, " + user.getPrenom() + " " + user.getNom() + " !");
        welcome.getStyle().set("color", BRAND_BLUE).set("font-weight", "800").set("margin", "0").set("font-size", "2.8em");

        Span subtitle = new Span("Ravi de vous revoir. Voici l'analyse de votre activité.");
        subtitle.getStyle().set("color", "#666").set("font-size", "1.2em").set("margin-top", "5px");

        header.add(welcome, subtitle);
        container.add(header);
    }

    private void createStatisticsGrid(List<Reservation> reservations, UserStatisticsDTO stats) {
        // Calcul du nombre total de places (somme de nombrePlaces dans chaque réservation confirmée)
        int totalPlaces = reservations.stream()
                .filter(r -> r.getStatut() == ReservationStatus.CONFIRMEE)
                .mapToInt(Reservation::getNombrePlaces)
                .sum();

        Div grid = new Div();
        grid.setWidthFull();
        grid.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(auto-fit, minmax(300px, 1fr))")
                .set("gap", "30px");

        // Carte 1 : Nombre de réservations
        grid.add(createStatCard("confirme.svg", "Commandes effectuées", String.valueOf(stats.getReservationsCount()), BRAND_BLUE));

        // Carte 2 : CHANGÉ -> Nombre total de places
        grid.add(createStatCard("people.svg", "Places réservées", String.valueOf(totalPlaces), BRAND_BLUE));

        // Carte 3 : Dépenses
        grid.add(createStatCard("argent.svg", "Investissement Total", stats.getTotalSpent() + " MAD", BRAND_BLUE));

        container.add(grid);
    }

    private void createAnalyticsSection(List<Reservation> userReservations) {
        HorizontalLayout chartsLayout = new HorizontalLayout();
        chartsLayout.setWidthFull();
        chartsLayout.setSpacing(true);
        chartsLayout.getStyle().set("margin-top", "40px");

        // 1. Graphique par Catégorie
        VerticalLayout categoryChart = createChartContainer("Répartition par Catégorie");
        Map<EventCategory, Long> categoryData = userReservations.stream()
                .collect(Collectors.groupingBy(r -> r.getEvenement().getCategorie(), Collectors.counting()));

        long totalRes = userReservations.size();
        for (EventCategory cat : EventCategory.values()) {
            long count = categoryData.getOrDefault(cat, 0L);
            double percentage = totalRes > 0 ? (count * 100.0 / totalRes) : 0;
            categoryChart.add(createHorizontalBar(cat.name(), percentage, count));
        }

        // 2. Graphique d'activité mensuelle
        VerticalLayout activityChart = createChartContainer("Activité des 6 derniers mois");
        HorizontalLayout barArea = new HorizontalLayout();
        barArea.setWidthFull();
        barArea.setHeight("200px");
        barArea.setAlignItems(FlexComponent.Alignment.END);
        barArea.setJustifyContentMode(FlexComponent.JustifyContentMode.AROUND);

        Map<String, Long> monthlyData = userReservations.stream()
                .collect(Collectors.groupingBy(r -> r.getDateReservation().getMonth()
                        .getDisplayName(TextStyle.SHORT, Locale.FRENCH), Collectors.counting()));

        for (int i = 5; i >= 0; i--) {
            String month = java.time.LocalDate.now().minusMonths(i).getMonth()
                    .getDisplayName(TextStyle.SHORT, Locale.FRENCH);
            long count = monthlyData.getOrDefault(month, 0L);
            barArea.add(createVerticalBar(month, count));
        }
        activityChart.add(barArea);

        chartsLayout.add(categoryChart, activityChart);
        container.add(chartsLayout);
    }

    private VerticalLayout createChartContainer(String title) {
        VerticalLayout card = new VerticalLayout();
        card.getStyle().set("background-color", "white").set("border-radius", "25px").set("padding", "30px").set("box-shadow", "0 10px 30px rgba(0,0,0,0.03)");
        H3 h3 = new H3(title);
        h3.getStyle().set("color", BRAND_BLUE).set("font-weight", "800").set("margin-bottom", "20px");
        card.add(h3);
        return card;
    }

    private VerticalLayout createHorizontalBar(String label, double percentage, long count) {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false); layout.setSpacing(false); layout.setWidthFull();
        HorizontalLayout textRow = new HorizontalLayout(new Span(label), new Span(count + " billets"));
        textRow.setWidthFull(); textRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        textRow.getStyle().set("font-size", "0.85em").set("font-weight", "600").set("color", "#555");
        Div track = new Div();
        track.setWidthFull(); track.setHeight("10px");
        track.getStyle().set("background-color", "#f0f2f5").set("border-radius", "5px").set("margin-top", "5px");
        Div fill = new Div();
        fill.setWidth(percentage + "%"); fill.setHeightFull();
        fill.getStyle().set("background-color", BRAND_BLUE).set("border-radius", "5px");
        track.add(fill);
        layout.add(textRow, track);
        return layout;
    }

    private VerticalLayout createVerticalBar(String month, long value) {
        VerticalLayout col = new VerticalLayout();
        col.setAlignItems(Alignment.CENTER); col.setPadding(false); col.setSpacing(false);
        double height = Math.min(value * 30, 150);
        Div bar = new Div();
        bar.setWidth("20px"); bar.setHeight(height + "px");
        bar.getStyle().set("background", "linear-gradient(to top, " + BRAND_BLUE + ", #435591)").set("border-radius", "4px 4px 0 0");
        col.add(new Span(String.valueOf(value)), bar, new Span(month));
        col.getChildren().forEach(c -> c.getStyle().set("font-size", "0.75em"));
        return col;
    }

    private Div createStatCard(String iconName, String label, String value, String color) {
        Div card = new Div();
        card.getStyle()
                .set("background-color", "white")
                .set("border-radius", "25px")
                .set("padding", "40px")
                .set("box-shadow", "0 15px 35px rgba(0,0,0,0.05)")
                .set("border-top", "6px solid " + color)
                .set("transition", "transform 0.3s ease");

        Image icon = new Image(ICON_PATH + iconName, "");
        icon.setHeight("35px");

        Span val = new Span(value);
        val.getStyle().set("font-size", "2.5em").set("font-weight", "800").set("color", BRAND_BLUE).set("display", "block").set("margin", "15px 0 5px 0");

        Span lbl = new Span(label);
        lbl.getStyle().set("color", "#888").set("text-transform", "uppercase").set("font-size", "0.9em").set("font-weight", "600");

        card.add(icon, val, lbl);
        card.getElement().executeJs("this.onmouseover = () => { this.style.transform = 'translateY(-10px)'; };" +
                "this.onmouseout = () => { this.style.transform = 'translateY(0)'; };");
        return card;
    }
}