package com.event.event_reservation.view.client;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
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
import com.event.event_reservation.dto.UserStatisticsDTO;
import com.event.event_reservation.entity.User;
import com.event.event_reservation.service.UserService;
import com.event.event_reservation.view.components.VaadinAppLayout;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Dashboard CLIENT
 * URL: /dashboard
 */
@Route(value = "dashboard", layout = VaadinAppLayout.class)
@PageTitle("Dashboard - Event Reservation")
public class DashboardView extends VerticalLayout {

    private final UserService userService;

    @Autowired
    public DashboardView(UserService userService) {
        this.userService = userService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        User currentUser = VaadinSession.getCurrentUser();
        if (currentUser == null) {
            NavigationManager.goToLogin();
            return;
        }

        createHeader(currentUser);
        createStatisticsCards(currentUser);
        createQuickActions();
    }

    /**
     * Créer le header
     */
    private void createHeader(User user) {
        H1 title = new H1("👋 Bienvenue, " + user.getPrenom() + " !");
        title.getStyle().set("margin-bottom", "0");

        Span subtitle = new Span("Voici un aperçu de votre activité");
        subtitle.getStyle().set("color", "var(--lumo-secondary-text-color)");

        add(title, subtitle);
    }

    /**
     * Créer les cartes de statistiques
     */
    private void createStatisticsCards(User user) {
        UserStatisticsDTO stats = userService.getUserStatistics(user.getId());

        HorizontalLayout cards = new HorizontalLayout();
        cards.setWidthFull();
        cards.setSpacing(true);
        cards.getStyle().set("flex-wrap", "wrap");

        // Carte 1: Nombre de réservations
        Div reservationsCard = createStatCard(
                VaadinIcon.TICKET,
                "Réservations",
                String.valueOf(stats.getReservationsCount()),
                "#3b82f6"
        );

        // Carte 2: Événements créés (si organisateur)
        Div eventsCard = createStatCard(
                VaadinIcon.CALENDAR,
                "Événements organisés",
                String.valueOf(stats.getEventsCreated()),
                "#8b5cf6"
        );

        // Carte 3: Montant dépensé
        Div spentCard = createStatCard(
                VaadinIcon.DOLLAR,
                "Total dépensé",
                stats.getTotalSpent() + " DH",
                "#10b981"
        );

        cards.add(reservationsCard, eventsCard, spentCard);
        add(cards);
    }

    /**
     * Créer une carte de statistique
     */
    private Div createStatCard(VaadinIcon icon, String label, String value, String color) {
        Div card = new Div();
        card.setWidth("300px");
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
     * Créer les actions rapides
     */
    private void createQuickActions() {
        H2 actionsTitle = new H2("🚀 Actions Rapides");
        actionsTitle.getStyle().set("margin-top", "2em");

        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);
        actions.getStyle().set("flex-wrap", "wrap");

        // Bouton: Voir les événements
        Button eventsBtn = new Button("Explorer les événements", VaadinIcon.CALENDAR.create());
        eventsBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        eventsBtn.addClickListener(e -> NavigationManager.goToEventList());

        // Bouton: Mes réservations
        Button reservationsBtn = new Button("Mes réservations", VaadinIcon.TICKET.create());
        reservationsBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        reservationsBtn.addClickListener(e -> NavigationManager.goToMyReservations());

        // Bouton: Mon profil
        Button profileBtn = new Button("Mon profil", VaadinIcon.USER.create());
        profileBtn.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        profileBtn.addClickListener(e -> NavigationManager.goToProfile());

        actions.add(eventsBtn, reservationsBtn, profileBtn);
        add(actionsTitle, actions);
    }
}