package com.event.event_reservation.view.admin;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.router.*;
import com.event.event_reservation.entity.Event;
import com.event.event_reservation.entity.enums.EventStatus;
import com.event.event_reservation.entity.enums.UserRole;
import com.event.event_reservation.repository.EventRepository;
import com.event.event_reservation.service.EventService;
import com.event.event_reservation.config.*;
import com.event.event_reservation.view.components.VaadinAppLayout;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "admin/events", layout = VaadinAppLayout.class)
@PageTitle("Admin - Événements")
public class AllEventsManagementView extends VerticalLayout {

    private final EventRepository eventRepository;
    private final EventService eventService;
    private final Grid<Event> grid = new Grid<>(Event.class, false);

    @Autowired
    public AllEventsManagementView(EventRepository eventRepository, EventService eventService) {
        this.eventRepository = eventRepository;
        this.eventService = eventService;

        if (VaadinSession.getCurrentUser() == null ||
                VaadinSession.getCurrentUser().getRole() != UserRole.ADMIN) {
            NavigationManager.goToHome();
            return;
        }

        setSizeFull();
        add(new H1("📅 Gestion des Événements"));

        configureGrid();
        grid.setItems(eventRepository.findAll());
        add(grid);
    }

    private void configureGrid() {
        grid.addColumn(Event::getTitre).setHeader("Titre");
        grid.addColumn(e -> e.getOrganisateur().getEmail()).setHeader("Organisateur");
        grid.addColumn(Event::getVille).setHeader("Ville");
        grid.addColumn(Event::getStatut).setHeader("Statut");
        grid.addColumn(e -> e.getCapaciteRestante() + "/" + e.getCapaciteMax()).setHeader("Places");

        grid.addComponentColumn(event -> {
            Button publish = new Button("Publier",
                    e -> eventService.publishEvent(
                            VaadinSession.getCurrentUser().getId(), event.getId()));

            Button cancel = new Button("Annuler",
                    e -> eventService.cancelEvent(
                            VaadinSession.getCurrentUser().getId(), event.getId(), "ADMIN"));

            Button delete = new Button("Supprimer",
                    e -> eventService.deleteEvent(
                            VaadinSession.getCurrentUser().getId(), event.getId()));

            return new HorizontalLayout(publish, cancel, delete);
        }).setHeader("Actions");
    }
}
