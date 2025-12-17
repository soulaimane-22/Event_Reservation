package com.event.event_reservation.view.publics;

import com.event.event_reservation.config.NavigationManager;
import com.event.event_reservation.entity.Event;
import com.event.event_reservation.entity.enums.EventStatus;
import com.event.event_reservation.service.EventService;
import com.event.event_reservation.view.components.VaadinAppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Page d'accueil publique
 * URL: /
 */
@Route(value = "", layout = VaadinAppLayout.class)
@PageTitle("Accueil - Event Reservation")
public class HomeView extends VerticalLayout {

    private final EventService eventService;

    @Autowired
    public HomeView(EventService eventService) {
        this.eventService = eventService;

        // Configuration du layout
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        setAlignItems(Alignment.CENTER);

        // Contenu EXISTANT (inchangé)
        createHeroSection();
        createEventsGridSection();
        createFeaturesSection();
        createCallToAction();
    }

    /* ===================== HERO SECTION ====================== */

    private void createHeroSection() {
        VerticalLayout hero = new VerticalLayout();
        hero.setWidth("100%");
        hero.setMaxWidth("800px");
        hero.setPadding(true);
        hero.setSpacing(true);
        hero.setAlignItems(Alignment.CENTER);
        hero.getStyle()
                .set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)")
                .set("color", "white")
                .set("border-radius", "15px")
                .set("margin-top", "2em");

        H1 title = new H1("🎭 Bienvenue sur Event Reservation");
        title.getStyle().set("margin", "0").set("color", "white");

        Paragraph subtitle = new Paragraph(
                "Découvrez et réservez les meilleurs événements culturels près de chez vous"
        );
        subtitle.getStyle()
                .set("font-size", "1.2em")
                .set("text-align", "center")
                .set("max-width", "600px")
                .set("color", "rgba(255,255,255,0.9)");

        HorizontalLayout buttons = new HorizontalLayout();

        Button exploreBtn = new Button("Explorer les événements", VaadinIcon.CALENDAR.create());
        exploreBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        exploreBtn.getStyle().set("background", "white").set("color", "#667eea");
        exploreBtn.addClickListener(e -> NavigationManager.goToEventList());

        Button registerBtn = new Button("Créer un compte", VaadinIcon.USER_CARD.create());
        registerBtn.addThemeVariants(ButtonVariant.LUMO_CONTRAST, ButtonVariant.LUMO_LARGE);
        registerBtn.addClickListener(e -> NavigationManager.goToRegister());

        buttons.add(exploreBtn, registerBtn);
        hero.add(title, subtitle, buttons);
        add(hero);
    }

    /* ================= EVENTS GRID ================= */

    private void createEventsGridSection() {
        H2 title = new H2("🎟️ Événements à venir");
        title.getStyle().set("margin-top", "3em");

        Div grid = new Div();
        grid.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(3, 1fr)")
                .set("gap", "24px")
                .set("max-width", "1100px")
                .set("width", "100%");

        List<Event> events = eventService.searchEvents(
                        null, null, null, null, null, null
                ).stream()
                .filter(e -> e.getStatut() == EventStatus.PUBLIE)
                .limit(6)
                .toList();

        if (events.isEmpty()) {
            grid.add(new Paragraph("Aucun événement disponible pour le moment."));
        } else {
            events.forEach(event -> grid.add(createEventCard(event)));
        }

        add(title, grid);
    }

    /* ================= EVENT CARD ================= */

    private VerticalLayout createEventCard(Event event) {
        VerticalLayout card = new VerticalLayout();
        card.setPadding(true);
        card.setSpacing(true);

        card.getStyle()
                .set("background", "white")
                .set("border", "1px solid #e5e7eb")
                .set("border-radius", "12px")
                .set("box-shadow", "0 2px 6px rgba(0,0,0,0.08)");

        /* ========= 🔥 SEULE MODIFICATION ICI ========= */
        Image eventImage = new Image(
                event.getImageUrl(),   // ⬅️ URL déjà complète depuis la DB
                event.getTitre()
        );
        eventImage.setWidthFull();
        eventImage.setHeight("180px");
        eventImage.getStyle()
                .set("object-fit", "cover")
                .set("border-radius", "8px");
        /* ============================================ */

        H3 title = new H3(event.getTitre());
        Span date = new Span(
                event.getDateDebut().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        );
        Span city = new Span("📍 " + event.getVille());
        Span price = new Span("💰 " + event.getPrixUnitaire() + " DH");

        Button details = new Button("Voir détails", VaadinIcon.EYE.create());
        details.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        details.addClickListener(e ->
                NavigationManager.goToEventDetail(event.getId())
        );

        card.add(eventImage, title, date, city, price, details);
        return card;
    }

    /* ================= FEATURES (INCHANGÉ) ================= */

    private void createFeaturesSection() {
        H2 featuresTitle = new H2("Pourquoi choisir Event Reservation ?");
        featuresTitle.getStyle().set("margin-top", "3em").set("text-align", "center");

        HorizontalLayout features = new HorizontalLayout();
        features.setWidthFull();
        features.setMaxWidth("1000px");
        features.setSpacing(true);
        features.getStyle().set("flex-wrap", "wrap");

        features.add(
                createFeatureCard(VaadinIcon.CALENDAR, "Événements Variés",
                        "Concerts, théâtres, conférences, événements sportifs et bien plus"),
                createFeatureCard(VaadinIcon.TICKET, "Réservation Facile",
                        "Réservez vos places en quelques clics seulement"),
                createFeatureCard(VaadinIcon.SHIELD, "Paiement Sécurisé",
                        "Vos transactions sont 100% sécurisées")
        );

        add(featuresTitle, features);
    }

    private VerticalLayout createFeatureCard(VaadinIcon icon, String title, String description) {
        VerticalLayout card = new VerticalLayout();
        card.setWidth("300px");
        card.setPadding(true);
        card.setSpacing(true);
        card.setAlignItems(Alignment.CENTER);
        card.getStyle()
                .set("background", "white")
                .set("border", "1px solid #e5e7eb")
                .set("border-radius", "10px")
                .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)");

        card.add(icon.create(), new H2(title), new Paragraph(description));
        return card;
    }

    /* ================= CTA (INCHANGÉ) ================= */

    private void createCallToAction() {
        VerticalLayout cta = new VerticalLayout();
        cta.setWidth("100%");
        cta.setMaxWidth("700px");
        cta.setPadding(true);
        cta.setSpacing(true);
        cta.setAlignItems(Alignment.CENTER);
        cta.getStyle()
                .set("margin-top", "3em")
                .set("background", "#f3f4f6")
                .set("border-radius", "10px");

        H2 ctaTitle = new H2("Prêt à découvrir les événements ?");
        Paragraph ctaText = new Paragraph(
                "Rejoignez des milliers d'utilisateurs qui profitent déjà de nos services"
        );

        Button startBtn = new Button("Commencer maintenant", VaadinIcon.ARROW_RIGHT.create());
        startBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        startBtn.addClickListener(e -> NavigationManager.goToEventList());

        cta.add(ctaTitle, ctaText, startBtn);
        add(cta);
    }
}
