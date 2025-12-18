package com.event.event_reservation.view.client;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
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
import java.util.Optional;

/**
 * Formulaire de réservation
 * URL: /event/{id}/reserve
 */
@Route(value = "reserve", layout = VaadinAppLayout.class)
@PageTitle("Réserver - Event Reservation")
public class ReservationFormView extends VerticalLayout implements HasUrlParameter<Long> {

    private final EventRepository eventRepository;
    private final ReservationService reservationService;

    private Event event;
    private User currentUser;

    private IntegerField placesField;
    private TextArea commentField;
    private Span totalAmount;
    private Button submitBtn;

    @Autowired
    public ReservationFormView(EventRepository eventRepository, ReservationService reservationService) {
        this.eventRepository = eventRepository;
        this.reservationService = reservationService;

        currentUser = VaadinSession.getCurrentUser();
        if (currentUser == null) {
            NavigationManager.goToLogin();
            return;
        }

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        setMaxWidth("800px");
        getStyle().set("margin", "0 auto");
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, Long eventId) {
        if (eventId == null) {
            showError("Événement non trouvé");
            NavigationManager.goToEventList();
            return;
        }

        loadEvent(eventId);
    }

    /**
     * Charger l'événement
     */
    private void loadEvent(Long eventId) {
        Optional<Event> eventOptional = eventRepository.findById(eventId);

        if (eventOptional.isEmpty()) {
            showError("Événement non trouvé");
            NavigationManager.goToEventList();
            return;
        }

        this.event = eventOptional.get();
        createContent();
    }

    /**
     * Créer le contenu
     */
    private void createContent() {
        // Bouton retour
        Button backBtn = new Button("← Retour à l'événement",
                e -> NavigationManager.goToEventDetail(event.getId()));
        backBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        add(backBtn);

        // Card principale
        VerticalLayout card = new VerticalLayout();
        card.setPadding(true);
        card.setSpacing(true);
        card.getStyle()
                .set("background", "white")
                .set("border-radius", "10px")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.1)");

        // Titre
        H1 title = new H1("🎫 Réserver des places");
        title.getStyle().set("margin", "0");

        // Informations événement
        VerticalLayout eventInfo = createEventInfo();

        // Formulaire
        VerticalLayout form = createForm();

        // Récapitulatif
        VerticalLayout summary = createSummary();

