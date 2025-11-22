package com.event.event_reservation.entity;

import com.event.event_reservation.entity.enums.EmailType;
import com.event.event_reservation.entity.enums.EmailStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "email_logs", indexes = {
        @Index(name = "idx_dest_type", columnList = "destinataire,type_email"),
        @Index(name = "idx_email_statut", columnList = "statut"),
        @Index(name = "idx_email_date", columnList = "date_creation")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String destinataire;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmailType typeEmail;

    @Column(nullable = false, length = 255)
    private String sujet;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmailStatus statut = EmailStatus.EN_ATTENTE;

    @Column
    private LocalDateTime dateEnvoi;

    @Column(nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    @Column(length = 1000)
    private String messageErreur;

    // Relations
    @ManyToOne
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;
}