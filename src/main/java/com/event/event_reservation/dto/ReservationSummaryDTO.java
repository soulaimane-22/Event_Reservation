package com.event.event_reservation.dto;

import com.event.event_reservation.entity.enums.ReservationStatus;
import lombok.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationSummaryDTO {
    private Long reservationId;
    private String codeReservation;
    private String eventTitle;
    private LocalDateTime eventDate;
    private Integer nombrePlaces;
    private BigDecimal prixUnitaire;
    private BigDecimal montantTotal;
    private ReservationStatus statut;
    private LocalDateTime dateReservation;
}