package com.event.event_reservation.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "avis", indexes = {
        @Index(name = "idx_user_event", columnList = "utilisateur_id,evenement_id", unique = true),
        @Index(name = "idx_evenement", columnList = "evenement_id"),
        @Index(name = "idx_note", columnList = "note")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Avis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer note; // 1 à 5

    @Column(length = 1000)
    private String commentaire;

    @Column(nullable = false)
    private LocalDateTime datePublication = LocalDateTime.now();

    @Column
    private LocalDateTime dateModification;

    // Relations
    @ManyToOne
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private User utilisateur;

    @ManyToOne
    @JoinColumn(name = "evenement_id", nullable = false)
    private Event evenement;

    @ManyToOne
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    @PreUpdate
    protected void onUpdate() {
        dateModification = LocalDateTime.now();
    }
}