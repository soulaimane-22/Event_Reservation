package com.event.event_reservation.view.publics;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.event.event_reservation.config.NavigationManager;
import com.event.event_reservation.config.VaadinSession;
import com.event.event_reservation.entity.Event;
import com.event.event_reservation.repository.EventRepository;
import com.event.event_reservation.view.components.VaadinAppLayout;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

@Route(value = "event", layout = VaadinAppLayout.class)
@PageTitle("Détails Événement - Event Reservation")
public class EventDetailView extends VerticalLayout implements HasUrlParameter<Long> {

    private final EventRepository eventRepository;
    private final String BRAND_BLUE = "#253366";
    private final String ICON_PATH = "images/events/icons/";

    private Event event;
    private Long eventId;

    @Autowired
    public EventDetailView(EventRepository eventRepository) {
        this.eventRepository = eventRepository;

        // Configuration Plein Écran pour l'image
        setPadding(false);
        setSpacing(false);
        setWidthFull();
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, Long parameter) {
        if (parameter == null) {
            NavigationManager.goToEventList();
            return;
        }
        this.eventId = parameter;
        loadEvent();
    }

    private void loadEvent() {
        Optional<Event> eventOptional = eventRepository.findById(eventId);
        if (eventOptional.isEmpty()) {
            NavigationManager.goToEventList();
            return;
        }
        this.event = eventOptional.get();
        createContent();
    }

    private void createContent() {
        removeAll();

        // 1. IMAGE EDGE-TO-EDGE
        Div heroImage = new Div();
        heroImage.setWidthFull();
        heroImage.setHeight("500px");
        heroImage.getStyle()
                .set("background-image", "url('" + event.getImageUrl() + "')")
                .set("background-size", "cover")
                .set("background-position", "center")
                .set("box-shadow", "inset 0 -100px 100px -50px rgba(0,0,0,0.2)");

        // 2. CONTENEUR DE CONTENU (CENTRÉ)
        VerticalLayout mainContent = new VerticalLayout();
        mainContent.setMaxWidth("1100px");
        mainContent.getStyle().set("margin", "0 auto");
        mainContent.setPadding(true);
        mainContent.getStyle().set("margin-top", "-60px"); // Effet de chevauchement sur l'image

        // Card Blanche pour les infos
        VerticalLayout infoCard = new VerticalLayout();
        infoCard.getStyle()
                .set("background", "white")
                .set("border-radius", "20px")
                .set("padding", "40px")
                .set("box-shadow", "0 15px 35px rgba(0,0,0,0.1)");

        // Titre et Catégorie
        H1 title = new H1(event.getTitre());
        title.getStyle().set("color", BRAND_BLUE).set("margin", "0");

        Span categoryBadge = new Span(event.getCategorie().toString());
        categoryBadge.getStyle()
                .set("background", "#f0f2f5")
                .set("color", BRAND_BLUE)
                .set("padding", "5px 15px")
                .set("border-radius", "5px")
                .set("font-weight", "600");

        HorizontalLayout header = new HorizontalLayout(title, categoryBadge);
        header.setAlignItems(Alignment.CENTER);
        header.setWidthFull();
        header.setFlexGrow(1, title);

        infoCard.add(header, new Hr());

        // Grille d'informations avec SVG
        Div infoGrid = new Div();
        infoGrid.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(auto-fit, minmax(300px, 1fr))")
                .set("gap", "20px")
                .set("width", "100%");

        infoGrid.add(
                createSvgInfoRow("date_time.svg", "Début", event.getDateDebut().format(DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy 'à' HH:mm", Locale.FRENCH))),
                createSvgInfoRow("date_fin.svg", "Fin", event.getDateFin().format(DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy 'à' HH:mm", Locale.FRENCH))),
                createSvgInfoRow("ville.svg", "Ville", event.getVille()),
                createSvgInfoRow("map.svg", "Lieu précis", event.getLieu()),
                createSvgInfoRow("argent.svg", "Tarif", event.getPrixUnitaire() + " MAD"),
                createSvgInfoRow("people.svg", "Disponibilité", event.getCapaciteRestante() + " places sur " + event.getCapaciteMax())
        );

        infoCard.add(infoGrid);

        // Section Description avec icône
        HorizontalLayout descHeader = new HorizontalLayout(
                new Image(ICON_PATH + "description.svg", ""),
                new H2("Description")
        );
        descHeader.setAlignItems(Alignment.CENTER);
        descHeader.getStyle().set("margin-top", "30px");

        Paragraph description = new Paragraph(event.getDescription());
        description.getStyle().set("color", "#444").set("line-height", "1.8").set("font-size", "1.1em");

        // Section Organisateur
        HorizontalLayout orgRow = new HorizontalLayout(
                new Image(ICON_PATH + "organizer.svg", ""),
                new Span("Organisé par : "),
                new Span(event.getOrganisateur().getPrenom() + " " + event.getOrganisateur().getNom())
        );
        orgRow.getStyle().set("font-weight", "600").set("color", BRAND_BLUE).set("margin-top", "20px");
        orgRow.setAlignItems(Alignment.CENTER);

        // Bouton de Réservation
        Button reserveBtn = new Button("Réserver maintenant");
        reserveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        reserveBtn.setWidthFull();
        reserveBtn.getStyle().set("background-color", BRAND_BLUE).set("height", "60px").set("font-size", "1.3em");
        reserveBtn.addClickListener(e -> handleReservation());

        if (event.getCapaciteRestante() <= 0) {
            reserveBtn.setText("Événement Complet");
            reserveBtn.setEnabled(false);
            reserveBtn.getStyle().set("background-color", "#ccc");
        }

        infoCard.add(descHeader, description, orgRow, new Hr(), reserveBtn);

        mainContent.add(infoCard);
        add(heroImage, mainContent);
    }

    /**
     * Crée une ligne d'information propre avec une icône SVG
     */
    private HorizontalLayout createSvgInfoRow(String svgName, String label, String value) {
        HorizontalLayout row = new HorizontalLayout();
        row.setAlignItems(Alignment.CENTER);
        row.setSpacing(true);
        row.getStyle().set("padding", "10px 0");

        Image icon = new Image(ICON_PATH + svgName, "");
        icon.setWidth("28px");
        icon.setHeight("28px");

        VerticalLayout textLayout = new VerticalLayout();
        textLayout.setPadding(false);
        textLayout.setSpacing(false);

        Span labelSpan = new Span(label);
        labelSpan.getStyle().set("font-size", "0.85em").set("color", "#888").set("text-transform", "uppercase");

        Span valueSpan = new Span(value);
        valueSpan.getStyle().set("font-weight", "600").set("color", BRAND_BLUE);

        textLayout.add(labelSpan, valueSpan);
        row.add(icon, textLayout);
        return row;
    }

    private void handleReservation() {
        if (VaadinSession.isUserLoggedIn()) {
            NavigationManager.goToReservationForm(eventId);
        } else {
            Notification n = Notification.show("Veuillez vous connecter pour réserver.", 3000, Notification.Position.TOP_CENTER);
            n.addThemeVariants(NotificationVariant.LUMO_PRIMARY);
            UI.getCurrent().navigate("login");
        }
    }
}