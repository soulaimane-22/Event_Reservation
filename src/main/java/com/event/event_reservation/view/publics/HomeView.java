package com.event.event_reservation.view.publics;

import com.event.event_reservation.config.NavigationManager;
import com.event.event_reservation.entity.Event;
import com.event.event_reservation.entity.enums.EventStatus;
import com.event.event_reservation.service.EventService;
import com.event.event_reservation.view.components.VaadinAppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Route(value = "", layout = VaadinAppLayout.class)
@PageTitle("Accueil - Event Reservation")
public class HomeView extends VerticalLayout {

    private final EventService eventService;
    private final String BRAND_BLUE = "#253366";
    private final String ICON_PATH = "images/events/icons/";

    @Autowired
    public HomeView(EventService eventService) {
        this.eventService = eventService;

        // Configuration Plein Écran
        setPadding(false);
        setSpacing(false);
        setMargin(false);
        setWidthFull();

        // 1. HERO SECTION (Texte pur sur Gradient)
        createHeroSection();

        // Conteneur pour la grille
        VerticalLayout content = new VerticalLayout();
        content.setPadding(true);
        content.setSpacing(true);
        content.setAlignItems(Alignment.CENTER);
        content.getStyle().set("padding", "60px 5%");

        List<Event> allEvents = eventService.searchEvents(null, null, null, null, null, null)
                .stream()
                .filter(e -> e.getStatut() == EventStatus.PUBLIE)
                .sorted(Comparator.comparing(Event::getDateDebut))
                .toList();

        // 2. Grille des événements
        createEventsGridSection(content, allEvents);

        add(content);
        createCallToAction();
    }

    private void createHeroSection() {
        Div hero = new Div();
        hero.setWidthFull();
        hero.setHeight("500px");
        hero.getStyle()
                .set("background", "linear-gradient(135deg, " + BRAND_BLUE + " 0%, #435591 100%)")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("color", "white")
                .set("position", "relative")
                .set("overflow", "hidden");

        // Cercle décoratif discret
        Span decoration = new Span();
        decoration.getStyle()
                .set("position", "absolute").set("top", "-100px").set("right", "-100px")
                .set("width", "400px").set("height", "400px")
                .set("border-radius", "50%")
                .set("background", "rgba(255,255,255,0.05)");
        hero.add(decoration);

        VerticalLayout heroContent = new VerticalLayout();
        heroContent.setAlignItems(Alignment.CENTER);
        heroContent.setSpacing(true);

        H1 title = new H1("Réservez vos moments inoubliables");
        title.getStyle()
                .set("font-size", "3.5em")
                .set("margin", "0")
                .set("text-align", "center")
                .set("font-weight", "800")
                .set("text-shadow", "2px 2px 15px rgba(0,0,0,0.2)");

        Paragraph subtitle = new Paragraph("La plateforme de référence pour la billetterie et la gestion d'événements au Maroc.");
        subtitle.getStyle()
                .set("font-size", "1.4em")
                .set("max-width", "700px")
                .set("text-align", "center")
                .set("opacity", "0.9");

        heroContent.add(title, subtitle);
        hero.add(heroContent);
        add(hero);
    }

    private void createEventsGridSection(VerticalLayout container, List<Event> events) {
        VerticalLayout titleBlock = new VerticalLayout();
        titleBlock.setAlignItems(Alignment.CENTER);
        titleBlock.setSpacing(false);
        titleBlock.setPadding(false);
        titleBlock.getStyle().set("margin-bottom", "50px");

        Span sub = new Span("Event Reservation - Votre plateforme de billetterie au Maroc et en Afrique");
        sub.getStyle()
                .set("color", BRAND_BLUE)
                .set("font-weight", "600")
                .set("font-size", "0.95em")
                .set("opacity", "0.8")
                .set("letter-spacing", "0.5px");

        H2 mainHeading = new H2("Les événements incontournables");
        mainHeading.getStyle()
                .set("color", BRAND_BLUE)
                .set("font-size", "2.5em")
                .set("margin-top", "10px")
                .set("font-weight", "700");

        titleBlock.add(sub, mainHeading);

        Div grid = new Div();
        grid.setWidthFull();
        grid.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(auto-fill, minmax(350px, 1fr))")
                .set("gap", "40px");

        if (events.isEmpty()) {
            grid.add(new Paragraph("Aucun événement disponible pour le moment."));
        } else {
            events.forEach(event -> grid.add(createEventCard(event)));
        }

        container.add(titleBlock, grid);
    }

