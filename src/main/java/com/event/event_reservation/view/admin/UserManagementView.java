package com.event.event_reservation.view.admin;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.button.*;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.router.*;
import com.event.event_reservation.entity.User;
import com.event.event_reservation.entity.enums.UserRole;
import com.event.event_reservation.service.UserService;
import com.event.event_reservation.config.*;
import com.event.event_reservation.view.components.VaadinAppLayout;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Route(value = "admin/users", layout = VaadinAppLayout.class)
@PageTitle("Admin - Utilisateurs")
public class UserManagementView extends VerticalLayout {

    private final UserService userService;
    private final Grid<User> grid = new Grid<>(User.class, false);

    @Autowired
    public UserManagementView(UserService userService) {
        this.userService = userService;

        if (VaadinSession.getCurrentUser() == null ||
                VaadinSession.getCurrentUser().getRole() != UserRole.ADMIN) {
            NavigationManager.goToHome();
            return;
        }

        setSizeFull();
        add(new H1("👥 Gestion des Utilisateurs"));

        createFilters();
        configureGrid();
        refreshGrid(null, null);
    }

    private void createFilters() {
        TextField search = new TextField();
        search.setPlaceholder("Nom ou email");

        ComboBox<UserRole> roleFilter = new ComboBox<>("Rôle");
        roleFilter.setItems(UserRole.values());

        ComboBox<Boolean> activeFilter = new ComboBox<>("Statut");
        activeFilter.setItems(true, false);
        activeFilter.setItemLabelGenerator(v -> v ? "Actif" : "Inactif");

        Button filterBtn = new Button("Filtrer", e ->
                refreshGrid(roleFilter.getValue(), activeFilter.getValue())
        );

        HorizontalLayout filters = new HorizontalLayout(search, roleFilter, activeFilter, filterBtn);
        add(filters);

        search.addValueChangeListener(e -> {
            List<User> users = userService.searchUsers(e.getValue());
            grid.setItems(users);
        });
    }

    private void configureGrid() {
        grid.addColumn(u -> u.getPrenom() + " " + u.getNom()).setHeader("Nom");
        grid.addColumn(User::getEmail).setHeader("Email");
        grid.addColumn(User::getRole).setHeader("Rôle");

        grid.addComponentColumn(u -> badge(u.getActif() ? "ACTIF" : "INACTIF",
                u.getActif() ? "#10b981" : "#ef4444")).setHeader("Statut");

        grid.addComponentColumn(user -> {
            Button view = new Button("Voir", e -> openUserDialog(user));
            Button toggle = new Button(user.getActif() ? "Désactiver" : "Activer",
                    e -> {
                        userService.toggleUserActive(user.getId(), !user.getActif());
                        refreshGrid(null, null);
                    });
            return new HorizontalLayout(view, toggle);
        }).setHeader("Actions");

        add(grid);
    }

    private void refreshGrid(UserRole role, Boolean actif) {
        grid.setItems(userService.getUsersWithFilters(role, actif));
    }

    private Span badge(String text, String color) {
        Span badge = new Span(text);
        badge.getStyle()
                .set("background", color)
                .set("color", "white")
                .set("padding", "4px 10px")
                .set("border-radius", "6px");
        return badge;
    }

    private void openUserDialog(User user) {
        Dialog dialog = new Dialog();
        dialog.add(new H3("Profil Utilisateur"),
                new Span("Nom : " + user.getPrenom() + " " + user.getNom()),
                new Span("Email : " + user.getEmail()),
                new Span("Rôle : " + user.getRole()));
        dialog.open();
    }
}
