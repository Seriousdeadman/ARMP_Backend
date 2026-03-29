package com.university.backend.ressource.dto;

import com.university.backend.ressource.enums.ResourceType;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationRequest {
    private ResourceType resourceType;
    private Long resourceId;
    private LocalDateTime startDatetime;
    private LocalDateTime endDatetime;
}