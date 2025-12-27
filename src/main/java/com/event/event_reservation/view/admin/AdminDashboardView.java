// ============================================
// 🔐 AdminDashboardView.java - DASHBOARD ADMIN
// 📁 Chemin: src/main/java/com/event/event_reservation/view/admin/AdminDashboardView.java
// ============================================

package com.event.event_reservation.view.admin;

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
import com.event.event_reservation.entity.User;
import com.event.event_reservation.entity.enums.EventStatus;
import com.event.event_reservation.entity.enums.ReservationStatus;
import com.event.event_reservation.entity.enums.UserRole;
import com.event.event_reservation.repository.EventRepository;
import com.event.event_reservation.repository.ReservationRepository;
import com.event.event_reservation.repository.UserRepository;
import com.event.event_reservation.view.components.VaadinAppLayout;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

/**
 * Dashboard Administrateur
 * URL: /admin/dashboard
 */
@Route(value = "admin/dashboard", layout = VaadinAppLayout.class)
@PageTitle("Dashboard Admin - Event Reservation")
public class AdminDashboardView extends VerticalLayout {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final ReservationRepository reservationRepository;

    @Autowired
    public AdminDashboardView(UserRepository userRepository,
                              EventRepository eventRepository,
                              ReservationRepository reservationRepository) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.reservationRepository = reservationRepository;

