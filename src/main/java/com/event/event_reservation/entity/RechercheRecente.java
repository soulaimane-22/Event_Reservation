package com.event.event_reservation.entity;

import com.event.event_reservation.entity.enums.EventCategory;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "recherches_recentes", indexes = {
        @Index(name = "idx_utilisateur", columnList = "utilisateur_id"),
        @Index(name = "idx_date", columnList = "date_recherche")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RechercheRecente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String ville;

    @Enumerated(EnumType.STRING)
    private EventCategory categorie;

    @Column
    private LocalDateTime dateDebutMin;

    @Column
    private LocalDateTime dateDebutMax;

    @Column(precision = 10, scale = 2)
    private BigDecimal prixMin;

    @Column(precision = 10, scale = 2)
    private BigDecimal prixMax;

    @Column(nullable = false)
    private LocalDateTime dateRecherche = LocalDateTime.now();

    @Column
    private Integer nombreUtilisations = 1;

    // Relations
    @ManyToOne
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private User utilisateur;
}
