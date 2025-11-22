package com.event.event_reservation.entity;

import com.event.event_reservation.entity.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_dest_lue", columnList = "destinataire_id,lue"),
        @Index(name = "idx_date_creation", columnList = "date_creation")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false, length = 200)
    private String titre;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(nullable = false)
    private Boolean lue = false;

    @Column(nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    @Column
    private LocalDateTime dateLecture;

    // Relations
    @ManyToOne
    @JoinColumn(name = "destinataire_id", nullable = false)
    private User destinataire;

    @ManyToOne
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    @ManyToOne
    @JoinColumn(name = "evenement_id")
    private Event evenement;
}