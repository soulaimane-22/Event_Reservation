package com.event.event_reservation.view.client;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
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

@Route(value = "my-reservations", layout = VaadinAppLayout.class)
@PageTitle("Mes Réservations - Event Reservation")
public class MyReservationsView extends VerticalLayout {

    private final ReservationService reservationService;
    private final String BRAND_BLUE = "#253366";
    private final String ICON_PATH = "images/events/icons/";

    private Grid<Reservation> grid;
    private TextField searchField;
    private ComboBox<ReservationStatus> statusFilter;

    private List<Reservation> allReservations;
    private User currentUser;

    @Autowired
    public MyReservationsView(ReservationService reservationService) {
        this.reservationService = reservationService;

        this.currentUser = VaadinSession.getCurrentUser();
        if (currentUser == null) {
            UI.getCurrent().navigate("login");
            return;
        }

        // Configuration Layout Racine
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().set("background-color", "#f8f9fa");

        // Conteneur de contenu centré (Edge-to-Edge feel)
        VerticalLayout container = new VerticalLayout();
        container.setWidthFull();
        container.setMaxWidth("1200px");
        container.getStyle().set("margin", "0 auto");
        container.setPadding(true);

        createHeader(container);
        createFilters(container);
        createGrid(container);

        loadReservations();
        add(container);
    }

    private void createHeader(VerticalLayout container) {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);
        header.getStyle().set("margin", "30px 0");

        Image ticketIcon = new Image(ICON_PATH + "ticket.svg", "");
        ticketIcon.setWidth("50px");

        VerticalLayout titleLayout = new VerticalLayout();
        titleLayout.setPadding(false);
        titleLayout.setSpacing(false);

        H1 title = new H1("Mes Réservations");
        title.getStyle()
                .set("color", BRAND_BLUE)
                .set("font-weight", "800")
                .set("margin", "0")
                .set("font-size", "2.5em");

        Span subtitle = new Span("Gérez vos accès et l'historique de vos billets");
        subtitle.getStyle().set("color", "#666").set("font-size", "1.1em");

