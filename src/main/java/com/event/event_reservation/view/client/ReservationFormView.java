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
import com.vaadin.flow.server.StreamResource;
import com.event.event_reservation.config.NavigationManager;
import com.event.event_reservation.config.VaadinSession;
import com.event.event_reservation.entity.Event;
import com.event.event_reservation.entity.Reservation;
import com.event.event_reservation.entity.User;
import com.event.event_reservation.repository.EventRepository;
import com.event.event_reservation.service.ReservationService;
import com.event.event_reservation.view.components.VaadinAppLayout;
import org.springframework.beans.factory.annotation.Autowired;

// Imports PDFBox 3.0.3
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
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
        VerticalLayout container = new VerticalLayout();
        container.setWidthFull();
        container.setMaxWidth("900px");
        container.getStyle().set("margin", "40px auto");
        container.setPadding(true);

        HorizontalLayout header = new HorizontalLayout();
        header.setAlignItems(Alignment.CENTER);
        header.getStyle().set("margin-bottom", "30px");
        Image ticketIcon = new Image(ICON_PATH + "ticket.svg", "");
        ticketIcon.setWidth("45px");
        H1 title = new H1("Finaliser votre réservation");
        title.getStyle().set("color", BRAND_BLUE).set("font-weight", "800").set("margin", "0").set("font-size", "2.2em");
        header.add(ticketIcon, title);

        VerticalLayout eventCard = createStyledCard();
        H2 eventTitre = new H2(event.getTitre());
        eventTitre.getStyle().set("color", BRAND_BLUE).set("margin", "0 0 20px 0");
        Div infoGrid = new Div();
        infoGrid.getStyle().set("display", "grid").set("grid-template-columns", "repeat(auto-fit, minmax(200px, 1fr))").set("gap", "20px");
        infoGrid.add(
                createMiniInfo("date_time.svg", event.getDateDebut().format(DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy", Locale.FRENCH))),
                createMiniInfo("clock_hour.svg", event.getDateDebut().format(DateTimeFormatter.ofPattern("HH:mm"))),
                createMiniInfo("map.svg", event.getVille()),
                createMiniInfo("argent.svg", event.getPrixUnitaire() + " MAD / place")
        );
        eventCard.add(eventTitre, infoGrid);

        VerticalLayout formCard = createStyledCard();
        placesField = new IntegerField("Nombre de places");
        placesField.setMin(1);
        placesField.setMax(Math.min(10, event.getCapaciteRestante()));
        placesField.setValue(1);
        placesField.setStepButtonsVisible(true);
        placesField.setWidth("250px");
        placesField.addValueChangeListener(e -> updateTotal());
        commentField = new TextArea("Commentaire (optionnel)");
        commentField.setWidthFull();
        formCard.add(placesField, commentField);

        VerticalLayout summaryCard = createStyledCard();
        summaryCard.getStyle().set("background-color", BRAND_BLUE).set("color", "white");
        HorizontalLayout priceRow = new HorizontalLayout();
        priceRow.setWidthFull();
        priceRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        totalAmount = new Span(calculateTotal() + " MAD");
        totalAmount.getStyle().set("font-size", "2em").set("font-weight", "800");
        priceRow.add(new Span("TOTAL À PAYER"), totalAmount);

        Button confirmBtn = new Button("Confirmer la réservation");
        confirmBtn.setWidthFull();
        confirmBtn.getStyle().set("background-color", "white").set("color", BRAND_BLUE).set("height", "65px").set("font-weight", "800");
        confirmBtn.addClickListener(e -> handleSubmit());
        summaryCard.add(priceRow, confirmBtn);

        container.add(header, eventCard, formCard, summaryCard);
        add(container);
    }

    private void handleSubmit() {
        try {
            Reservation res = reservationService.createReservation(currentUser.getId(), event.getId(), placesField.getValue());
            if (!commentField.getValue().isEmpty()) res.setCommentaire(commentField.getValue());
            reservationService.confirmReservation(res.getId());
            showSuccessLayout(res);
        } catch (Exception e) {
            Notification.show("Erreur : " + e.getMessage(), 4000, Notification.Position.TOP_CENTER).addThemeVariants(NotificationVariant.LUMO_ERROR);
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

        Image successIcon = new Image(ICON_PATH + "ticket.svg", "");
        successIcon.setWidth("90px");

        H2 successTitle = new H2("Réservation Validée !");
        successTitle.getStyle().set("color", "#10b981");

        Span code = new Span(res.getCodeReservation());
        code.getStyle().set("font-size", "2.5em").set("font-weight", "900").set("color", BRAND_BLUE).set("background", "#f1f3f9").set("padding", "15px 40px").set("border-radius", "15px").set("margin", "20px 0");

        StreamResource resource = new StreamResource("Ticket_" + res.getCodeReservation() + ".pdf", () -> {
            try {
                return generateTicketPdf(res);
            } catch (Exception e) {
                e.printStackTrace();
                return new ByteArrayInputStream("Erreur PDF".getBytes());
            }
        });

        Anchor downloadTicket = new Anchor(resource, "");
        downloadTicket.getElement().setAttribute("download", true);

        Button btnDownload = new Button("📄 Télécharger mon ticket (PDF)");
        btnDownload.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_PRIMARY);
        btnDownload.setWidthFull();
        btnDownload.getStyle().set("height", "55px").set("margin-bottom", "10px").set("font-weight", "800");
        downloadTicket.add(btnDownload);

        Button btnMyRes = new Button("Retour à mes réservations", e -> UI.getCurrent().navigate("my-reservations"));
        btnMyRes.setWidthFull();
        btnMyRes.getStyle().set("color", BRAND_BLUE);

        card.add(successIcon, successTitle, new Span("Voici votre code d'accès :"), code, downloadTicket, btnMyRes);
        layout.add(card);
        add(layout);
    }

    /**
     * Génère un PDF valide pour PDFBox 3.x
     */
    private ByteArrayInputStream generateTicketPdf(Reservation res) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float width = page.getMediaBox().getWidth();
                float height = page.getMediaBox().getHeight();

                PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                PDType1Font fontRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

                // TITRE (Couleur convertie en float 0.0 - 1.0 pour PDFBox)
                contentStream.beginText();
                contentStream.setFont(fontBold, 22);
                contentStream.setNonStrokingColor(37/255f, 51/255f, 102/255f);
                contentStream.newLineAtOffset(50, height - 60);
                contentStream.showText("TICKET DE RESERVATION");
                contentStream.endText();

                // LIGNE DE SÉPARATION
                contentStream.setLineWidth(1.5f);
                contentStream.moveTo(50, height - 75);
                contentStream.lineTo(width - 50, height - 75);
                contentStream.stroke();

                float y = height - 120;

                // INFOS ÉVÉNEMENT (On retire les accents pour éviter les erreurs de police Helvetica)
                y = addPdfLine(contentStream, fontBold, fontRegular, 14, 50, y, "EVENEMENT : ", cleanAccents(event.getTitre()));
                y = addPdfLine(contentStream, fontBold, fontRegular, 12, 50, y, "DATE : ", event.getDateDebut().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                y = addPdfLine(contentStream, fontBold, fontRegular, 12, 50, y, "HEURE : ", event.getDateDebut().format(DateTimeFormatter.ofPattern("HH:mm")));
                y = addPdfLine(contentStream, fontBold, fontRegular, 12, 50, y, "VILLE : ", cleanAccents(event.getVille()));

                y -= 30;
                contentStream.setLineWidth(0.5f);
                contentStream.moveTo(50, y);
                contentStream.lineTo(width - 50, y);
                contentStream.stroke();
                y -= 30;

                // INFOS CLIENT
                y = addPdfLine(contentStream, fontBold, fontRegular, 12, 50, y, "CLIENT : ", cleanAccents(currentUser.getPrenom() + " " + currentUser.getNom()));
                y = addPdfLine(contentStream, fontBold, fontRegular, 12, 50, y, "PLACES : ", String.valueOf(res.getNombrePlaces()));
                y = addPdfLine(contentStream, fontBold, fontRegular, 12, 50, y, "TOTAL : ", res.getMontantTotal() + " MAD");

                // ENCADRÉ DU CODE
                y -= 80;
                contentStream.setNonStrokingColor(230/255f, 230/255f, 235/255f);
                contentStream.addRect(100, y, width - 200, 60);
                contentStream.fill();

                contentStream.setNonStrokingColor(37/255f, 51/255f, 102/255f);
                contentStream.beginText();
                contentStream.setFont(fontBold, 26);
                contentStream.newLineAtOffset(200, y + 20);
                contentStream.showText(res.getCodeReservation());
                contentStream.endText();
            }
            document.save(baos);
        }
        return new ByteArrayInputStream(baos.toByteArray());
    }

    private float addPdfLine(PDPageContentStream cs, PDType1Font bold, PDType1Font reg, int size, float x, float y, String label, String value) throws Exception {
        cs.beginText();
        cs.setFont(bold, size);
        cs.newLineAtOffset(x, y);
        cs.showText(label);
        cs.setFont(reg, size);
        cs.showText(value != null ? value : "");
        cs.endText();
        return y - 20;
    }

    // Supprime les accents pour éviter le plantage PDF
    private String cleanAccents(String src) {
        if (src == null) return "";
        return src.replace("é", "e").replace("è", "e").replace("à", "a").replace("ê", "e").replace("ç", "c").replace("ô", "o");
    }

    private HorizontalLayout createMiniInfo(String icon, String text) {
        HorizontalLayout row = new HorizontalLayout(new Image(ICON_PATH + icon, ""), new Span(text));
        row.setAlignItems(Alignment.CENTER);
        row.getChildren().filter(c -> c instanceof Image).forEach(i -> ((Image)i).setWidth("20px"));
        return row;
    }

    private VerticalLayout createStyledCard() {
        VerticalLayout card = new VerticalLayout();
        card.getStyle().set("background-color", "white").set("border-radius", "25px").set("padding", "35px").set("box-shadow", "0 10px 30px rgba(0,0,0,0.06)").set("margin-bottom", "20px");
        return card;
    }

    private void updateTotal() {
        if (totalAmount != null) totalAmount.setText(calculateTotal() + " MAD");
    }

    private String calculateTotal() {
        if (placesField == null || placesField.getValue() == null) return "0.00";
        return String.format(Locale.US, "%.2f", event.getPrixUnitaire().multiply(new BigDecimal(placesField.getValue())));
    }
}