package com.event.event_reservation.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatisticsDTO {
    private Long userId;
    private long eventsCreated;
    private long reservationsCount;
    private BigDecimal totalSpent;
}