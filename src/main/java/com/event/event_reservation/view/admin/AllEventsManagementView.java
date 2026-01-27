package com.event.event_reservation.view.admin;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.event.event_reservation.entity.Event;
import com.event.event_reservation.entity.enums.EventCategory;
import com.event.event_reservation.entity.enums.EventStatus;
import com.event.event_reservation.entity.enums.UserRole;
import com.event.event_reservation.repository.EventRepository;
import com.event.event_reservation.service.EventService;
import com.event.event_reservation.config.NavigationManager;
import com.event.event_reservation.config.VaadinSession;
import com.event.event_reservation.view.components.VaadinAppLayout;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Route(value = "admin/events", layout = VaadinAppLayout.class)
@PageTitle("Gestion des Événements - Admin")
public class AllEventsManagementView extends VerticalLayout {

    private final EventRepository eventRepository;
    private final EventService eventService;

    private final String BRAND_BLUE = "#253366";
    private final String ICON_PATH = "images/events/icons/";

    private Grid<Event> grid;
    private TextField searchField;
    private ComboBox<EventCategory> categoryFilter;
    private ComboBox<String> cityFilter;
    private ComboBox<EventStatus> statusFilter;

    private List<Event> allEvents;
    private final VerticalLayout container = new VerticalLayout();

    @Autowired
    public AllEventsManagementView(EventRepository eventRepository, EventService eventService) {
        this.eventRepository = eventRepository;
        this.eventService = eventService;

        // Sécurité Admin
        if (VaadinSession.getCurrentUser() == null || VaadinSession.getCurrentUser().getRole() != UserRole.ADMIN) {
            UI.getCurrent().navigate("");
            return;
        }

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().set("background-color", "#f8f9fa");

        container.setWidthFull();
        container.setMaxWidth("1350px");
        container.setPadding(true);
        container.getStyle().set("margin", "0 auto");

        createHeader();
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

        Image icon = new Image(ICON_PATH + "event.svg", "");
        icon.setWidth("50px");

        VerticalLayout titles = new VerticalLayout();
        titles.setPadding(false); titles.setSpacing(false);

        H1 title = new H1("Gestion des Événements");
        title.getStyle().set("color", BRAND_BLUE).set("font-weight", "800").set("margin", "0").set("font-size", "2.5em");
        Span subtitle = new Span("Panneau de contrôle global pour l'administration des événements");
        subtitle.getStyle().set("color", "#666").set("font-size", "1.1em");

        titles.add(title, subtitle);
        headerRow.add(icon, titles);
        container.add(headerRow);
    }

    private void createFilterCard() {
        HorizontalLayout filterRow = new HorizontalLayout();
        filterRow.setWidthFull();
        filterRow.setAlignItems(Alignment.END);
        filterRow.setSpacing(true);
        filterRow.getStyle()
                .set("background-color", "#EFF1FC")
                .set("padding", "25px")
                .set("border-radius", "15px")
                .set("box-shadow", "0px 7px 29px 0px rgba(37, 51, 102, 0.4)")
                .set("transition", "transform 0.3s ease")
                .set("margin-bottom", "20px");

        searchField = new TextField("Rechercher");
        searchField.setPlaceholder("Titre ou description...");
        searchField.setPrefixComponent(new Image(ICON_PATH + "recherche.svg", ""));
        searchField.setWidth("300px");
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> applyFilters());

        categoryFilter = new ComboBox<>("Catégorie");
        categoryFilter.setItems(EventCategory.values());
        categoryFilter.setPlaceholder("Toutes");
        categoryFilter.setClearButtonVisible(true);
        categoryFilter.setWidth("180px");
        categoryFilter.addValueChangeListener(e -> applyFilters());

        cityFilter = new ComboBox<>("Ville");
        cityFilter.setItems("Casablanca", "Rabat", "Marrakech", "Tanger", "Agadir", "Fès");
        cityFilter.setPlaceholder("Villes");
        cityFilter.setClearButtonVisible(true);
        cityFilter.setWidth("150px");
        cityFilter.addValueChangeListener(e -> applyFilters());

        statusFilter = new ComboBox<>("Statut");
        statusFilter.setItems(EventStatus.values());
        statusFilter.setPlaceholder("Statuts");
        statusFilter.setClearButtonVisible(true);
        statusFilter.setWidth("150px");
        statusFilter.addValueChangeListener(e -> applyFilters());

        Button resetBtn = new Button(new Image(ICON_PATH + "reinitialiser.svg", ""));
        resetBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        resetBtn.addClickListener(e -> {
            searchField.clear(); categoryFilter.clear(); cityFilter.clear(); statusFilter.clear();
            loadData();
        });

