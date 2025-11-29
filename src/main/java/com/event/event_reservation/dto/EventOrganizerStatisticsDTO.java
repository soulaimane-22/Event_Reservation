package com.event.event_reservation.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventOrganizerStatisticsDTO {
    private long totalEvents;
    private long publishedEvents;
    private long draftEvents;
    private long totalReservations;
    private BigDecimal totalRevenue;
}