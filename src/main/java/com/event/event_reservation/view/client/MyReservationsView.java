package com.event.event_reservation.view.client;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.event.event_reservation.config.NavigationManager;
import com.event.event_reservation.config.VaadinSession;
import com.event.event_reservation.entity.Reservation;
import com.event.event_reservation.entity.User;
import com.event.event_reservation.entity.enums.ReservationStatus;
import com.event.event_reservation.service.ReservationService;
import com.event.event_reservation.view.components.VaadinAppLayout;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Page Mes Réservations
 * URL: /my-reservations
 */
@Route(value = "my-reservations", layout = VaadinAppLayout.class)
@PageTitle("Mes Réservations - Event Reservation")
public class MyReservationsView extends VerticalLayout {

    private final ReservationService reservationService;

    private Grid<Reservation> grid;
    private TextField searchField;
    private ComboBox<ReservationStatus> statusFilter;

    private List<Reservation> allReservations;
    private User currentUser;

    @Autowired
    public MyReservationsView(ReservationService reservationService) {
        this.reservationService = reservationService;

        currentUser = VaadinSession.getCurrentUser();
        if (currentUser == null) {
            NavigationManager.goToLogin();
            return;
        }

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        createHeader();
        createFilters();
        createGrid();

        loadReservations();
    }

    /**
     * Créer le header
     */
    private void createHeader() {
        H1 title = new H1("🎫 Mes Réservations");
        title.getStyle().set("margin-bottom", "0");

        Span subtitle = new Span("Gérez toutes vos réservations d'événements");
        subtitle.getStyle().set("color", "var(--lumo-secondary-text-color)");

        add(title, subtitle);
    }

    /**
     * Créer les filtres
     */
    private void createFilters() {
        HorizontalLayout filters = new HorizontalLayout();
        filters.setWidthFull();
        filters.setSpacing(true);
        filters.getStyle().set("flex-wrap", "wrap");

        // Recherche par code
        searchField = new TextField();
        searchField.setPlaceholder("Rechercher par code...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setWidth("300px");
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> applyFilters());

        // Filtre statut
        statusFilter = new ComboBox<>("Statut");
        statusFilter.setItems(ReservationStatus.values());
        statusFilter.setPlaceholder("Tous");
        statusFilter.setWidth("200px");
        statusFilter.addValueChangeListener(e -> applyFilters());

        // Bouton réinitialiser
        Button resetBtn = new Button("Réinitialiser", VaadinIcon.REFRESH.create());
        resetBtn.addClickListener(e -> resetFilters());

        filters.add(searchField, statusFilter, resetBtn);
        add(filters);
    }

    /**
     * Créer la grille
     */
    private void createGrid() {
        grid = new Grid<>(Reservation.class, false);
        grid.setHeight("600px");

        // Colonne Code
        grid.addColumn(Reservation::getCodeReservation)
                .setHeader("Code")
                .setAutoWidth(true);

        // Colonne Événement
        grid.addColumn(r -> r.getEvenement().getTitre())
                .setHeader("Événement")
                .setAutoWidth(true)
                .setFlexGrow(1);

        // Colonne Date
        grid.addColumn(r -> r.getDateReservation().format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
        )).setHeader("Date réservation").setAutoWidth(true);

        // Colonne Places
        grid.addColumn(Reservation::getNombrePlaces)
                .setHeader("Places")
                .setAutoWidth(true);

        // Colonne Montant
        grid.addColumn(r -> r.getMontantTotal() + " DH")
                .setHeader("Montant")
                .setAutoWidth(true);

        // Colonne Statut
        grid.addComponentColumn(this::createStatusBadge)
                .setHeader("Statut")
                .setAutoWidth(true);

        // Colonne Actions
        grid.addComponentColumn(this::createActions)
                .setHeader("Actions")
                .setAutoWidth(true);

        add(grid);
    }

    /**
     * Créer le badge de statut
     */
    private Span createStatusBadge(Reservation reservation) {
        Span badge = new Span(reservation.getStatut().toString());
        badge.getElement().getThemeList().add("badge");

        String color = switch (reservation.getStatut()) {
            case CONFIRMEE -> "#10b981";
            case EN_ATTENTE -> "#f59e0b";
            case ANNULEA -> null;
            case ANNULEE -> "#ef4444";
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
     * Créer les actions
     */
    private HorizontalLayout createActions(Reservation reservation) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);

        // Bouton Voir détails
        Button detailsBtn = new Button(VaadinIcon.EYE.create());
        detailsBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        detailsBtn.addClickListener(e ->
                NavigationManager.goToEventDetail(reservation.getEvenement().getId())
        );

        // Bouton Annuler (si possible)
        if (reservation.getStatut() == ReservationStatus.CONFIRMEE) {
            Button cancelBtn = new Button(VaadinIcon.CLOSE.create());
            cancelBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            cancelBtn.addClickListener(e -> confirmCancellation(reservation));
            actions.add(cancelBtn);
        }

        actions.add(detailsBtn);
        return actions;
    }

    /**
     * Confirmer l'annulation
     */
    private void confirmCancellation(Reservation reservation) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Annuler la réservation ?");
        dialog.setText("Êtes-vous sûr de vouloir annuler cette réservation ? Code: " +
                reservation.getCodeReservation());

        dialog.setCancelable(true);
        dialog.setCancelText("Non");

        dialog.setConfirmText("Oui, annuler");
        dialog.setConfirmButtonTheme("error primary");

        dialog.addConfirmListener(e -> handleCancellation(reservation));

        dialog.open();
    }

    /**
     * Gérer l'annulation
     */
    private void handleCancellation(Reservation reservation) {
        try {
            reservationService.cancelReservation(reservation.getId());
            showSuccess("Réservation annulée avec succès");
            loadReservations();
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }

    /**
     * Charger les réservations
     */
    private void loadReservations() {
        allReservations = reservationService.getUserReservations(currentUser.getId());
        grid.setItems(allReservations);
    }

    /**
     * Appliquer les filtres
     */
    private void applyFilters() {
        String searchTerm = searchField.getValue().toLowerCase().trim();
        ReservationStatus status = statusFilter.getValue();

        List<Reservation> filtered = allReservations.stream()
                .filter(r -> {
                    boolean matchSearch = searchTerm.isEmpty() ||
                            r.getCodeReservation().toLowerCase().contains(searchTerm);

                    boolean matchStatus = status == null || r.getStatut() == status;

                    return matchSearch && matchStatus;
                })
                .toList();

        grid.setItems(filtered);
    }

    /**
     * Réinitialiser les filtres
     */
    private void resetFilters() {
        searchField.clear();
        statusFilter.clear();
        grid.setItems(allReservations);
    }

    /**
     * Afficher un message de succès
     */
    private void showSuccess(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    /**
     * Afficher un message d'erreur
     */
    private void showError(String message) {
        Notification notification = Notification.show(message, 4000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}