package com.event.event_reservation.view.client;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.*;
import com.event.event_reservation.config.NavigationManager;
import com.event.event_reservation.config.VaadinSession;
import com.event.event_reservation.entity.Event;
import com.event.event_reservation.entity.Reservation;
import com.event.event_reservation.entity.User;
import com.event.event_reservation.repository.EventRepository;
import com.event.event_reservation.service.ReservationService;
import com.event.event_reservation.view.components.VaadinAppLayout;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

@Route(value = "reserve", layout = VaadinAppLayout.class)
@PageTitle("Réservation - Event Reservation")
public class ReservationFormView extends VerticalLayout implements HasUrlParameter<Long> {

    private final EventRepository eventRepository;
    private final ReservationService reservationService;
    private final String BRAND_BLUE = "#253366";
    private final String ICON_PATH = "images/events/icons/";

    private Event event;
    private User currentUser;

    private IntegerField placesField;
    private TextArea commentField;
    private Span totalAmount;

    @Autowired
    public ReservationFormView(EventRepository eventRepository, ReservationService reservationService) {
        this.eventRepository = eventRepository;
        this.reservationService = reservationService;
        this.currentUser = VaadinSession.getCurrentUser();

        if (currentUser == null) {
            UI.getCurrent().navigate("login");
            return;
        }

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().set("background-color", "#f8f9fa");
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, Long eventId) {
        if (eventId == null) {
            UI.getCurrent().navigate("events");
            return;
        }
        loadEvent(eventId);
    }

    private void loadEvent(Long eventId) {
        Optional<Event> eventOptional = eventRepository.findById(eventId);
        if (eventOptional.isEmpty()) {
            UI.getCurrent().navigate("events");
            return;
        }
        this.event = eventOptional.get();
        createContent();
    }