    private VerticalLayout createEventCard(Event event) {
        VerticalLayout card = new VerticalLayout();
        card.setPadding(false);
        card.setSpacing(false);
        card.getStyle()
                .set("background", "white")
                .set("border-radius", "20px")
                .set("overflow", "hidden")
                .set("box-shadow", "0 10px 30px rgba(0,0,0,0.08)")
                .set("transition", "transform 0.3s ease");

        Div imageContainer = new Div();
        imageContainer.setWidthFull();
        imageContainer.setHeight("240px");
        imageContainer.getStyle().set("position", "relative");

        String url = event.getImageUrl();
        if (url == null || url.isEmpty()) {
            // Chemin vers une image par défaut dans src/main/resources/static/images/
            url = "images/events/icons/event.svg";
        }
        Image img = new Image(url, event.getTitre());
        img.setWidthFull(); img.setHeightFull();
        img.getStyle().set("object-fit", "cover");

        if (event.getNbPlacesDisponibles() <= 0) {
            Span soldOut = new Span("COMPLET");
            soldOut.getStyle()
                    .set("position", "absolute").set("top", "20px").set("left", "20px")
                    .set("background", BRAND_BLUE).set("color", "white")
                    .set("padding", "8px 20px").set("font-weight", "800")
                    .set("border-radius", "6px").set("font-size", "0.8em");
            imageContainer.add(img, soldOut);
        } else {
            imageContainer.add(img);
        }

        VerticalLayout body = new VerticalLayout();
        body.setPadding(true);
        body.setSpacing(false);
        body.getStyle().set("padding", "30px");

        H3 title = new H3(event.getTitre());
        title.getStyle().set("color", BRAND_BLUE).set("margin", "0 0 20px 0").set("font-size", "1.4em");

        body.add(title);
        body.add(createInfoRow("date_time.svg", event.getDateDebut().format(DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy", Locale.FRENCH))));
        body.add(createInfoRow("clock_hour.svg", event.getDateDebut().format(DateTimeFormatter.ofPattern("HH:mm"))));
        body.add(createInfoRow("map.svg", event.getVille()));

        Span priceValue = new Span(event.getPrixUnitaire() + " MAD");
        priceValue.getStyle()
                .set("color", BRAND_BLUE).set("font-weight", "800")
                .set("font-size", "1.6em").set("margin-top", "15px");

        HorizontalLayout priceRow = createInfoRow("argent.svg", "");
        priceRow.add(priceValue);
        body.add(priceRow);

        Button detailsBtn = new Button("Détails de l'événement");
        detailsBtn.setWidthFull();
        detailsBtn.getStyle()
                .set("margin-top", "25px")
                .set("background-color", BRAND_BLUE)
                .set("color", "white")
                .set("height", "50px");
        detailsBtn.addClickListener(e -> NavigationManager.goToEventDetail(event.getId()));

        body.add(detailsBtn);
        card.add(imageContainer, body);

        card.getElement().executeJs("this.onmouseover = () => { this.style.transform = 'translateY(-15px)'; }; " +
                "this.onmouseout = () => { this.style.transform = 'translateY(0)'; };");

        return card;
    }

    private HorizontalLayout createInfoRow(String iconName, String text) {
        HorizontalLayout row = new HorizontalLayout();
        row.setAlignItems(Alignment.CENTER);
        row.setSpacing(true);
        row.getStyle().set("margin-bottom", "12px");

        Image icon = new Image(ICON_PATH + iconName, "");
        icon.setWidth("22px"); icon.setHeight("22px");

        Span infoText = new Span(text);
        infoText.getStyle().set("color", BRAND_BLUE).set("font-size", "1em").set("font-weight", "500");

        row.add(icon, infoText);
        return row;
    }

    private void createCallToAction() {
        VerticalLayout cta = new VerticalLayout();
        cta.setWidthFull();
        cta.setAlignItems(Alignment.CENTER);
        cta.getStyle().set("background", "#f4f6f9").set("padding", "80px 0");

        H2 ctaTitle = new H2("Prêt pour une expérience inoubliable ?");
        ctaTitle.getStyle().set("color", BRAND_BLUE);

        Button startBtn = new Button("Commencer dès maintenant", e -> NavigationManager.goToRegister());
        startBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        startBtn.getStyle().set("background-color", BRAND_BLUE).set("padding", "0 50px");

        cta.add(ctaTitle, startBtn);
        add(cta);
    }
}