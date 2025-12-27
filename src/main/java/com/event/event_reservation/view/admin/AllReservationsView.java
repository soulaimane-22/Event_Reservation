package com.event.event_reservation.view.admin;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.router.*;
import com.event.event_reservation.entity.Reservation;
import com.event.event_reservation.entity.enums.UserRole;
import com.event.event_reservation.repository.ReservationRepository;
import com.event.event_reservation.config.*;
import com.event.event_reservation.view.components.VaadinAppLayout;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.stream.Collectors;

@Route(value = "admin/reservations", layout = VaadinAppLayout.class)
@PageTitle("Admin - Réservations")
public class AllReservationsView extends VerticalLayout {

    private final ReservationRepository reservationRepository;

    @Autowired
    public AllReservationsView(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;

        if (VaadinSession.getCurrentUser() == null ||
                VaadinSession.getCurrentUser().getRole() != UserRole.ADMIN) {
            NavigationManager.goToHome();
            return;
        }

        setSizeFull();
        add(new H1("🎫 Gestion des Réservations"));

        Button export = new Button("Exporter CSV", e -> exportCSV());
        add(export);

        Grid<Reservation> grid = new Grid<>(Reservation.class, false);
        grid.addColumn(Reservation::getCodeReservation).setHeader("Code");
        grid.addColumn(r -> r.getUtilisateur().getEmail()).setHeader("Utilisateur");
        grid.addColumn(r -> r.getEvenement().getTitre()).setHeader("Événement");
        grid.addColumn(Reservation::getMontantTotal).setHeader("Montant");
        grid.addColumn(Reservation::getStatut).setHeader("Statut");

        grid.setItems(reservationRepository.findAll());
        add(grid);
    }

    private void exportCSV() {
        String csv = reservationRepository.findAll().stream()
                .map(r -> r.getCodeReservation() + ";" +
                        r.getUtilisateur().getEmail() + ";" +
                        r.getEvenement().getTitre() + ";" +
                        r.getMontantTotal())
                .collect(Collectors.joining("\n"));

        UI.getCurrent().getPage().executeJs(
                "const a=document.createElement('a');" +
                        "a.href='data:text/csv;charset=utf-8," + csv + "';" +
                        "a.download='reservations.csv';a.click();");
    }
}
