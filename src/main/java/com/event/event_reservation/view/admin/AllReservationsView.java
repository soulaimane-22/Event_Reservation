package com.event.event_reservation.view.admin;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.event.event_reservation.entity.Reservation;
import com.event.event_reservation.entity.enums.ReservationStatus;
import com.event.event_reservation.entity.enums.EventCategory;
import com.event.event_reservation.entity.enums.UserRole;
import com.event.event_reservation.repository.ReservationRepository;
import com.event.event_reservation.config.VaadinSession;
import com.event.event_reservation.config.NavigationManager;
import com.event.event_reservation.view.components.VaadinAppLayout;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Route(value = "admin/reservations", layout = VaadinAppLayout.class)
@PageTitle("Gestion Réservations - Admin")
public class AllReservationsView extends VerticalLayout {

    private final ReservationRepository reservationRepository;
    private final String BRAND_BLUE = "#253366";
    private final String ICON_PATH = "images/events/icons/";

    private Grid<Reservation> grid;
    private TextField searchField;
    private ComboBox<ReservationStatus> statusFilter;
    private ComboBox<EventCategory> categoryFilter;
    private List<Reservation> allData;
    private final VerticalLayout container = new VerticalLayout();

    @Autowired
    public AllReservationsView(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;

        // Sécurité
        if (VaadinSession.getCurrentUser() == null || VaadinSession.getCurrentUser().getRole() != UserRole.ADMIN) {
            UI.getCurrent().navigate("");
            return;
        }

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().set("background-color", "#f8f9fa");

        container.setWidthFull();
        container.setMaxWidth("1300px");
        container.getStyle().set("margin", "0 auto");
        container.setPadding(true);

        createHeader();
        createStatsSection();
        createFilterCard();
        createGrid();

        add(container);
        loadData();
    }

    private void createHeader() {
        HorizontalLayout headerRow = new HorizontalLayout();
        headerRow.setWidthFull();
        headerRow.setAlignItems(Alignment.CENTER);
        headerRow.getStyle().set("margin", "30px 0");

        Image icon = new Image(ICON_PATH + "ticket.svg", "");
        icon.setWidth("50px");

        VerticalLayout titles = new VerticalLayout();
        titles.setPadding(false); titles.setSpacing(false);

        H1 title = new H1("Gestion des Réservations");
        title.getStyle().set("color", BRAND_BLUE).set("font-weight", "800").set("margin", "0").set("font-size", "2.5em");
        Span subtitle = new Span("Supervision et analyse des ventes de billets");
        subtitle.getStyle().set("color", "#666").set("font-size", "1.1em");

        titles.add(title, subtitle);
        headerRow.add(icon, titles);
        container.add(headerRow);
    }

    private void createStatsSection() {
        List<Reservation> reservations = reservationRepository.findAll();
        long totalRes = reservations.size();
        long totalPlaces = reservations.stream().mapToLong(Reservation::getNombrePlaces).sum();
        Map<String, Long> byCat = reservations.stream()
                .collect(Collectors.groupingBy(r -> r.getEvenement().getCategorie().name(), Collectors.counting()));

        HorizontalLayout statsRow = new HorizontalLayout();
        statsRow.setWidthFull();
        statsRow.getStyle().set("margin-bottom", "30px");

        // Cards avec largeur diminuée (23% au lieu de 30%)
        statsRow.add(
                createStatMiniCard("Total Réservations", String.valueOf(totalRes), "#3b82f6"),
                createStatMiniCard("Places Réservées", String.valueOf(totalPlaces), "#10b981"),
                createCategoryBreakdownCard(byCat)
        );

        container.add(statsRow);
    }

    private Div createStatMiniCard(String label, String value, String color) {
        Div card = new Div();
        card.getStyle()
                .set("background-color", "white")
                .set("border-radius", "15px")
                .set("padding", "20px")
                .set("border-top", "5px solid " + BRAND_BLUE)
                .set("box-shadow", "0 4px 12px rgba(0,0,0,0.03)");
        card.setWidth("23%"); // Largeur diminuée

        Span val = new Span(value);
        val.getStyle().set("font-size", "1.6em").set("font-weight", "800").set("color", BRAND_BLUE);

        Span lbl = new Span(label);
        lbl.getStyle().set("display", "block").set("color", "#888").set("font-size", "0.75em").set("text-transform", "uppercase");

        card.add(val, lbl);
        return card;
    }

    private Div createCategoryBreakdownCard(Map<String, Long> byCat) {
        Div card = new Div();
        card.getStyle().set("background-color", "white").set("border-radius", "15px").set("padding", "15px 20px").set("box-shadow", "0 4px 12px rgba(0,0,0,0.03)");
        card.setWidth("45%");

        Span title = new Span("Top Catégories");
        title.getStyle().set("font-weight", "bold").set("font-size", "0.8em").set("color", "#888").set("display", "block").set("margin-bottom", "10px");
        card.add(title);

        HorizontalLayout badges = new HorizontalLayout();
        byCat.entrySet().stream().limit(4).forEach(entry -> {
            Span b = new Span(entry.getKey() + ": " + entry.getValue());
            b.getStyle().set("background", "#f1f3f9").set("color", BRAND_BLUE).set("padding", "4px 10px").set("border-radius", "8px").set("font-size", "0.75em").set("font-weight", "700");
            badges.add(b);
        });
        card.add(badges);
        return card;
    }

