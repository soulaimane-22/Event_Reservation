package com.event.event_reservation.entity;

import com.event.event_reservation.entity.enums.StatisticType;
import com.event.event_reservation.entity.enums.EventCategory;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "statistiques", indexes = {
        @Index(name = "idx_type_periode", columnList = "type,periode"),
        @Index(name = "idx_periode", columnList = "periode")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Statistique {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatisticType type;

    @Column(nullable = false, length = 10)
    private String periode; // Format: YYYY-MM

    @Enumerated(EnumType.STRING)
    private EventCategory categorie;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valeur;

    @Column
    private Integer nombre;

    @Column(nullable = false)
    private LocalDateTime dateCalcul = LocalDateTime.now();
}