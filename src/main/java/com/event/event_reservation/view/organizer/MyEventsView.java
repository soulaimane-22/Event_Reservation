package com.event.event_reservation.view.organizer;

import com.event.event_reservation.config.NavigationManager;
import com.event.event_reservation.config.VaadinSession;
import com.event.event_reservation.entity.Event;
import com.event.event_reservation.entity.User;
import com.event.event_reservation.entity.enums.EventStatus;
import com.event.event_reservation.entity.enums.UserRole;
import com.event.event_reservation.repository.EventRepository;
import com.event.event_reservation.view.components.VaadinAppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.VaadinIcon;
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

    @Autowired
    public MyEventsView(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
        this.currentUser = VaadinSession.getCurrentUser();

        if (currentUser == null || currentUser.getRole() == UserRole.CLIENT) {
            NavigationManager.goToLogin();
            return;
        }

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        createHeader();
        createGrid();
    }

    private void createHeader() {
        H1 title = new H1("📋 Mes Événements");

        Button createBtn = new Button("Créer un événement", VaadinIcon.PLUS.create());
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createBtn.addClickListener(e -> NavigationManager.goToCreateEvent());

        add(title, createBtn);
    }

    private void createGrid() {
        List<Event> events = eventRepository.findByOrganisateurId(currentUser.getId());

        Grid<Event> grid = new Grid<>(Event.class, false);
        grid.setSizeFull();

        grid.addColumn(Event::getTitre).setHeader("Titre").setFlexGrow(1);
        grid.addColumn(e -> e.getDateDebut().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .setHeader("Date");
        grid.addColumn(Event::getStatut).setHeader("Statut");

        grid.addComponentColumn(event -> {
            Button edit = new Button(VaadinIcon.EDIT.create());
            edit.addClickListener(e ->
                    NavigationManager.goToEditEvent(event.getId())
            );
            edit.setEnabled(event.getStatut() != EventStatus.TERMINE);
            return edit;
        }).setHeader("Modifier");

        grid.addComponentColumn(event -> {
            Button reservations = new Button(VaadinIcon.TICKET.create());
            reservations.addClickListener(e ->
                    NavigationManager.goToEventReservations(event.getId())
            );
            return reservations;
        }).setHeader("Réservations");

        grid.setItems(events);
        add(grid);
    }
}
