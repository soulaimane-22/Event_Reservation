package com.event.event_reservation.view.admin;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.event.event_reservation.entity.User;
import com.event.event_reservation.entity.enums.UserRole;
import com.event.event_reservation.service.UserService;
import com.event.event_reservation.config.VaadinSession;
import com.event.event_reservation.view.components.VaadinAppLayout;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Route(value = "admin/users", layout = VaadinAppLayout.class)
@PageTitle("Gestion Utilisateurs - Admin")
public class UserManagementView extends VerticalLayout {

    private final UserService userService;
    private final String BRAND_BLUE = "#253366";
    private final String ICON_PATH = "images/events/icons/";

    private final Grid<User> grid = new Grid<>(User.class, false);
    private TextField searchField;
    private ComboBox<UserRole> roleFilter;
    private ComboBox<Boolean> statusFilter;

    @Autowired
    public UserManagementView(UserService userService) {
        this.userService = userService;

        User currentUser = VaadinSession.getCurrentUser();
        if (currentUser == null || currentUser.getRole() != UserRole.ADMIN) {
            UI.getCurrent().navigate("");
            return;
        }

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().set("background-color", "#f8f9fa");

        VerticalLayout container = new VerticalLayout();
        container.setWidthFull();
        container.setMaxWidth("1250px");
        container.getStyle().set("margin", "0 auto");
        container.setPadding(true);

        createHeader(container);
        createFilterCard(container);
        configureGrid(container);

        add(container);
        refreshGrid(null, null);
    }

    private void createHeader(VerticalLayout container) {
        HorizontalLayout headerRow = new HorizontalLayout();
        headerRow.setWidthFull();
        headerRow.setAlignItems(Alignment.CENTER);
        headerRow.getStyle().set("margin", "30px 0");

        Image peopleIcon = new Image(ICON_PATH + "people.svg", "");
        peopleIcon.setWidth("55px");

        VerticalLayout titles = new VerticalLayout();
        titles.setPadding(false); titles.setSpacing(false);

        H1 title = new H1("Gestion des Utilisateurs");
        title.getStyle().set("color", BRAND_BLUE).set("font-weight", "800").set("margin", "0").set("font-size", "2.5em");

        Span subtitle = new Span("Panneau d'administration et d'archivage professionnel");
        subtitle.getStyle().set("color", "#666").set("font-size", "1.1em");

        titles.add(title, subtitle);
        headerRow.add(peopleIcon, titles);
        container.add(headerRow);
    }

    private void createFilterCard(VerticalLayout container) {
        HorizontalLayout filterRow = new HorizontalLayout();
        filterRow.setWidthFull();
        filterRow.setAlignItems(Alignment.END);
        filterRow.getStyle()
                .set("background-color", "white")
                .set("padding", "25px")
                .set("border-radius", "15px")
                .set("box-shadow", "0 4px 15px rgba(0,0,0,0.05)")
                .set("margin-bottom", "20px");

        searchField = new TextField("Recherche");
        searchField.setPlaceholder("Email ou nom...");
        searchField.setPrefixComponent(new Image(ICON_PATH + "recherche.svg", ""));
        searchField.setWidth("350px");
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> applyCombinedFilters());

        roleFilter = new ComboBox<>("Rôle");
        roleFilter.setItems(UserRole.values());
        roleFilter.setPlaceholder("Tous");
        roleFilter.setClearButtonVisible(true);

        statusFilter = new ComboBox<>("Statut");
        statusFilter.setItems(true, false);
        statusFilter.setItemLabelGenerator(v -> v ? "Actif" : "Inactif");
        statusFilter.setPlaceholder("Tous");
        statusFilter.setClearButtonVisible(true);

        Button filterBtn = new Button("Appliquer", e -> refreshGrid(roleFilter.getValue(), statusFilter.getValue()));
        filterBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        filterBtn.getStyle().set("background-color", BRAND_BLUE);

        Button resetBtn = new Button(new Image(ICON_PATH + "reinitialiser.svg", ""));
        resetBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        resetBtn.addClickListener(e -> {
            searchField.clear(); roleFilter.clear(); statusFilter.clear();
            refreshGrid(null, null);
        });

