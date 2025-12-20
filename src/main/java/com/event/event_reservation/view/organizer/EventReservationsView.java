package com.event.event_reservation.view.organizer;

import com.event.event_reservation.config.NavigationManager;
import com.event.event_reservation.config.VaadinSession;
import com.event.event_reservation.entity.Reservation;
import com.event.event_reservation.entity.User;
import com.event.event_reservation.entity.enums.ReservationStatus;
import com.event.event_reservation.entity.enums.UserRole;
import com.event.event_reservation.repository.ReservationRepository;
import com.event.event_reservation.view.components.VaadinAppLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Route(value = "organizer/event/:id/reservations", layout = VaadinAppLayout.class)
@PageTitle("Réservations Événement")
public class EventReservationsView extends VerticalLayout implements BeforeEnterObserver {

    private final ReservationRepository reservationRepository;
    private final User currentUser;
    private Long eventId;

    @Autowired
    public EventReservationsView(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
        this.currentUser = VaadinSession.getCurrentUser();

        if (currentUser == null ||
                (currentUser.getRole() != UserRole.ORGANIZER && currentUser.getRole() != UserRole.ADMIN)) {
            NavigationManager.goToLogin();
            return;
        }

        setSizeFull();
        setPadding(true);
        setSpacing(true);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        eventId = Long.valueOf(event.getRouteParameters().get("id").get());
        loadReservations();
    }

    private void loadReservations() {
        List<Reservation> reservations =
                reservationRepository.findByEvenementIdAndStatut(eventId, ReservationStatus.CONFIRMEE);

        Grid<Reservation> grid = new Grid<>(Reservation.class, false);
        grid.setSizeFull();

        grid.addColumn(Reservation::getCodeReservation).setHeader("Code");
        grid.addColumn(r -> r.getUtilisateur().getEmail()).setHeader("Utilisateur");
        grid.addColumn(Reservation::getNombrePlaces).setHeader("Places");
        grid.addColumn(Reservation::getMontantTotal).setHeader("Montant");
        grid.addColumn(Reservation::getStatut).setHeader("Statut");

        grid.setItems(reservations);

        add(new H1("🎫 Réservations"), grid);
    }
}