    private void createContent() {
        removeAll();

        // Container centré
        VerticalLayout container = new VerticalLayout();
        container.setWidthFull();
        container.setMaxWidth("900px");
        container.getStyle().set("margin", "40px auto");
        container.setPadding(true);

        // 1. HEADER
        HorizontalLayout header = new HorizontalLayout();
        header.setAlignItems(Alignment.CENTER);
        header.getStyle().set("margin-bottom", "30px");

        Image ticketIcon = new Image(ICON_PATH + "ticket.svg", "");
        ticketIcon.setWidth("45px");

        H1 title = new H1("Finaliser votre réservation");
        title.getStyle()
                .set("color", BRAND_BLUE)
                .set("font-weight", "800")
                .set("margin", "0")
                .set("font-size", "2.2em");

        header.add(ticketIcon, title);

        // 2. RÉCAPITULATIF ÉVÉNEMENT (CARTE 1)
        VerticalLayout eventCard = createStyledCard();
        H2 eventTitre = new H2(event.getTitre());
        eventTitre.getStyle().set("color", BRAND_BLUE).set("margin", "0 0 20px 0");

        Div infoGrid = new Div();
        infoGrid.getStyle().set("display", "grid").set("grid-template-columns", "repeat(auto-fit, minmax(200px, 1fr))").set("gap", "20px");
        infoGrid.add(
                createMiniInfo("date_time.svg", event.getDateDebut().format(DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy", Locale.FRENCH))),
                createMiniInfo("clock_hour.svg", event.getDateDebut().format(DateTimeFormatter.ofPattern("HH:mm"))),
                createMiniInfo("map.svg", event.getVille()),
                createMiniInfo("argent.svg", event.getPrixUnitaire() + " MAD par place")
        );
        eventCard.add(eventTitre, infoGrid);

        // 3. FORMULAIRE DE RÉSERVATION (CARTE 2)
        VerticalLayout formCard = createStyledCard();

        Span formTitle = new Span("Détails de la réservation");
        formTitle.getStyle().set("color", BRAND_BLUE).set("font-weight", "700").set("text-transform", "uppercase").set("font-size", "0.9em");

        placesField = new IntegerField("Combien de places souhaitez-vous ?");
        placesField.setMin(1);
        placesField.setMax(Math.min(10, event.getCapaciteRestante()));
        placesField.setValue(1);
        placesField.setStepButtonsVisible(true);
        placesField.setWidth("250px");
        placesField.setHelperText("Disponible : " + event.getCapaciteRestante() + " places");
        placesField.addValueChangeListener(e -> updateTotal());

        commentField = new TextArea("Commentaire (optionnel)");
        commentField.setPlaceholder("Une précision à apporter ?");
        commentField.setWidthFull();
        commentField.setMaxLength(300);

        formCard.add(formTitle, placesField, commentCardSpacing(), commentField);

        // 4. RÉCAPITULATIF PRIX & ACTION (CARTE 3)
        VerticalLayout summaryCard = createStyledCard();
        summaryCard.getStyle().set("background-color", BRAND_BLUE).set("color", "white");

        HorizontalLayout priceRow = new HorizontalLayout();
        priceRow.setWidthFull();
        priceRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        priceRow.setAlignItems(Alignment.CENTER);

        Span totalLabel = new Span("TOTAL À PAYER");
        totalLabel.getStyle().set("font-weight", "600").set("font-size", "1.1em").set("opacity", "0.9");

        totalAmount = new Span(calculateTotal() + " MAD");
        totalAmount.getStyle().set("font-size", "2.2em").set("font-weight", "800");

        priceRow.add(totalLabel, totalAmount);

        Button confirmBtn = new Button("Confirmer la réservation");
        confirmBtn.setWidthFull();
        confirmBtn.getStyle()
                .set("background-color", "white")
                .set("color", BRAND_BLUE)
                .set("height", "65px")
                .set("font-weight", "800")
                .set("font-size", "1.3em")
                .set("margin-top", "20px")
                .set("cursor", "pointer");

        confirmBtn.addClickListener(e -> handleSubmit());

        summaryCard.add(priceRow, confirmBtn);

        container.add(header, eventCard, formCard, summaryCard);
        add(container);
    }

    private HorizontalLayout createMiniInfo(String iconName, String text) {
        HorizontalLayout row = new HorizontalLayout();
        row.setAlignItems(Alignment.CENTER);
        Image img = new Image(ICON_PATH + iconName, "");
        img.setWidth("22px");
        Span s = new Span(text);
        s.getStyle().set("color", "#555").set("font-weight", "500");
        row.add(img, s);
        return row;
    }

    private VerticalLayout createStyledCard() {
        VerticalLayout card = new VerticalLayout();
        card.setPadding(true);
        card.getStyle()
                .set("background-color", "white")
                .set("border-radius", "25px")
                .set("padding", "35px")
                .set("box-shadow", "0 10px 30px rgba(0,0,0,0.06)")
                .set("margin-bottom", "20px");
        return card;
    }

    private Div commentCardSpacing() {
        Div d = new Div(); d.setHeight("10px"); return d;
    }

    private void updateTotal() {
        if (totalAmount != null) totalAmount.setText(calculateTotal() + " MAD");
    }

    private String calculateTotal() {
        if (placesField == null || placesField.getValue() == null) return "0.00";
        BigDecimal total = event.getPrixUnitaire().multiply(new BigDecimal(placesField.getValue()));
        return String.format(Locale.US, "%.2f", total);
    }

    private void handleSubmit() {
        try {
            Reservation res = reservationService.createReservation(currentUser.getId(), event.getId(), placesField.getValue());
            if (!commentField.getValue().isEmpty()) {
                res.setCommentaire(commentField.getValue());
            }
            reservationService.confirmReservation(res.getId());
            showSuccessLayout(res);
        } catch (Exception e) {
            Notification n = Notification.show("Erreur : " + e.getMessage(), 4000, Notification.Position.TOP_CENTER);
            n.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void showSuccessLayout(Reservation res) {
        removeAll();
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        layout.setAlignItems(Alignment.CENTER);

        VerticalLayout card = createStyledCard();
        card.setMaxWidth("550px");
        card.setAlignItems(Alignment.CENTER);
        card.setSpacing(true);

        Image successIcon = new Image(ICON_PATH + "ticket.svg", "");
        successIcon.setWidth("90px");

        H2 successTitle = new H2("Réservation Validée !");
        successTitle.getStyle().set("color", "#10b981").set("margin", "10px 0");

        Span info = new Span("Présentez ce code à l'entrée de l'événement :");
        info.getStyle().set("color", "#666");

        Span code = new Span(res.getCodeReservation());
        code.getStyle()
                .set("font-size", "2.5em")
                .set("font-weight", "900")
                .set("color", BRAND_BLUE)
                .set("background", "#f1f3f9")
                .set("padding", "15px 40px")
                .set("border-radius", "15px")
                .set("letter-spacing", "2px")
                .set("margin", "20px 0");

        Button btnMyRes = new Button("Voir mes réservations", e -> UI.getCurrent().navigate("my-reservations"));
        btnMyRes.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnMyRes.getStyle().set("background-color", BRAND_BLUE).set("height", "55px").set("width", "100%");

        card.add(successIcon, successTitle, info, code, btnMyRes);
        layout.add(card);
        add(layout);
    }
}