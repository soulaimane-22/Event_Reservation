package com.event.event_reservation.config;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;
import org.springframework.stereotype.Component;

/**
 * Configuration du thème Vaadin pour l'application
 * Utilise le thème Lumo par défaut de Vaadin
 */
@Component
@Theme(value = "event-reservation")
public class VaadinThemeConfig implements AppShellConfigurator {

    // Cette classe configure le thème de l'application
    // Le thème "event-reservation" sera utilisé si vous créez des styles personnalisés
    // Sinon, Vaadin utilise automatiquement le thème Lumo par défaut

}