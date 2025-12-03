package com.event.event_reservation.view.publics;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.event.event_reservation.config.NavigationManager;
import com.event.event_reservation.entity.Event;
import com.event.event_reservation.entity.enums.EventCategory;
import com.event.event_reservation.entity.enums.EventStatus;
import com.event.event_reservation.service.EventService;
import com.event.event_reservation.view.components.VaadinAppLayout;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Page de liste des événements publics
 * URL: /events
 */
@Route(value = "events", layout = VaadinAppLayout.class)
@PageTitle("Événements - Event Reservation")
public class EventListView extends VerticalLayout {

    private final EventService eventService;

    private Grid<Event> grid;
    private TextField searchField;
    private ComboBox<EventCategory> categoryFilter;
    private ComboBox<String> cityFilter;

    private List<Event> allEvents;

    @Autowired
    public EventListView(EventService eventService) {
        this.eventService = eventService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        createHeader();
        createFilters();
        createGrid();

        loadEvents();
    }

    /**
     * Créer le header
     */
    private void createHeader() {
        H1 title = new H1("📅 Événements Disponibles");
        title.getStyle().set("margin-bottom", "0");

        Span subtitle = new Span("Découvrez tous les événements à venir");
        subtitle.getStyle().set("color", "var(--lumo-secondary-text-color)");

        add(title, subtitle);
    }

    /**
     * Créer les filtres de recherche
     */
    private void createFilters() {
        HorizontalLayout filters = new HorizontalLayout();
        filters.setWidthFull();
        filters.setSpacing(true);
        filters.getStyle().set("flex-wrap", "wrap");

        // Champ de recherche
        searchField = new TextField();
        searchField.setPlaceholder("Rechercher un événement...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setWidth("300px");
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> applyFilters());

        // Filtre catégorie
        categoryFilter = new ComboBox<>("Catégorie");
        categoryFilter.setItems(EventCategory.values());
        categoryFilter.setPlaceholder("Toutes");
        categoryFilter.setWidth("200px");
        categoryFilter.addValueChangeListener(e -> applyFilters());

        // Filtre ville
        cityFilter = new ComboBox<>("Ville");
        cityFilter.setItems("Toutes", "Casablanca", "Rabat", "Marrakech", "Tanger", "Fès");
        cityFilter.setValue("Toutes");
        cityFilter.setWidth("200px");
        cityFilter.addValueChangeListener(e -> applyFilters());

        // Bouton réinitialiser
        Button resetBtn = new Button("Réinitialiser", VaadinIcon.REFRESH.create());
        resetBtn.addClickListener(e -> resetFilters());

        filters.add(searchField, categoryFilter, cityFilter, resetBtn);
        add(filters);
    }

    /**
     * Créer la grille d'événements
     */
    private void createGrid() {
        grid = new Grid<>(Event.class, false);
        grid.setHeight("600px");

        // Colonne Titre
        grid.addColumn(Event::getTitre)
                .setHeader("Événement")
                .setAutoWidth(true)
                .setFlexGrow(1);

        // Colonne Catégorie
        grid.addComponentColumn(event -> {
            Span badge = new Span(event.getCategorie().toString());
            badge.getElement().getThemeList().add("badge");
            badge.getStyle()
                    .set("background", getCategoryColor(event.getCategorie()))
                    .set("color", "white")
                    .set("padding", "4px 8px")
                    .set("border-radius", "4px")
                    .set("font-size", "0.85em");
            return badge;
        }).setHeader("Catégorie").setAutoWidth(true);

        // Colonne Date
        grid.addColumn(event -> event.getDateDebut().format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
        )).setHeader("Date").setAutoWidth(true);

        // Colonne Ville
        grid.addColumn(Event::getVille)
                .setHeader("Ville")
                .setAutoWidth(true);

        // Colonne Prix
        grid.addColumn(event -> event.getPrixUnitaire() + " DH")
                .setHeader("Prix")
                .setAutoWidth(true);

        // Colonne Places disponibles
        grid.addComponentColumn(event -> {
            Span places = new Span(event.getCapaciteRestante() + " / " + event.getCapaciteMax());
            if (event.getCapaciteRestante() == 0) {
                places.getStyle().set("color", "var(--lumo-error-color)");
            } else if (event.getCapaciteRestante() < event.getCapaciteMax() * 0.2) {
                places.getStyle().set("color", "var(--lumo-warning-color)");
            } else {
                places.getStyle().set("color", "var(--lumo-success-color)");
            }
            return places;
        }).setHeader("Places").setAutoWidth(true);

        // Colonne Actions
        grid.addComponentColumn(event -> {
            Button detailsBtn = new Button("Voir détails", VaadinIcon.EYE.create());
            detailsBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
            detailsBtn.addClickListener(e -> NavigationManager.goToEventDetail(event.getId()));
            return detailsBtn;
        }).setHeader("Action").setAutoWidth(true);

        add(grid);
    }

    /**
     * Charger les événements
     */
    private void loadEvents() {
        // Récupérer seulement les événements PUBLIÉS
        allEvents = eventService.searchEvents(null, null, null, null, null, null)
                .stream()
                .filter(e -> e.getStatut() == EventStatus.PUBLIE)
                .toList();

        grid.setItems(allEvents);
    }

    /**
     * Appliquer les filtres
     */
    private void applyFilters() {
        String searchTerm = searchField.getValue().toLowerCase().trim();
        EventCategory category = categoryFilter.getValue();
        String city = cityFilter.getValue();

        List<Event> filtered = allEvents.stream()
                .filter(event -> {
                    // Filtre recherche
                    boolean matchSearch = searchTerm.isEmpty() ||
                            event.getTitre().toLowerCase().contains(searchTerm) ||
                            event.getDescription().toLowerCase().contains(searchTerm);

                    // Filtre catégorie
                    boolean matchCategory = category == null ||
                            event.getCategorie() == category;

                    // Filtre ville
                    boolean matchCity = city == null || city.equals("Toutes") ||
                            event.getVille().equalsIgnoreCase(city);

                    return matchSearch && matchCategory && matchCity;
                })
                .toList();

        grid.setItems(filtered);
    }

    /**
     * Réinitialiser les filtres
     */
    private void resetFilters() {
        searchField.clear();
        categoryFilter.clear();
        cityFilter.setValue("Toutes");
        grid.setItems(allEvents);
    }

    /**
     * Obtenir la couleur selon la catégorie
     */
    private String getCategoryColor(EventCategory category) {
        return switch (category) {
            case CONCERT -> "#8b5cf6";
            case THEATRE -> "#ec4899";
            case CONFERENCE -> "#3b82f6";
            case SPORT -> "#10b981";
            case AUTRE -> "#6b7280";
        };
    }
}