package com.event.event_reservation.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationStatisticsDTO {
    private long totalReservations;
    private long confirmedReservations;
    private long cancelledReservations;
    private BigDecimal totalSpent;
    private BigDecimal averageSpent;
}