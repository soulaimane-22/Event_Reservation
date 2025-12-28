package com.event.event_reservation.view.client;

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
import com.event.event_reservation.dto.UserStatisticsDTO;
import com.event.event_reservation.entity.User;
import com.event.event_reservation.service.UserService;
import com.event.event_reservation.view.components.VaadinAppLayout;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Dashboard Client Épuré
 * Focus : Statistiques uniquement
 */
@Route(value = "dashboard", layout = VaadinAppLayout.class)
@PageTitle("Tableau de bord - Event Reservation")
public class DashboardView extends VerticalLayout implements BeforeEnterObserver {

    private final UserService userService;
    private final String BRAND_BLUE = "#253366";
    private final String ICON_PATH = "images/events/icons/";

    private final VerticalLayout container = new VerticalLayout();

    @Autowired
    public DashboardView(UserService userService) {
        this.userService = userService;

        // Configuration de base du layout racine
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().set("background-color", "#f8f9fa");

        // Conteneur de contenu centré
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

        // Construction du contenu
        container.removeAll();
        createHeader(currentUser);
        createStatisticsGrid(currentUser);
    }

    private void createHeader(User user) {
        VerticalLayout header = new VerticalLayout();
        header.setPadding(false);
        header.setSpacing(false);
        header.getStyle().set("margin", "40px 0");

        H1 welcome = new H1("Bonjour, " + user.getPrenom()  + " " +  user.getNom() + " !");
        welcome.getStyle()
                .set("color", BRAND_BLUE)
                .set("font-weight", "800")
                .set("margin", "0")
                .set("font-size", "2.8em");

        Span subtitle = new Span("Ravi de vous revoir. Voici un résumé de votre activité sur la plateforme.");
        subtitle.getStyle()
                .set("color", "#666")
                .set("font-size", "1.2em")
                .set("margin-top", "5px");

        header.add(welcome, subtitle);
        container.add(header);
    }

    private void createStatisticsGrid(User user) {
        UserStatisticsDTO stats = userService.getUserStatistics(user.getId());

        // Grille adaptative pour les cartes
        Div grid = new Div();
        grid.setWidthFull();
        grid.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(auto-fit, minmax(320px, 1fr))")
                .set("gap", "30px");

        // Carte 1 : Réservations
        grid.add(createStatCard(
                "statistics.svg",
                "Réservations effectuées",
                String.valueOf(stats.getReservationsCount()),
                "#3b82f6"
        ));

        // Carte 2 : Événements (Si applicable)
        grid.add(createStatCard(
                "statistics.svg",
                "Événements organisés",
                String.valueOf(stats.getEventsCreated()),
                "#8b5cf6"
        ));

        // Carte 3 : Dépenses
        grid.add(createStatCard(
                "statistics.svg",
                "Investissement Total",
                stats.getTotalSpent() + " MAD",
                "#10b981"
        ));

        container.add(grid);
    }

    private Div createStatCard(String iconName, String label, String value, String color) {
        Div card = new Div();
        card.getStyle()
                .set("background-color", "white")
                .set("border-radius", "25px")
                .set("padding", "40px")
                .set("box-shadow", "0 15px 35px rgba(0,0,0,0.05)")
                .set("border-top", "6px solid " + color) // Accent de couleur en haut pour changer du bord gauche
                .set("transition", "transform 0.3s ease");

        Image icon = new Image(ICON_PATH + iconName, "");
        icon.setHeight("35px");

        Span val = new Span(value);
        val.getStyle()
                .set("font-size", "2.5em")
                .set("font-weight", "800")
                .set("color", BRAND_BLUE)
                .set("display", "block")
                .set("margin", "15px 0 5px 0");

        Span lbl = new Span(label);
        lbl.getStyle()
                .set("color", "#888")
                .set("text-transform", "uppercase")
                .set("font-size", "0.9em")
                .set("font-weight", "600")
                .set("letter-spacing", "1px");

        card.add(icon, val, lbl);

        // Effet au survol
        card.getElement().executeJs("this.onmouseover = () => { this.style.transform = 'translateY(-10px)'; };" +
                "this.onmouseout = () => { this.style.transform = 'translateY(0)'; };");

        return card;
    }
}