        card.add(title, new Hr(), eventInfo, new Hr(), form, new Hr(), summary);
        add(card);
    }

    /**
     * Créer les infos événement
     */
    private VerticalLayout createEventInfo() {
        VerticalLayout info = new VerticalLayout();
        info.setPadding(false);
        info.setSpacing(false);

        H2 eventTitle = new H2(event.getTitre());
        eventTitle.getStyle().set("margin", "0");

        Span date = new Span("📅 " + event.getDateDebut().format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")
        ));
        date.getStyle().set("color", "#666");

        Span location = new Span("📍 " + event.getLieu() + ", " + event.getVille());
        location.getStyle().set("color", "#666");

        Span price = new Span("💰 Prix unitaire: " + event.getPrixUnitaire() + " DH");
        price.getStyle().set("color", "#666").set("font-weight", "bold");

        Span available = new Span("✅ Places disponibles: " + event.getCapaciteRestante() +
                " / " + event.getCapaciteMax());
        available.getStyle().set("color", "#10b981");

        info.add(eventTitle, date, location, price, available);
        return info;
    }

    /**
     * Créer le formulaire
     */
    private VerticalLayout createForm() {
        VerticalLayout form = new VerticalLayout();
        form.setPadding(false);
        form.setSpacing(true);

        H3 formTitle = new H3("Nombre de places");

        // Champ nombre de places
        placesField = new IntegerField();
        placesField.setMin(1);
        placesField.setMax(Math.min(10, event.getCapaciteRestante()));
        placesField.setValue(1);
        placesField.setStepButtonsVisible(true);
        placesField.setHelperText("Maximum 10 places par réservation");
        placesField.setWidth("200px");
        placesField.addValueChangeListener(e -> updateTotal());

        // Champ commentaire
        H3 commentTitle = new H3("Commentaire (optionnel)");
        commentField = new TextArea();
        commentField.setPlaceholder("Ajoutez un commentaire...");
        commentField.setMaxLength(500);
        commentField.setWidthFull();

        form.add(formTitle, placesField, commentTitle, commentField);
        return form;
    }

    /**
     * Créer le récapitulatif
     */
    private VerticalLayout createSummary() {
        VerticalLayout summary = new VerticalLayout();
        summary.setPadding(true);
        summary.setSpacing(true);
        summary.getStyle()
                .set("background", "#f9fafb")
                .set("border-radius", "8px");

        H3 summaryTitle = new H3("📋 Récapitulatif");
        summaryTitle.getStyle().set("margin-top", "0");

        HorizontalLayout priceRow = new HorizontalLayout();
        priceRow.setWidthFull();
        priceRow.setJustifyContentMode(JustifyContentMode.BETWEEN);

        Span priceLabel = new Span("Prix unitaire:");
        Span priceValue = new Span(event.getPrixUnitaire() + " DH");
        priceValue.getStyle().set("font-weight", "bold");
        priceRow.add(priceLabel, priceValue);

        HorizontalLayout totalRow = new HorizontalLayout();
        totalRow.setWidthFull();
        totalRow.setJustifyContentMode(JustifyContentMode.BETWEEN);

        Span totalLabel = new Span("TOTAL À PAYER:");
        totalLabel.getStyle().set("font-weight", "bold").set("font-size", "1.2em");

        totalAmount = new Span(calculateTotal() + " DH");
        totalAmount.getStyle()
                .set("font-weight", "bold")
                .set("font-size", "1.5em")
                .set("color", "#3b82f6");
        totalRow.add(totalLabel, totalAmount);

        // Bouton confirmer
        submitBtn = new Button("Confirmer la réservation", VaadinIcon.CHECK.create());
        submitBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        submitBtn.setWidthFull();
        submitBtn.addClickListener(e -> handleSubmit());

        summary.add(summaryTitle, priceRow, totalRow, submitBtn);
        return summary;
    }

    /**
     * Mettre à jour le total
     */
    private void updateTotal() {
        if (totalAmount != null) {
            totalAmount.setText(calculateTotal() + " DH");
        }
    }

    /**
     * Calculer le total
     */
    private String calculateTotal() {
        if (placesField == null || placesField.getValue() == null) {
            return "0";
        }

        BigDecimal total = event.getPrixUnitaire()
                .multiply(new BigDecimal(placesField.getValue()));

        return total.toString();
    }

    /**
     * Gérer la soumission
     */
    private void handleSubmit() {
        Integer places = placesField.getValue();
        String comment = commentField.getValue();

        if (places == null || places < 1) {
            showError("Veuillez sélectionner au moins 1 place");
            return;
        }

        if (places > event.getCapaciteRestante()) {
            showError("Pas assez de places disponibles");
            return;
        }

        try {
            Reservation reservation = reservationService.createReservation(
                    currentUser.getId(),
                    event.getId(),
                    places
            );

            // Ajouter commentaire si présent
            if (comment != null && !comment.trim().isEmpty()) {
                reservation.setCommentaire(comment);
            }

            // Confirmer immédiatement
            reservationService.confirmReservation(reservation.getId());

            showSuccessDialog(reservation);

        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }

    /**
     * Afficher le dialogue de succès
     */
    private void showSuccessDialog(Reservation reservation) {
        removeAll();

        VerticalLayout successCard = new VerticalLayout();
        successCard.setMaxWidth("600px");
        successCard.setPadding(true);
        successCard.setSpacing(true);
        successCard.setAlignItems(Alignment.CENTER);
        successCard.getStyle()
                .set("background", "white")
                .set("border-radius", "10px")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.1)")
                .set("margin", "2em auto");

        H1 successIcon = new H1("✅");
        successIcon.getStyle().set("font-size", "4em").set("margin", "0");

        H2 successTitle = new H2("Réservation confirmée !");
        successTitle.getStyle().set("color", "#10b981").set("margin", "0.5em 0");

        Span codeLabel = new Span("Votre code de réservation :");
        codeLabel.getStyle().set("color", "#666");

        Span code = new Span(reservation.getCodeReservation());
        code.getStyle()
                .set("font-size", "2em")
                .set("font-weight", "bold")
                .set("color", "#3b82f6")
                .set("padding", "0.5em 1em")
                .set("background", "#eff6ff")
                .set("border-radius", "8px");

        Paragraph info = new Paragraph(
                "Un email de confirmation vous a été envoyé. " +
                        "Gardez ce code pour accéder à l'événement."
        );
        info.getStyle().set("text-align", "center").set("color", "#666");

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);

        Button reservationsBtn = new Button("Mes réservations", VaadinIcon.TICKET.create());
        reservationsBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        reservationsBtn.addClickListener(e -> NavigationManager.goToMyReservations());

        Button eventsBtn = new Button("Autres événements", VaadinIcon.CALENDAR.create());
        eventsBtn.addClickListener(e -> NavigationManager.goToEventList());

        buttons.add(reservationsBtn, eventsBtn);

        successCard.add(successIcon, successTitle, codeLabel, code, info, buttons);
        add(successCard);
    }

    /**
     * Afficher un message d'erreur
     */
    private void showError(String message) {
        Notification notification = Notification.show(message, 4000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}