package com.event.event_reservation.entity;

import com.event.event_reservation.entity.enums.UserRole;
import com.event.event_reservation.entity.enums.ThemePreference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_email", columnList = "email", unique = true),
        @Index(name = "idx_role", columnList = "role")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 100)
    private String prenom;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Builder.Default // Indispensable pour que le Builder ne mette pas null
    @Column(nullable = false)
    private LocalDateTime dateInscription = LocalDateTime.now();

    @Builder.Default // Indispensable pour que le Builder ne mette pas null
    @Column(nullable = false)
    private Boolean actif = true;

    @Column(length = 20)
    private String telephone;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private ThemePreference themePreference = ThemePreference.LIGHT;

    @Column
    private LocalDateTime dateModification;

    /* ===================== RELATIONS ===================== */

    @OneToMany(mappedBy = "organisateur", cascade = CascadeType.REMOVE)
    private List<Event> eventsOrganises;

    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.REMOVE)
    private List<Reservation> reservations;

    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.REMOVE)
    private List<Avis> avis;

    @OneToMany(mappedBy = "destinataire", cascade = CascadeType.REMOVE)
    private List<Notification> notifications;

    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.REMOVE)
    private List<RechercheRecente> recherchesRecentes;

    /* ===================== LIFECYCLE ===================== */

    @PrePersist
    protected void onCreate() {
        if (this.dateInscription == null) this.dateInscription = LocalDateTime.now();
        this.dateModification = LocalDateTime.now();
        if (this.actif == null) this.actif = true;
    }

    @PreUpdate
    protected void onUpdate() {
        this.dateModification = LocalDateTime.now();
    }
}