package com.event.event_reservation.entity;

import com.event.event_reservation.entity.enums.EventCategory;
import com.event.event_reservation.entity.enums.EventStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.math.BigDecimal;

@Entity
@Table(name = "events", indexes = {
        @Index(name = "idx_ville_dateDebut", columnList = "ville,date_debut"),
        @Index(name = "idx_categorie", columnList = "categorie"),
        @Index(name = "idx_statut", columnList = "statut"),
        @Index(name = "idx_organisateur", columnList = "organisateur_id"),
        @Index(name = "idx_dateDebut", columnList = "date_debut")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String titre;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventCategory categorie;

    @Column(nullable = false)
    private LocalDateTime dateDebut;

    @Column(nullable = false)
    private LocalDateTime dateFin;

    @Column(nullable = false, length = 255)
    private String lieu;

    @Column(nullable = false, length = 100)
    private String ville;

    @Column
    private Double latitude;

    @Column
    private Double longitude;

    @Column(nullable = false)
    private Integer capaciteMax;

    @Column(nullable = false)
    private Integer capaciteRestante;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal prixUnitaire;

    @Column(length = 500)
    private String imageUrl;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus statut = EventStatus.BROUILLON;

    @Builder.Default
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    @Column
    private LocalDateTime dateModification;

    @Builder.Default
    @Column(precision = 3, scale = 2)
    private BigDecimal moyenneNotes = BigDecimal.ZERO;

    @Builder.Default
    @Column
    private Integer nombreAvis = 0;

    // Relations
    @ManyToOne
    @JoinColumn(name = "organisateur_id", nullable = false)
    private User organisateur;

    @OneToMany(mappedBy = "evenement", cascade = CascadeType.REMOVE)
    private List<Reservation> reservations;

    @OneToMany(mappedBy = "evenement", cascade = CascadeType.REMOVE)
    private List<Avis> avis;

    @OneToMany(mappedBy = "evenement", cascade = CascadeType.REMOVE)
    private List<Notification> notifications;

    /* ===================== HELPERS ===================== */

    public Integer getNbPlacesDisponibles() {
        return this.capaciteRestante != null ? this.capaciteRestante : 0;
    }

    @PrePersist
    protected void onCreate() {
        if (this.dateCreation == null) this.dateCreation = LocalDateTime.now();
        // Initialisation automatique de la capacité restante à la création
        if (this.capaciteRestante == null) this.capaciteRestante = this.capaciteMax;
    }

    @PreUpdate
    protected void onUpdate() {
        dateModification = LocalDateTime.now();
    }
}