    private void createFilterCard() {
        HorizontalLayout filterRow = new HorizontalLayout();
        filterRow.setWidthFull();
        filterRow.setAlignItems(Alignment.END);
        filterRow.getStyle().set("background", "white").set("padding", "25px").set("border-radius", "15px").set("box-shadow", "0 4px 15px rgba(0,0,0,0.05)").set("margin-bottom", "20px");

        searchField = new TextField("Rechercher");
        searchField.setPlaceholder("Code, Client ou Événement...");
        searchField.setPrefixComponent(new Image(ICON_PATH + "recherche.svg", ""));
        searchField.setWidth("300px");
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> applyFilters());

        statusFilter = new ComboBox<>("Statut");
        statusFilter.setItems(ReservationStatus.values());
        statusFilter.setPlaceholder("Tous");
        statusFilter.setClearButtonVisible(true);
        statusFilter.setWidth("180px");
        statusFilter.addValueChangeListener(e -> applyFilters());

        // NOUVEAU FILTRE : Catégorie
        categoryFilter = new ComboBox<>("Catégorie");
        categoryFilter.setItems(EventCategory.values());
        categoryFilter.setPlaceholder("Toutes");
        categoryFilter.setClearButtonVisible(true);
        categoryFilter.setWidth("180px");
        categoryFilter.addValueChangeListener(e -> applyFilters());

        Button resetBtn = new Button(new Image(ICON_PATH + "reinitialiser.svg", ""));
        resetBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        resetBtn.addClickListener(e -> {
            searchField.clear();
            statusFilter.clear();
            categoryFilter.clear();
            loadData();
        });

        Anchor exportBtn = createCsvExportButton();

        filterRow.add(searchField, statusFilter, categoryFilter, resetBtn, exportBtn);
        filterRow.setFlexGrow(1, searchField);
        container.add(filterRow);
    }

    private Anchor createCsvExportButton() {
        Image icon = new Image(ICON_PATH + "exportcsv.svg", "");
        icon.setWidth("24px");

        Button btn = new Button(icon);
        btn.getStyle()
                .set("background-color", "white")
                .set("width", "50px").set("height", "50px")
                .set("border-radius", "12px").set("cursor", "pointer");

        StreamResource res = new StreamResource("export_reservations.csv", () -> {
            StringBuilder csv = new StringBuilder("Code;Client;Email;Evenement;Places;Montant;Statut\n");
            for (Reservation r : allData) {
                csv.append(r.getCodeReservation()).append(";")
                        .append(r.getUtilisateur().getPrenom()).append(" ").append(r.getUtilisateur().getNom()).append(";")
                        .append(r.getUtilisateur().getEmail()).append(";")
                        .append(r.getEvenement().getTitre()).append(";")
                        .append(r.getNombrePlaces()).append(";")
                        .append(r.getMontantTotal()).append(";")
                        .append(r.getStatut()).append("\n");
            }
            return new ByteArrayInputStream(csv.toString().getBytes(StandardCharsets.UTF_8));
        });

        Anchor anchor = new Anchor(res, "");
        anchor.getElement().setAttribute("download", true);
        anchor.add(btn);
        return anchor;
    }

    private void createGrid() {
        grid = new Grid<>(Reservation.class, false);
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        grid.getStyle().set("background-color", "white").set("border-radius", "20px").set("box-shadow", "0 10px 30px rgba(0,0,0,0.03)").set("overflow", "hidden");
        grid.setHeight("600px");

        grid.addColumn(Reservation::getCodeReservation).setHeader("CODE").setAutoWidth(true).setSortable(true);

        // EMAIL DU CLIENT AJOUTÉ
        grid.addColumn(r -> r.getUtilisateur().getEmail()).setHeader("EMAIL CLIENT").setAutoWidth(true).setSortable(true);

        grid.addColumn(r -> r.getUtilisateur().getPrenom() + " " + r.getUtilisateur().getNom()).setHeader("NOM CLIENT").setFlexGrow(1);
        grid.addColumn(r -> r.getEvenement().getTitre()).setHeader("ÉVÉNEMENT").setFlexGrow(1);
        grid.addColumn(r -> r.getMontantTotal() + " MAD").setHeader("MONTANT").setAutoWidth(true);

        grid.addComponentColumn(r -> {
            Span s = new Span(r.getStatut().toString());
            s.getStyle().set("padding", "4px 12px").set("border-radius", "20px").set("font-size", "0.75em").set("font-weight", "bold").set("color", "white");
            String color = r.getStatut() == ReservationStatus.CONFIRMEE ? "#10b981" : (r.getStatut() == ReservationStatus.EN_ATTENTE ? "#f59e0b" : "#ef4444");
            s.getStyle().set("background-color", color);
            return s;
        }).setHeader("STATUT").setAutoWidth(true);

        container.add(grid);
    }

    private void loadData() {
        allData = reservationRepository.findAll();
        grid.setItems(allData);
    }

    private void applyFilters() {
        String query = searchField.getValue().toLowerCase().trim();
        ReservationStatus status = statusFilter.getValue();
        EventCategory category = categoryFilter.getValue();

        List<Reservation> filtered = allData.stream().filter(r ->
                (query.isEmpty() || r.getCodeReservation().toLowerCase().contains(query) ||
                        r.getUtilisateur().getEmail().toLowerCase().contains(query) ||
                        r.getEvenement().getTitre().toLowerCase().contains(query)) &&
                        (status == null || r.getStatut() == status) &&
                        (category == null || r.getEvenement().getCategorie() == category)
        ).collect(Collectors.toList());

        grid.setItems(filtered);
    }
}