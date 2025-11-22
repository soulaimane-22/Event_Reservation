package com.event.event_reservation.entity;

import com.event.event_reservation.entity.enums.ReservationStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "reservations", indexes = {
        @Index(name = "idx_code", columnList = "code_reservation", unique = true),
        @Index(name = "idx_user_event", columnList = "utilisateur_id,evenement_id"),
        @Index(name = "idx_res_statut", columnList = "statut"),
        @Index(name = "idx_date_reservation", columnList = "date_reservation")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String codeReservation;

    @Column(nullable = false)
    private Integer nombrePlaces;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal montantTotal;

    @Column(nullable = false)
    private LocalDateTime dateReservation = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus statut = ReservationStatus.EN_ATTENTE;

    @Column(length = 1000)
    private String commentaire;

    @Column
    private Boolean emailConfirmationEnvoye = false;

    @Column
    private Boolean emailRappelEnvoye = false;

    // Relations
    @ManyToOne
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private User utilisateur;

    @ManyToOne
    @JoinColumn(name = "evenement_id", nullable = false)
    private Event evenement;

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.REMOVE)
    private java.util.List<Avis> avis;

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.REMOVE)
    private java.util.List<Notification> notifications;

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.REMOVE)
    private java.util.List<EmailLog> emailLogs;
}