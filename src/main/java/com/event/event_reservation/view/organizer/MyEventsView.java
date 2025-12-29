package com.event.event_reservation.view.organizer;

import com.event.event_reservation.config.NavigationManager;
import com.event.event_reservation.config.VaadinSession;
import com.event.event_reservation.entity.Event;
import com.event.event_reservation.entity.User;
import com.event.event_reservation.entity.enums.EventStatus;
import com.event.event_reservation.entity.enums.UserRole;
import com.event.event_reservation.repository.EventRepository;
import com.event.event_reservation.view.components.VaadinAppLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Route(value = "organizer/events", layout = VaadinAppLayout.class)
@PageTitle("Mes Événements - Organizer")
public class MyEventsView extends VerticalLayout {

    private final EventRepository eventRepository;
    private final User currentUser;
    private final String BRAND_BLUE = "#253366";
    private final String ICON_PATH = "images/events/icons/";

    @Autowired
    public MyEventsView(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
        this.currentUser = VaadinSession.getCurrentUser();

        if (currentUser == null || currentUser.getRole() == UserRole.CLIENT) {
            UI.getCurrent().navigate("login");
            return;
        }

        // Configuration Layout
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().set("background-color", "#f8f9fa");

        // Container centré
        VerticalLayout container = new VerticalLayout();
        container.setWidthFull();
        container.setMaxWidth("1250px");
        container.setPadding(true);
        container.getStyle().set("margin", "0 auto");

        createHeader(container);
        createGrid(container);

        add(container);
    }

    private void createHeader(VerticalLayout container) {
        HorizontalLayout headerRow = new HorizontalLayout();
        headerRow.setWidthFull();
        headerRow.setAlignItems(Alignment.CENTER);
        headerRow.setJustifyContentMode(JustifyContentMode.BETWEEN);
        headerRow.getStyle().set("margin", "30px 0");

        H1 title = new H1("Mes Événements");
        title.getStyle()
                .set("color", BRAND_BLUE)
                .set("font-weight", "800")
                .set("margin", "0")
                .set("font-size", "2.5em");

        // --- BOUTON CRÉER AVEC ICÔNE creerevent.svg ---
        Image createIcon = new Image(ICON_PATH + "creerevent.svg", "");
        createIcon.setWidth("20px");
        createIcon.getStyle().set("margin-right", "8px");

        Button createBtn = new Button("Créer un événement", createIcon);
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createBtn.getStyle()
                .set("background-color", BRAND_BLUE)
                .set("height", "50px")
                .set("padding", "0 25px")
                .set("font-weight", "700");
        createBtn.addClickListener(e -> NavigationManager.goToCreateEvent());

        headerRow.add(title, createBtn);
        container.add(headerRow);
    }

    private void createGrid(VerticalLayout container) {
        List<Event> events = eventRepository.findByOrganisateurId(currentUser.getId());

        Grid<Event> grid = new Grid<>(Event.class, false);
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        grid.getStyle()
                .set("background-color", "white")
                .set("border-radius", "20px")
                .set("box-shadow", "0 10px 30px rgba(0,0,0,0.05)")
                .set("overflow", "hidden");
        grid.setHeight("650px");

        // Colonne Événement
        grid.addColumn(Event::getTitre)
                .setHeader("ÉVÉNEMENT")
                .setSortable(true)
                .setFlexGrow(1);

        // Colonne Date
        grid.addColumn(e -> e.getDateDebut().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                .setHeader("DATE")
                .setSortable(true)
                .setAutoWidth(true);

        // Colonne Statut avec Badge
        grid.addComponentColumn(this::createStatusBadge)
                .setHeader("STATUT")
                .setAutoWidth(true);

        // Colonne Remplissage
        grid.addColumn(e -> (e.getCapaciteMax() - e.getCapaciteRestante()) + " / " + e.getCapaciteMax())
                .setHeader("RÉSERVATIONS")
                .setAutoWidth(true);

        // --- COLONNE MODIFIER (SVG modifier.svg) ---
        grid.addComponentColumn(event -> {
            Image editIcon = new Image(ICON_PATH + "modifier.svg", "Modifier");
            editIcon.setWidth("22px");

            Button editBtn = new Button(editIcon);
            editBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            editBtn.getStyle().set("cursor", "pointer");
            editBtn.addClickListener(e -> NavigationManager.goToEditEvent(event.getId()));

            if (event.getStatut() == EventStatus.TERMINE) {
                editBtn.setEnabled(false);
                editBtn.getStyle().set("opacity", "0.3");
            }

            return editBtn;
        }).setHeader("MODIFIER").setAutoWidth(true);

        // --- COLONNE VOIR RÉSERVATIONS (SVG ticket.svg) ---
        grid.addComponentColumn(event -> {
            Image ticketIcon = new Image(ICON_PATH + "ticket.svg", "Tickets");
            ticketIcon.setWidth("22px");

            Button resBtn = new Button(ticketIcon);
            resBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            resBtn.getStyle().set("cursor", "pointer");
            resBtn.addClickListener(e -> NavigationManager.goToEventReservations(event.getId()));

            return resBtn;
        }).setHeader("RESERVATIONS").setAutoWidth(true);

        grid.setItems(events);
        container.add(grid);
    }

    private Span createStatusBadge(Event event) {
        Span badge = new Span(event.getStatut().toString());
        var s = badge.getStyle();
        s.set("padding", "5px 12px")
                .set("border-radius", "20px")
                .set("font-size", "0.75em")
                .set("font-weight", "bold")
                .set("color", "white");

        String color = switch (event.getStatut()) {
            case PUBLIE -> "#10b981";
            case BROUILLON -> "#6b7280";
            case ANNULE -> "#ef4444";
            case TERMINE -> "#3b82f6";
        };
        s.set("background-color", color);
        return badge;
    }
}