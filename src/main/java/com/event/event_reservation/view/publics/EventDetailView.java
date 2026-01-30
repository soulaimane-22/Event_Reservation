package com.event.event_reservation.view.publics;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.router.*;
import com.event.event_reservation.config.NavigationManager;
import com.event.event_reservation.config.VaadinSession;
import com.event.event_reservation.entity.Event;
import com.event.event_reservation.repository.EventRepository;
import com.event.event_reservation.view.client.ReservationFormView;
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

        // CONFIGURATION PLEIN ÉCRAN TOTAL (Supprime tout l'espace blanc)
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        setMargin(false);
        getStyle().set("background-color", "white");
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

        // 1. CONTENEUR SPLIT (H: 100vh pour forcer l'affichage)
        HorizontalLayout mainSplit = new HorizontalLayout();
        mainSplit.setWidthFull();
        mainSplit.setHeight("100vh"); // Occupe toute la hauteur de l'écran
        mainSplit.setSpacing(false);
        mainSplit.setPadding(false);
        mainSplit.setMargin(false);

        // 2. COLONNE GAUCHE : IMAGE FORCÉE
        Div imageSide = new Div();
        imageSide.setWidth("50%");
        imageSide.setHeightFull();
        imageSide.getStyle().set("overflow", "hidden");

        // Utilisation d'un composant Image pour garantir la visibilité
        String url = (event.getImageUrl() != null) ? event.getImageUrl() : "images/events/default-event.jpg";
        Image eventImg = new Image(url, event.getTitre());
        eventImg.setWidth("100%");
        eventImg.setHeight("100%");
        eventImg.getStyle().set("object-fit", "cover"); // Remplit sans déformer

        imageSide.add(eventImg);

        // 3. COLONNE DROITE : CONTENU SCROLLABLE
        VerticalLayout infoSide = new VerticalLayout();
        infoSide.setWidth("50%");
        infoSide.setHeightFull();
        infoSide.setPadding(false);
        infoSide.setSpacing(false);

        // Zone de texte interne
        VerticalLayout scrollContent = new VerticalLayout();
        scrollContent.setPadding(true);
        scrollContent.getStyle().set("padding", "60px");
        scrollContent.setSpacing(true);

        // Catégorie
        Span category = new Span(event.getCategorie().toString());
        category.getStyle()
                .set("background", "#F1F3F9")
                .set("color", BRAND_BLUE)
                .set("padding", "8px 20px")
                .set("border-radius", "50px")
                .set("font-weight", "800")
                .set("font-size", "0.8em");

        // Titre
        H1 title = new H1(event.getTitre());
        title.getStyle()
                .set("color", BRAND_BLUE)
                .set("font-weight", "900")
                .set("font-size", "3.2em")
                .set("margin", "10px 0");

        // Grille d'infos avec icônes
        Div infoGrid = new Div();
        infoGrid.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "1fr 1fr")
                .set("gap", "30px")
                .set("width", "100%")
                .set("margin", "40px 0");

        infoGrid.add(
                createSvgInfoRow("date_time.svg", "Date", new Span(event.getDateDebut().format(DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy", Locale.FRENCH)))),
                createSvgInfoRow("clock_hour.svg", "Heure", new Span(event.getDateDebut().format(DateTimeFormatter.ofPattern("HH:mm")))),
                createSvgInfoRow("ville.svg", "Ville", new Span(event.getVille())),
                createSvgInfoRow("map.svg", "Lieu", createLocationComponent()),
                createSvgInfoRow("argent.svg", "Tarif", new Span(event.getPrixUnitaire() + " MAD")),
                createSvgInfoRow("people.svg", "Places", new Span(event.getCapaciteRestante() + " / " + event.getCapaciteMax()))
        );

        // Description
        H2 descTitle = new H2("À propos de l'événement");
        descTitle.getStyle().set("color", BRAND_BLUE).set("font-weight", "800");

        Paragraph description = new Paragraph(event.getDescription());
        description.getStyle().set("color", "#444").set("line-height", "1.8").set("font-size", "1.1em");

        // Organisateur
        HorizontalLayout orgRow = new HorizontalLayout(
                new Image(ICON_PATH + "organizer.svg", ""),
                new Span("Organisé par : " + event.getOrganisateur().getPrenom() + " " + event.getOrganisateur().getNom())
        );
        orgRow.setAlignItems(Alignment.CENTER);
        orgRow.getStyle().set("color", "#777").set("font-weight", "600").set("margin-top", "20px");
        orgRow.getChildren().filter(c -> c instanceof Image).forEach(i -> ((Image)i).setWidth("22px"));

        scrollContent.add(category, title, new Hr(), infoGrid, descTitle, description, orgRow);

        // Scroller pour le texte à droite
        Scroller scroller = new Scroller(scrollContent);
        scroller.setSizeFull();

        // 4. BOUTON DE RÉSERVATION (TOUJOURS EN BAS)
        Button reserveBtn = new Button("Réserver mes places");
        reserveBtn.setWidthFull();
        reserveBtn.getStyle()
                .set("background-color", BRAND_BLUE)
                .set("color", "white")
                .set("height", "80px")
                .set("font-weight", "900")
                .set("font-size", "1.5em")
                .set("border-radius", "0")
                .set("cursor", "pointer");

        reserveBtn.addClickListener(e -> handleReservation());

        if (event.getCapaciteRestante() <= 0) {
            reserveBtn.setText("Événement Complet");
            reserveBtn.setEnabled(false);
            reserveBtn.getStyle().set("background-color", "#A0A0A0");
        }

        infoSide.add(scroller, reserveBtn);

        // ASSEMBLAGE FINAL
        mainSplit.add(imageSide, infoSide);
        add(mainSplit);
    }

    private HorizontalLayout createLocationComponent() {
        HorizontalLayout layout = new HorizontalLayout(new Span(event.getLieu()));
        layout.setAlignItems(Alignment.CENTER);
        if (event.getLatitude() != null && event.getLongitude() != null) {
            String mapUrl = "https://www.google.com/maps/search/?api=1&query=" + event.getLatitude() + "," + event.getLongitude();
            Image mapsIcon = new Image(ICON_PATH + "googlemaps.svg", "Maps");
            mapsIcon.setWidth("22px");
            Anchor mapsLink = new Anchor(mapUrl, mapsIcon);
            mapsLink.setTarget("_blank");
            layout.add(mapsLink);
        }
        return layout;
    }

    private HorizontalLayout createSvgInfoRow(String svgName, String label, com.vaadin.flow.component.Component valueComponent) {
        HorizontalLayout row = new HorizontalLayout();
        row.setAlignItems(Alignment.CENTER);
        Image icon = new Image(ICON_PATH + svgName, "");
        icon.setWidth("32px");
        VerticalLayout textLayout = new VerticalLayout();
        textLayout.setPadding(false); textLayout.setSpacing(false);
        Span labelSpan = new Span(label);
        labelSpan.getStyle().set("font-size", "0.75em").set("color", "#999").set("text-transform", "uppercase").set("letter-spacing", "1px");
        valueComponent.getStyle().set("font-weight", "700").set("color", BRAND_BLUE).set("font-size", "1em");
        textLayout.add(labelSpan, valueComponent);
        row.add(icon, textLayout);
        return row;
    }

    private void handleReservation() {
        if (VaadinSession.isUserLoggedIn()) {
            UI.getCurrent().navigate(ReservationFormView.class, eventId);
        } else {
            Notification.show("Veuillez vous connecter pour réserver.", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_PRIMARY);
            UI.getCurrent().navigate("login");
        }
    }
}