package com.event.event_reservation.view.publics;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.*;
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
import java.util.Locale;

@Route(value = "events", layout = VaadinAppLayout.class)
@PageTitle("Événements - Event Reservation")
public class EventListView extends VerticalLayout {

    private final EventService eventService;
    private final String BRAND_BLUE = "#253366";
    private final String ICON_PATH = "images/events/icons/";

    private TextField searchField;
    private ComboBox<EventCategory> categoryFilter;
    private ComboBox<String> cityFilter;
    private Div cardsContainer; // Remplace la Grid

    private List<Event> allEvents;

    @Autowired
    public EventListView(EventService eventService) {
        this.eventService = eventService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        setAlignItems(Alignment.CENTER);
        getStyle().set("background-color", "#f8f9fa");

        createHeader();
        createFilters();

        // Initialisation du conteneur de cartes
        cardsContainer = new Div();
        cardsContainer.setWidthFull();
        cardsContainer.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(auto-fill, minmax(350px, 1fr))")
                .set("gap", "30px")
                .set("padding", "20px 0");

        add(cardsContainer);

        loadEvents();
    }

    /**
     * Créer le header attractif avec icône SVG
     */
    private void createHeader() {
        VerticalLayout headerContainer = new VerticalLayout();
        headerContainer.setAlignItems(Alignment.CENTER);
        headerContainer.setSpacing(false);
        headerContainer.getStyle().set("margin-top", "40px").set("margin-bottom", "20px");

        // 1. L'icône Event SVG (Mise en avant)
        Image eventIcon = new Image(ICON_PATH + "event.svg", "Event Icon");
        eventIcon.setWidth("60px");
        eventIcon.setHeight("60px");
        eventIcon.getStyle().set("margin-bottom", "20px");

        // 2. Titre principal attractif
        H1 title = new H1("Vivez des moments d'exception");
        title.getStyle()
                .set("color", BRAND_BLUE)
                .set("margin", "0")
                .set("font-size", "2.8em")
                .set("font-weight", "800")
                .set("text-align", "center");

        // 3. Sous-titre engageant
        Span subtitle = new Span("Explorez notre sélection exclusive et sécurisez vos places pour les plus grands événements au Maroc.");
        subtitle.getStyle()
                .set("color", BRAND_BLUE)
                .set("opacity", "0.7")
                .set("font-size", "1.2em")
                .set("text-align", "center")
                .set("max-width", "700px")
                .set("margin-top", "10px");

        headerContainer.add(eventIcon, title, subtitle);
        add(headerContainer);
    }
    private void createFilters() {
        HorizontalLayout filters = new HorizontalLayout();
        filters.setWidthFull();
        filters.setMaxWidth("1100px");
        filters.setAlignItems(Alignment.END);
        filters.setSpacing(true);
        filters.getStyle()
                .set("background", "white")
                .set("padding", "20px")
                .set("border-radius", "15px")
                .set("box-shadow", "0 4px 12px rgba(0,0,0,0.05)")
                .set("margin-bottom", "20px");

        // Champ recherche avec icône SVG
        searchField = new TextField("Recherche");
        searchField.setPlaceholder("Nom de l'événement...");
        Image searchIcon = new Image(ICON_PATH + "recherche.svg", "");
        searchIcon.setWidth("18px");
        searchField.setPrefixComponent(searchIcon);
        searchField.setWidth("350px");
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> applyFilters());

        // Catégorie
        categoryFilter = new ComboBox<>("Catégorie");
        categoryFilter.setItems(EventCategory.values());
        categoryFilter.setPlaceholder("Toutes les catégories");
        categoryFilter.setClearButtonVisible(true);
        categoryFilter.addValueChangeListener(e -> applyFilters());

        // Ville
        cityFilter = new ComboBox<>("Ville");
        cityFilter.setItems("Casablanca", "Rabat", "Marrakech", "Tanger", "Agadir", "Fès");
        cityFilter.setPlaceholder("Toutes les villes");
        cityFilter.setClearButtonVisible(true);
        cityFilter.addValueChangeListener(e -> applyFilters());

        // Bouton réinitialiser avec SVG
        Button resetBtn = new Button("Réinitialiser");
        Image resetIcon = new Image(ICON_PATH + "reinitialiser.svg", "");
        resetIcon.setWidth("18px");
        resetBtn.setIcon(resetIcon);
        resetBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        resetBtn.getStyle().set("color", BRAND_BLUE);
        resetBtn.addClickListener(e -> resetFilters());