        titleLayout.add(title, subtitle);
        header.add(ticketIcon, titleLayout);
        container.add(header);
    }

    private void createFilters(VerticalLayout container) {
        HorizontalLayout filterCard = new HorizontalLayout();
        filterCard.setWidthFull();
        filterCard.setAlignItems(Alignment.END);
        filterCard.setSpacing(true);
        filterCard.getStyle()
                .set("background-color", "white")
                .set("padding", "25px")
                .set("border-radius", "15px")
                .set("box-shadow", "0 4px 15px rgba(0,0,0,0.05)")
                .set("margin-bottom", "30px");

        // Recherche par code avec icône
        searchField = new TextField("Rechercher un code");
        searchField.setPlaceholder("Ex: RES-123...");
        Image searchIcon = new Image(ICON_PATH + "recherche.svg", "");
        searchIcon.setWidth("18px");
        searchField.setPrefixComponent(searchIcon);
        searchField.setWidth("350px");
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> applyFilters());

        // Filtre statut
        statusFilter = new ComboBox<>("Filtrer par statut");
        statusFilter.setItems(ReservationStatus.values());
        statusFilter.setPlaceholder("Tous les statuts");
        statusFilter.setWidth("250px");
        statusFilter.addValueChangeListener(e -> applyFilters());

        // Bouton réinitialiser
        Button resetBtn = new Button("Réinitialiser");
        Image resetIcon = new Image(ICON_PATH + "reinitialiser.svg", "");
        resetIcon.setWidth("18px");
        resetBtn.setIcon(resetIcon);
        resetBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        resetBtn.getStyle().set("color", BRAND_BLUE).set("font-weight", "600");
        resetBtn.addClickListener(e -> resetFilters());

        filterCard.add(searchField, statusFilter, resetBtn);
        filterCard.setFlexGrow(1, searchField);
        container.add(filterCard);
    }

    private void createGrid(VerticalLayout container) {
        grid = new Grid<>(Reservation.class, false);
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        grid.getStyle()
                .set("border-radius", "15px")
                .set("overflow", "hidden")
                .set("box-shadow", "0 10px 30px rgba(0,0,0,0.03)");
        grid.setHeight("600px");

        grid.addColumn(Reservation::getCodeReservation)
                .setHeader("CODE")
                .setAutoWidth(true)
                .setSortable(true);

        grid.addColumn(r -> r.getEvenement().getTitre())
                .setHeader("ÉVÉNEMENT")
                .setFlexGrow(1)
                .setSortable(true);

        grid.addColumn(r -> r.getDateReservation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                .setHeader("DATE ACHAT")
                .setAutoWidth(true);

        grid.addColumn(Reservation::getNombrePlaces)
                .setHeader("PLACES")
                .setTextAlign(ColumnTextAlign.CENTER)
                .setAutoWidth(true);

        grid.addColumn(r -> r.getMontantTotal() + " MAD")
                .setHeader("MONTANT")
                .setAutoWidth(true);

        grid.addComponentColumn(this::createStatusBadge)
                .setHeader("STATUT")
                .setAutoWidth(true);

        grid.addComponentColumn(this::createActions)
                .setHeader("ACTIONS")
                .setTextAlign(ColumnTextAlign.END)
                .setAutoWidth(true);

        container.add(grid);
    }

    private Span createStatusBadge(Reservation reservation) {
        Span badge = new Span(reservation.getStatut().toString());
        var s = badge.getStyle();
        s.set("padding", "6px 12px");
        s.set("border-radius", "20px");
        s.set("font-size", "0.8em");
        s.set("font-weight", "bold");
        s.set("color", "white");

        String color = switch (reservation.getStatut()) {
            case CONFIRMEE -> "#10b981";
            case EN_ATTENTE -> "#f59e0b";
            case ANNULEA -> null;
            case ANNULEE -> "#ef4444";
        };
        s.set("background-color", color);

        return badge;
    }

    private HorizontalLayout createActions(Reservation reservation) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setJustifyContentMode(JustifyContentMode.END);

        Button detailsBtn = new Button("Voir");
        detailsBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        detailsBtn.getStyle().set("color", BRAND_BLUE).set("font-weight", "700");
        detailsBtn.addClickListener(e -> NavigationManager.goToEventDetail(reservation.getEvenement().getId()));

        actions.add(detailsBtn);

        if (reservation.getStatut() == ReservationStatus.CONFIRMEE) {
            Button cancelBtn = new Button("Annuler");
            cancelBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            cancelBtn.addClickListener(e -> confirmCancellation(reservation));
            actions.add(cancelBtn);
        }

        return actions;
    }

    private void confirmCancellation(Reservation reservation) {
        ConfirmDialog dialog = new ConfirmDialog("Annuler la réservation",
                "Êtes-vous sûr de vouloir annuler la réservation " + reservation.getCodeReservation() + " ?",
                "Oui, annuler", e -> handleCancellation(reservation),
                "Non, garder", e -> {});
        dialog.setConfirmButtonTheme("error primary");
        dialog.open();
    }

    private void handleCancellation(Reservation reservation) {
        try {
            reservationService.cancelReservation(reservation.getId());
            Notification.show("Réservation annulée avec succès", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            loadReservations();
        } catch (Exception e) {
            Notification.show(e.getMessage(), 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void loadReservations() {
        allReservations = reservationService.getUserReservations(currentUser.getId());
        grid.setItems(allReservations);
    }

    private void applyFilters() {
        String query = searchField.getValue().toLowerCase().trim();
        ReservationStatus status = statusFilter.getValue();

        List<Reservation> filtered = allReservations.stream()
                .filter(r -> (query.isEmpty() || r.getCodeReservation().toLowerCase().contains(query)) &&
                        (status == null || r.getStatut() == status))
                .toList();

        grid.setItems(filtered);
    }

    private void resetFilters() {
        searchField.clear();
        statusFilter.clear();
        grid.setItems(allReservations);
    }
}