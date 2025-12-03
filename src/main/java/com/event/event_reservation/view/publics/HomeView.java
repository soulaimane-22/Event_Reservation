package com.event.event_reservation.view.publics;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.event.event_reservation.config.NavigationManager;
import com.event.event_reservation.view.components.VaadinAppLayout;

/**
 * Page d'accueil publique
 * URL: /
 */
@Route(value = "", layout = VaadinAppLayout.class)
@PageTitle("Accueil - Event Reservation")
public class HomeView extends VerticalLayout {

    public HomeView() {
        // Configuration du layout
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        setAlignItems(Alignment.CENTER);

        // Créer le contenu
        createHeroSection();
        createFeaturesSection();
        createCallToAction();
    }

    /**
     * Section Hero (bannière principale)
     */
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
        title.getStyle()
                .set("margin", "0")
                .set("text-align", "center")
                .set("color", "white");

        Paragraph subtitle = new Paragraph(
                "Découvrez et réservez les meilleurs événements culturels près de chez vous"
        );
        subtitle.getStyle()
                .set("font-size", "1.2em")
                .set("text-align", "center")
                .set("max-width", "600px")
                .set("color", "rgba(255,255,255,0.9)");

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);

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

    /**
     * Section Fonctionnalités
     */
    private void createFeaturesSection() {
        H2 featuresTitle = new H2("Pourquoi choisir Event Reservation ?");
        featuresTitle.getStyle().set("margin-top", "3em").set("text-align", "center");

        HorizontalLayout features = new HorizontalLayout();
        features.setWidthFull();
        features.setMaxWidth("1000px");
        features.setSpacing(true);
        features.getStyle().set("flex-wrap", "wrap");

        features.add(
                createFeatureCard(
                        VaadinIcon.CALENDAR,
                        "Événements Variés",
                        "Concerts, théâtres, conférences, événements sportifs et bien plus"
                ),
                createFeatureCard(
                        VaadinIcon.TICKET,
                        "Réservation Facile",
                        "Réservez vos places en quelques clics seulement"
                ),
                createFeatureCard(
                        VaadinIcon.SHIELD,
                        "Paiement Sécurisé",
                        "Vos transactions sont 100% sécurisées"
                )
        );

        add(featuresTitle, features);
    }

    /**
     * Créer une carte de fonctionnalité
     */
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

        icon.create().setSize("48px");
        icon.create().setColor("#667eea");

        H2 cardTitle = new H2(title);
        cardTitle.getStyle()
                .set("margin", "0.5em 0")
                .set("font-size", "1.2em")
                .set("color", "#333");

        Paragraph cardDesc = new Paragraph(description);
        cardDesc.getStyle()
                .set("text-align", "center")
                .set("color", "#666")
                .set("font-size", "0.95em");

        card.add(icon.create(), cardTitle, cardDesc);
        return card;
    }

    /**
     * Section Call-to-Action
     */
    private void createCallToAction() {
        VerticalLayout cta = new VerticalLayout();
        cta.setWidth("100%");
        cta.setMaxWidth("700px");
        cta.setPadding(true);
        cta.setSpacing(true);
        cta.setAlignItems(Alignment.CENTER);
        cta.getStyle()
                .set("margin-top", "3em")
                .set("margin-bottom", "2em")
                .set("background", "#f3f4f6")
                .set("border-radius", "10px");

        H2 ctaTitle = new H2("Prêt à découvrir les événements ?");
        ctaTitle.getStyle().set("margin", "0").set("text-align", "center");

        Paragraph ctaText = new Paragraph(
                "Rejoignez des milliers d'utilisateurs qui profitent déjà de nos services"
        );
        ctaText.getStyle().set("text-align", "center").set("color", "#666");

        Button startBtn = new Button("Commencer maintenant", VaadinIcon.ARROW_RIGHT.create());
        startBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        startBtn.addClickListener(e -> NavigationManager.goToEventList());

        cta.add(ctaTitle, ctaText, startBtn);
        add(cta);
    }
}