        filterRow.add(searchField, roleFilter, statusFilter, filterBtn, resetBtn);
        filterRow.setFlexGrow(1, searchField);
        container.add(filterRow);
    }

    private void configureGrid(VerticalLayout container) {
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        grid.getStyle()
                .set("background-color", "white")
                .set("border-radius", "20px")
                .set("box-shadow", "0 10px 30px rgba(0,0,0,0.03)")
                .set("overflow", "hidden");
        grid.setHeight("600px");

        grid.addColumn(u -> u.getPrenom() + " " + u.getNom()).setHeader("UTILISATEUR").setSortable(true).setFlexGrow(1);
        grid.addColumn(User::getEmail).setHeader("EMAIL").setAutoWidth(true);

        grid.addComponentColumn(u -> {
            Span b = new Span(u.getActif() ? "ACTIF" : "INACTIF");
            b.getStyle().set("padding", "4px 12px").set("border-radius", "20px").set("font-size", "0.75em").set("font-weight", "bold").set("color", "white");
            b.getStyle().set("background-color", u.getActif() ? "#10b981" : "#ef4444");
            return b;
        }).setHeader("STATUT").setAutoWidth(true);

        grid.addComponentColumn(user -> {
            HorizontalLayout actions = new HorizontalLayout();

            Button viewBtn = new Button("Détails");
            viewBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            viewBtn.getStyle().set("color", BRAND_BLUE).set("font-weight", "800");
            viewBtn.addClickListener(e -> openUserDialog(user));

            Button toggleBtn = new Button(user.getActif() ? "Désactiver" : "Activer");
            toggleBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, user.getActif() ? ButtonVariant.LUMO_ERROR : ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_TERTIARY);
            toggleBtn.addClickListener(e -> {
                userService.toggleUserActive(user.getId(), !user.getActif());
                refreshGrid(roleFilter.getValue(), statusFilter.getValue());
                Notification.show("Statut mis à jour");
            });

            actions.add(viewBtn, toggleBtn);
            return actions;
        }).setHeader("ACTIONS").setAutoWidth(true);

        container.add(grid);
    }

    private void refreshGrid(UserRole role, Boolean actif) {
        grid.setItems(userService.getUsersWithFilters(role, actif));
    }

    private void applyCombinedFilters() {
        String search = searchField.getValue();
        if (search != null && !search.isEmpty()) {
            grid.setItems(userService.searchUsers(search));
        } else {
            refreshGrid(roleFilter.getValue(), statusFilter.getValue());
        }
    }

    /**
     * DIALOGUE PRO : Affiche les détails et permet l'export Texte Officiel
     */
    private void openUserDialog(User user) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Fiche Individuelle : " + user.getPrenom() + " " + user.getNom());
        dialog.setWidth("500px");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(true);
        content.setAlignItems(Alignment.CENTER);

        Image userIcon = new Image(ICON_PATH + "client.svg", "");
        userIcon.setWidth("70px");

        H3 fullName = new H3(user.getPrenom() + " " + user.getNom());
        fullName.getStyle().set("color", BRAND_BLUE).set("font-weight", "800");

        VerticalLayout info = new VerticalLayout(
                new Span("Rôle : " + user.getRole().name()),
                new Span("Email : " + user.getEmail()),
                new Span("Téléphone : " + (user.getTelephone() != null ? user.getTelephone() : "N/A")),
                new Span("Statut : " + (user.getActif() ? "Compte Actif" : "Compte Suspendu"))
        );
        info.setSpacing(false);
        info.setAlignItems(Alignment.CENTER);
        info.getChildren().forEach(c -> c.getStyle().set("margin-bottom", "5px").set("font-weight", "600"));

        // BOUTON EXPORT PRO (.TXT)
        Anchor exportBtn = createOfficialArchiveExport(user);
        exportBtn.getStyle().set("margin-top", "20px");

        content.add(userIcon, fullName, info, exportBtn);
        dialog.add(content);

        Button close = new Button("Fermer", e -> dialog.close());
        close.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        dialog.getFooter().add(close);

        dialog.open();
    }

    /**
     * Génère un document texte officiel pour l'archivage (Format Pro)
     */
    private Anchor createOfficialArchiveExport(User u) {
        Image icon = new Image(ICON_PATH + "downloadbold.svg", "Export");
        icon.setWidth("24px");
        icon.setHeight("24px");

        Button btn = new Button(icon);
        btn.getStyle()
                .set("background-color", "white")
                .set("width", "50px")
                .set("height", "50px")
                .set("border-radius", "12px")
                .set("cursor", "pointer")
                .set("display", "flex")
                .set("justify-content", "center")
                .set("align-items", "center");

        StreamResource res = new StreamResource("ARCHIVE_" + u.getNom().toUpperCase() + ".txt", () -> {
            String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            StringBuilder sb = new StringBuilder();
            sb.append("--------------------------------------------------\n");
            sb.append("   OCCASIO EVENTS - DOCUMENT D'ARCHIVE OFFICIEL   \n");
            sb.append("--------------------------------------------------\n\n");
            sb.append("DATE D'EXTRACTION : ").append(date).append("\n\n");
            sb.append("ETAT CIVIL :\n");
            sb.append("  > NOM      : ").append(u.getNom().toUpperCase()).append("\n");
            sb.append("  > PRENOM   : ").append(u.getPrenom()).append("\n\n");
            sb.append("COORDONNEES :\n");
            sb.append("  > EMAIL    : ").append(u.getEmail()).append("\n");
            sb.append("  > TELEPHONE: ").append(u.getTelephone() != null ? u.getTelephone() : "NON RENSEIGNE").append("\n\n");
            sb.append("COMPTE :\n");
            sb.append("  > RÔLE     : ").append(u.getRole().name()).append("\n");
            sb.append("  > STATUT   : ").append(u.getActif() ? "ACTIF (VALIDE)" : "INACTIF (SUSPENDU)").append("\n\n");
            sb.append("--------------------------------------------------\n");
            sb.append("Certifié conforme par le système d'administration\n");
            sb.append("Copyright 2025 - Event Reservation\n");
            sb.append("--------------------------------------------------\n");
            return new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8));
        });

        Anchor anchor = new Anchor(res, "");
        anchor.getElement().setAttribute("download", true);
        anchor.add(btn);
        return anchor;
    }
}