        User currentUser = VaadinSession.getCurrentUser();
        if (currentUser == null || currentUser.getRole() != UserRole.ADMIN) {
            NavigationManager.goToHome();
            return;
        }

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        createHeader();
        createStatisticsCards();
        createDetailedStats();
        createQuickActions();
    }

    /**
     * Créer le header
     */
    private void createHeader() {
        H1 title = new H1("🔐 Dashboard Administrateur");
        title.getStyle().set("margin-bottom", "0");

        Span subtitle = new Span("Vue d'ensemble de la plateforme");
        subtitle.getStyle().set("color", "var(--lumo-secondary-text-color)");

        add(title, subtitle);
    }

    /**
     * Créer les cartes de statistiques principales
     */
    private void createStatisticsCards() {
        HorizontalLayout cards = new HorizontalLayout();
        cards.setWidthFull();
        cards.setSpacing(true);
        cards.getStyle().set("flex-wrap", "wrap");

        // Carte 1: Utilisateurs
        long totalUsers = userRepository.count();
        Div usersCard = createStatCard(
                VaadinIcon.USERS,
                "Utilisateurs",
                String.valueOf(totalUsers),
                "#6366f1"
        );

        // Carte 2: Événements
        long totalEvents = eventRepository.count();
        Div eventsCard = createStatCard(
                VaadinIcon.CALENDAR,
                "Événements",
                String.valueOf(totalEvents),
                "#8b5cf6"
        );

        // Carte 3: Réservations
        long totalReservations = reservationRepository.count();
        Div reservationsCard = createStatCard(
                VaadinIcon.TICKET,
                "Réservations",
                String.valueOf(totalReservations),
                "#3b82f6"
        );

        // Carte 4: Revenus totaux
        BigDecimal totalRevenue = reservationRepository.findAll().stream()
                .filter(r -> r.getStatut() == ReservationStatus.CONFIRMEE)
                .map(r -> r.getMontantTotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Div revenueCard = createStatCard(
                VaadinIcon.DOLLAR,
                "Revenus Totaux",
                totalRevenue + " DH",
                "#10b981"
        );

        cards.add(usersCard, eventsCard, reservationsCard, revenueCard);
        add(cards);
    }

    /**
     * Créer les statistiques détaillées
     */
    private void createDetailedStats() {
        H2 detailsTitle = new H2("📊 Statistiques Détaillées");
        detailsTitle.getStyle().set("margin-top", "2em");
        add(detailsTitle);

        HorizontalLayout detailsLayout = new HorizontalLayout();
        detailsLayout.setWidthFull();
        detailsLayout.setSpacing(true);
        detailsLayout.getStyle().set("flex-wrap", "wrap");

        // Statistiques utilisateurs par rôle
        VerticalLayout usersStats = createDetailedCard(
                "👥 Utilisateurs par Rôle",
                new String[][]{
                        {"ADMIN", String.valueOf(userRepository.countByRole(UserRole.ADMIN)), "#ef4444"},
                        {"ORGANIZER", String.valueOf(userRepository.countByRole(UserRole.ORGANIZER)), "#f59e0b"},
                        {"CLIENT", String.valueOf(userRepository.countByRole(UserRole.CLIENT)), "#10b981"}
                }
        );

        // Statistiques événements par statut
        VerticalLayout eventsStats = createDetailedCard(
                "📅 Événements par Statut",
                new String[][]{
                        {"PUBLIE", String.valueOf(eventRepository.findByStatut(EventStatus.PUBLIE).size()), "#10b981"},
                        {"BROUILLON", String.valueOf(eventRepository.findByStatut(EventStatus.BROUILLON).size()), "#6b7280"},
                        {"ANNULE", String.valueOf(eventRepository.findByStatut(EventStatus.ANNULE).size()), "#ef4444"},
                        {"TERMINE", String.valueOf(eventRepository.findByStatut(EventStatus.TERMINE).size()), "#3b82f6"}
                }
        );

        // Statistiques réservations par statut
        VerticalLayout reservationsStats = createDetailedCard(
                "🎫 Réservations par Statut",
                new String[][]{
                        {"CONFIRMEE", String.valueOf(reservationRepository.findByStatut(ReservationStatus.CONFIRMEE).size()), "#10b981"},
                        {"EN_ATTENTE", String.valueOf(reservationRepository.findByStatut(ReservationStatus.EN_ATTENTE).size()), "#f59e0b"},
                        {"ANNULEE", String.valueOf(reservationRepository.findByStatut(ReservationStatus.ANNULEE).size()), "#ef4444"}
                }
        );

        detailsLayout.add(usersStats, eventsStats, reservationsStats);
        add(detailsLayout);
    }

    /**
     * Créer une carte de statistique principale
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
     * Créer une carte de statistiques détaillées
     */
    private VerticalLayout createDetailedCard(String title, String[][] data) {
        VerticalLayout card = new VerticalLayout();
        card.setWidth("350px");
        card.setPadding(true);
        card.setSpacing(true);
        card.getStyle()
                .set("background", "white")
                .set("border-radius", "10px")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.1)");

        H2 cardTitle = new H2(title);
        cardTitle.getStyle()
                .set("margin", "0 0 1em 0")
                .set("font-size", "1.2em")
                .set("color", "#333");
        card.add(cardTitle);

        for (String[] item : data) {
            HorizontalLayout row = new HorizontalLayout();
            row.setWidthFull();
            row.setAlignItems(Alignment.CENTER);

            Span badge = new Span(item[0]);
            badge.getStyle()
                    .set("background", item[2])
                    .set("color", "white")
                    .set("padding", "4px 12px")
                    .set("border-radius", "4px")
                    .set("font-size", "0.85em")
                    .set("min-width", "120px")
                    .set("text-align", "center");

            Span count = new Span(item[1]);
            count.getStyle()
                    .set("margin-left", "auto")
                    .set("font-weight", "bold")
                    .set("font-size", "1.5em")
                    .set("color", "#333");

            row.add(badge, count);
            card.add(row);
        }

        return card;
    }

    /**
     * Créer les actions rapides
     */
    private void createQuickActions() {
        H2 actionsTitle = new H2("🚀 Gestion Rapide");
        actionsTitle.getStyle().set("margin-top", "2em");

        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);
        actions.getStyle().set("flex-wrap", "wrap");

        // Bouton: Gérer les utilisateurs
        Button usersBtn = new Button("Gérer les utilisateurs", VaadinIcon.USERS.create());
        usersBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        usersBtn.addClickListener(e -> NavigationManager.goToUserManagement());

        // Bouton: Gérer les événements
        Button eventsBtn = new Button("Gérer les événements", VaadinIcon.CALENDAR.create());
        eventsBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        eventsBtn.addClickListener(e -> NavigationManager.goToAllEventsManagement());

        // Bouton: Gérer les réservations
        Button reservationsBtn = new Button("Gérer les réservations", VaadinIcon.TICKET.create());
        reservationsBtn.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        reservationsBtn.addClickListener(e -> NavigationManager.goToAllReservationsManagement());

        actions.add(usersBtn, eventsBtn, reservationsBtn);
        add(actionsTitle, actions);
    }
}