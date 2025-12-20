package com.event.event_reservation.view.organizer;

import com.event.event_reservation.config.NavigationManager;
import com.event.event_reservation.config.VaadinSession;
import com.event.event_reservation.entity.Event;
import com.event.event_reservation.entity.User;
import com.event.event_reservation.entity.enums.EventCategory;
import com.event.event_reservation.entity.enums.UserRole;
import com.event.event_reservation.repository.EventRepository;
import com.event.event_reservation.service.EventService;
import com.event.event_reservation.view.components.VaadinAppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Route(value = "organizer/event/new", layout = VaadinAppLayout.class)
@PageTitle("Créer Événement")
public class EventFormView extends VerticalLayout {

    private final EventService eventService;
    private final User currentUser;

    private final TextField titre = new TextField("Titre");
    private final TextArea description = new TextArea("Description");
    private final ComboBox<EventCategory> categorie = new ComboBox<>("Catégorie");
    private final DateTimePicker dateDebut = new DateTimePicker("Date début");
    private final DateTimePicker dateFin = new DateTimePicker("Date fin");
    private final TextField lieu = new TextField("Lieu");
    private final TextField ville = new TextField("Ville");
    private final NumberField capacite = new NumberField("Capacité max");
    private final NumberField prix = new NumberField("Prix");

    @Autowired
    public EventFormView(EventService eventService) {
        this.eventService = eventService;
        this.currentUser = VaadinSession.getCurrentUser();

        if (currentUser == null || currentUser.getRole() == UserRole.CLIENT) {
            NavigationManager.goToLogin();
            return;
        }

        setWidth("600px");
        setPadding(true);
        setSpacing(true);

        categorie.setItems(EventCategory.values());

        Button save = new Button("Sauvegarder", e -> saveEvent());
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        add(new H1("Créer un événement"),
                titre, description, categorie,
                dateDebut, dateFin, lieu, ville,
                capacite, prix, save);
    }

    private void saveEvent() {
        eventService.createEvent(
                currentUser.getId(),
                titre.getValue(),
                description.getValue(),
                categorie.getValue(),
                dateDebut.getValue(),
                dateFin.getValue(),
                lieu.getValue(),
                ville.getValue(),
                capacite.getValue().intValue(),
                BigDecimal.valueOf(prix.getValue()),
                null,
                null
        );
        NavigationManager.goToMyEvents();
    }
}