        filterRow.add(searchField, categoryFilter, cityFilter, statusFilter, resetBtn);
        filterRow.setFlexGrow(1, searchField);
        container.add(filterRow);
    }

    private void createGrid() {
        grid = new Grid<>(Event.class, false);
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        grid.getStyle()
                .set("background-color", "#EFF1FC")
                .set("border-radius", "20px")
                .set("box-shadow", "0px 7px 29px 0px rgba(37, 51, 102, 0.4)")
                .set("transition", "transform 0.3s ease")
                .set("overflow", "hidden");
        grid.setHeight("650px");

        grid.addColumn(Event::getTitre).setHeader("ÉVÉNEMENT").setSortable(true).setFlexGrow(1);
        grid.addColumn(e -> e.getOrganisateur().getEmail()).setHeader("ORGANISATEUR").setAutoWidth(true);

        grid.addComponentColumn(this::createStatusBadge).setHeader("STATUT").setAutoWidth(true);

        grid.addColumn(e -> e.getCapaciteRestante() + " / " + e.getCapaciteMax())
                .setHeader("PLACES")
                .setAutoWidth(true);

        grid.addComponentColumn(event -> {
            HorizontalLayout actions = new HorizontalLayout();

            // 1. Voir / Modifier
            Image editIcon = new Image(ICON_PATH + "modifier.svg", "");
            editIcon.setWidth("18px");
            Button editBtn = new Button(editIcon, e -> NavigationManager.goToEditEvent(event.getId()));
            editBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

            // 2. Publier (si Brouillon)
            Button pubBtn = new Button("Publier", e -> {
                eventService.publishEvent(VaadinSession.getCurrentUser().getId(), event.getId());
                loadData();
                Notification.show("Événement publié");
            });
            pubBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);
            pubBtn.setVisible(event.getStatut() == EventStatus.BROUILLON);

            // 3. Annuler (MODIFIÉ : Affiche si le statut n'est PAS publié, ex: Brouillon)
            Button cancelBtn = new Button("Annuler", e -> {
                eventService.cancelEvent(VaadinSession.getCurrentUser().getId(), event.getId(), "Annulation Admin");
                loadData();
                Notification.show("Événement annulé");
            });
            cancelBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
            cancelBtn.setVisible(event.getStatut() != EventStatus.PUBLIE); // <-- Correction ici

            // 4. Supprimer
            Button delBtn = new Button("Supprimer", e -> confirmDelete(event));
            delBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);

            actions.add(editBtn, pubBtn, cancelBtn, delBtn);
            return actions;
        }).setHeader("ACTIONS").setAutoWidth(true);

        container.add(grid);
    }

    private Span createStatusBadge(Event event) {
        Span badge = new Span(event.getStatut().toString());
        var s = badge.getStyle();
        s.set("padding", "4px 12px").set("border-radius", "20px").set("font-size", "0.75em").set("font-weight", "bold").set("color", "white");

        String color = switch (event.getStatut()) {
            case PUBLIE -> "#10b981";
            case BROUILLON -> "#6b7280";
            case ANNULE -> "#ef4444";
            case TERMINE -> "#3b82f6";
        };
        s.set("background-color", color);
        return badge;
    }

    private void confirmDelete(Event event) {
        ConfirmDialog dialog = new ConfirmDialog("Supprimer l'événement",
                "Cette action est définitive. Supprimer '" + event.getTitre() + "' ?",
                "Supprimer", e -> {
            try {
                eventService.deleteEvent(VaadinSession.getCurrentUser().getId(), event.getId());
                loadData();
                Notification.show("Événement supprimé");
            } catch (Exception ex) {
                Notification.show(ex.getMessage(), 3000, Notification.Position.TOP_CENTER).addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        }, "Conserver", e -> {});
        dialog.setConfirmButtonTheme("error primary");
        dialog.open();
    }

    private void loadData() {
        allEvents = eventRepository.findAll();
        grid.setItems(allEvents);
    }

    private void applyFilters() {
        String query = searchField.getValue().toLowerCase().trim();
        EventCategory cat = categoryFilter.getValue();
        String city = cityFilter.getValue();
        EventStatus status = statusFilter.getValue();

        List<Event> filtered = allEvents.stream().filter(e ->
                (query.isEmpty() || e.getTitre().toLowerCase().contains(query)) &&
                        (cat == null || e.getCategorie() == cat) &&
                        (city == null || e.getVille().equalsIgnoreCase(city)) &&
                        (status == null || e.getStatut() == status)
        ).collect(Collectors.toList());

        grid.setItems(filtered);
    }
}