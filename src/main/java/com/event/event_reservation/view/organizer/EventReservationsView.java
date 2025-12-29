package com.event.event_reservation.view.organizer;

import com.event.event_reservation.config.NavigationManager;
import com.event.event_reservation.config.VaadinSession;
import com.event.event_reservation.entity.Reservation;
import com.event.event_reservation.entity.User;
import com.event.event_reservation.entity.enums.ReservationStatus;
import com.event.event_reservation.entity.enums.UserRole;
import com.event.event_reservation.repository.ReservationRepository;
import com.event.event_reservation.view.components.VaadinAppLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.StreamResource;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Route(value = "organizer/event/:id/reservations", layout = VaadinAppLayout.class)
@PageTitle("Réservations Événement - Organizer")
public class EventReservationsView extends VerticalLayout implements BeforeEnterObserver {

    private final ReservationRepository reservationRepository;
    private final String BRAND_BLUE = "#253366";
    private final String ICON_PATH = "images/events/icons/";

    private Long eventId;
    private final VerticalLayout container = new VerticalLayout();
    private Grid<Reservation> grid;

    @Autowired
    public EventReservationsView(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().set("background-color", "#f8f9fa");

        container.setWidthFull();
        container.setMaxWidth("1250px");
        container.setPadding(true);
        container.getStyle().set("margin", "0 auto");
        add(container);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        User currentUser = VaadinSession.getCurrentUser();
        if (currentUser == null || (currentUser.getRole() != UserRole.ORGANIZER && currentUser.getRole() != UserRole.ADMIN)) {
            event.forwardTo("login");
            return;
        }

        eventId = Long.valueOf(event.getRouteParameters().get("id").get());

        container.removeAll();
        createHeader();
        createGridContainer();
        loadReservations();
    }

    private void createHeader() {
        HorizontalLayout headerRow = new HorizontalLayout();
        headerRow.setWidthFull();
        headerRow.setAlignItems(Alignment.CENTER);
        headerRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        headerRow.getStyle().set("margin", "30px 0");

        // --- GAUCHE : TITRE ET ICONE ---
        HorizontalLayout titleGroup = new HorizontalLayout();
        titleGroup.setAlignItems(Alignment.CENTER);

        Image ticketIcon = new Image(ICON_PATH + "ticket.svg", "");
        ticketIcon.setWidth("45px");

        VerticalLayout titles = new VerticalLayout();
        titles.setPadding(false); titles.setSpacing(false);
        H1 title = new H1("Liste des Réservations");
        title.getStyle().set("color", BRAND_BLUE).set("font-weight", "800").set("margin", "0").set("font-size", "2.5em");
        Span subtitle = new Span("Consultez les participants et exportez les données");
        subtitle.getStyle().set("color", "#666").set("font-size", "1.1em");
        titles.add(title, subtitle);

        titleGroup.add(ticketIcon, titles);

        // --- DROITE : BOUTON EXPORT CSV (ICON ONLY) ---
        Anchor downloadLink = createCsvDownloadLink();

        headerRow.add(titleGroup, downloadLink);
        container.add(headerRow);
    }

    private Anchor createCsvDownloadLink() {
        // Icône Export CSV
        Image csvIcon = new Image(ICON_PATH + "telech.svg", "Export CSV");
        csvIcon.setWidth("24px");
        csvIcon.setHeight("24px");

        // Bouton sans texte
        Button exportBtn = new Button(csvIcon);
        exportBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        exportBtn.getStyle()
                .set("background-color", "white")
                .set("width", "50px")  // Carré
                .set("height", "50px") // Carré
                .set("cursor", "pointer")
                .set("border-radius", "12px");

        // Création de la ressource CSV
        StreamResource resource = new StreamResource("export_reservations_" + eventId + ".csv", () -> {
            List<Reservation> data = reservationRepository.findByEvenementId(eventId);
            return new ByteArrayInputStream(generateCsvContent(data).getBytes(StandardCharsets.UTF_8));
        });

        Anchor anchor = new Anchor(resource, "");
        anchor.getElement().setAttribute("download", true);
        anchor.add(exportBtn);
        return anchor;
    }

    private String generateCsvContent(List<Reservation> reservations) {
        StringBuilder csv = new StringBuilder();
        csv.append("Code Reservation;Participant;Email;Places;Montant (MAD);Statut\n");
        for (Reservation r : reservations) {
            csv.append(r.getCodeReservation()).append(";")
                    .append(r.getUtilisateur().getPrenom()).append(" ").append(r.getUtilisateur().getNom()).append(";")
                    .append(r.getUtilisateur().getEmail()).append(";")
                    .append(r.getNombrePlaces()).append(";")
                    .append(r.getMontantTotal()).append(";")
                    .append(r.getStatut().toString()).append("\n");
        }
        return csv.toString();
    }

    private void createGridContainer() {
        grid = new Grid<>(Reservation.class, false);
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);

        grid.getStyle()
                .set("background-color", "white")
                .set("border-radius", "20px")
                .set("box-shadow", "0 10px 30px rgba(0,0,0,0.05)")
                .set("overflow", "hidden");
        grid.setHeight("650px");

        grid.addColumn(Reservation::getCodeReservation).setHeader("CODE").setAutoWidth(true).setSortable(true);
        grid.addColumn(r -> r.getUtilisateur().getPrenom() + " " + r.getUtilisateur().getNom()).setHeader("PARTICIPANT").setFlexGrow(1).setSortable(true);
        grid.addColumn(r -> r.getUtilisateur().getEmail()).setHeader("EMAIL").setAutoWidth(true);
        grid.addColumn(Reservation::getNombrePlaces).setHeader("PLACES").setTextAlign(ColumnTextAlign.CENTER).setAutoWidth(true);
        grid.addColumn(r -> r.getMontantTotal() + " MAD").setHeader("MONTANT").setAutoWidth(true);
        grid.addComponentColumn(this::createStatusBadge).setHeader("STATUT").setAutoWidth(true);

        container.add(grid);
    }

    private Span createStatusBadge(Reservation reservation) {
        Span badge = new Span(reservation.getStatut().toString());
        var s = badge.getStyle();
        s.set("padding", "5px 12px").set("border-radius", "20px").set("font-size", "0.75em").set("font-weight", "bold").set("color", "white");

        String color = switch (reservation.getStatut()) {
            case CONFIRMEE -> "#10b981";
            case EN_ATTENTE -> "#f59e0b";
            case ANNULEE -> "#ef4444";
            default -> "#6b7280";
        };
        s.set("background-color", color);
        return badge;
    }

    private void loadReservations() {
        List<Reservation> reservations = reservationRepository.findByEvenementId(eventId);
        grid.setItems(reservations);
    }
}