        filters.add(searchField, categoryFilter, cityFilter, resetBtn);
        filters.setFlexGrow(1, searchField);
        add(filters);
    }

    private void loadEvents() {
        allEvents = eventService.searchEvents(null, null, null, null, null, null)
                .stream()
                .filter(e -> e.getStatut() == EventStatus.PUBLIE)
                .toList();
        updateGrid(allEvents);
    }

    private void updateGrid(List<Event> events) {
        cardsContainer.removeAll();
        if (events.isEmpty()) {
            cardsContainer.add(new H3("Aucun événement ne correspond à vos critères."));
        } else {
            events.forEach(event -> cardsContainer.add(createEventCard(event)));
        }
    }

    private void applyFilters() {
        String searchTerm = searchField.getValue().toLowerCase().trim();
        EventCategory category = categoryFilter.getValue();
        String city = cityFilter.getValue();

        List<Event> filtered = allEvents.stream()
                .filter(event -> {
                    boolean matchSearch = searchTerm.isEmpty() || event.getTitre().toLowerCase().contains(searchTerm);
                    boolean matchCategory = category == null || event.getCategorie() == category;
                    boolean matchCity = city == null || event.getVille().equalsIgnoreCase(city);
                    return matchSearch && matchCategory && matchCity;
                })
                .toList();

        updateGrid(filtered);
    }

    private void resetFilters() {
        searchField.clear();
        categoryFilter.clear();
        cityFilter.clear();
        updateGrid(allEvents);
    }

    // --- CRÉATION DE LA CARTE (IDENTIQUE À HOME VIEW) ---
    private VerticalLayout createEventCard(Event event) {
        VerticalLayout card = new VerticalLayout();
        card.setPadding(false);
        card.setSpacing(false);
        card.getStyle()
                .set("background", "white")
                .set("border-radius", "20px")
                .set("overflow", "hidden")
                .set("box-shadow", "0 10px 30px rgba(0,0,0,0.08)")
                .set("transition", "transform 0.3s ease");

        Div imageContainer = new Div();
        imageContainer.setWidthFull();
        imageContainer.setHeight("220px");
        imageContainer.getStyle().set("position", "relative");

        Image img = new Image(event.getImageUrl(), event.getTitre());
        img.setWidthFull(); img.setHeightFull();
        img.getStyle().set("object-fit", "cover");

        if (event.getNbPlacesDisponibles() <= 0) {
            Span soldOut = new Span("COMPLET");
            soldOut.getStyle()
                    .set("position", "absolute").set("top", "20px").set("left", "20px")
                    .set("background", BRAND_BLUE).set("color", "white")
                    .set("padding", "8px 20px").set("font-weight", "800")
                    .set("border-radius", "6px").set("font-size", "0.8em");
            imageContainer.add(img, soldOut);
        } else {
            imageContainer.add(img);
        }

        VerticalLayout body = new VerticalLayout();
        body.setPadding(true);
        body.setSpacing(false);
        body.getStyle().set("padding", "25px");

        H3 title = new H3(event.getTitre());
        title.getStyle().set("color", BRAND_BLUE).set("margin", "0 0 15px 0").set("font-size", "1.3em");

        body.add(title);
        body.add(createInfoRow("date_time.svg", event.getDateDebut().format(DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy", Locale.FRENCH))));
        body.add(createInfoRow("clock_hour.svg", event.getDateDebut().format(DateTimeFormatter.ofPattern("HH:mm"))));
        body.add(createInfoRow("map.svg", event.getVille()));

        Span priceValue = new Span(event.getPrixUnitaire() + " MAD");
        priceValue.getStyle()
                .set("color", BRAND_BLUE).set("font-weight", "800")
                .set("font-size", "1.5em").set("margin-top", "10px");

        HorizontalLayout priceRow = createInfoRow("argent.svg", "");
        priceRow.add(priceValue);
        body.add(priceRow);

        Button detailsBtn = new Button("Détails de l'événement");
        detailsBtn.setWidthFull();
        detailsBtn.getStyle()
                .set("margin-top", "20px")
                .set("background-color", BRAND_BLUE)
                .set("color", "white")
                .set("height", "45px");
        detailsBtn.addClickListener(e -> NavigationManager.goToEventDetail(event.getId()));

        body.add(detailsBtn);
        card.add(imageContainer, body);

        card.getElement().executeJs("this.onmouseover = () => { this.style.transform = 'translateY(-10px)'; }; " +
                "this.onmouseout = () => { this.style.transform = 'translateY(0)'; };");

        return card;
    }

    private HorizontalLayout createInfoRow(String iconName, String text) {
        HorizontalLayout row = new HorizontalLayout();
        row.setAlignItems(Alignment.CENTER);
        row.setSpacing(true);
        row.getStyle().set("margin-bottom", "10px");

        Image icon = new Image(ICON_PATH + iconName, "");
        icon.setWidth("20px"); icon.setHeight("20px");

        Span infoText = new Span(text);
        infoText.getStyle().set("color", BRAND_BLUE).set("font-size", "0.95em").set("font-weight", "500");

        row.add(icon, infoText);
        return row;
    }
}