package com.university.backend.ressource.dto;

import com.university.backend.ressource.enums.ReservationStatus;
import com.university.backend.ressource.enums.ResourceType;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationResponse {
    private Long id;
    private String userId;
    private ResourceType resourceType;
    private Long resourceId;
    private String resourceName;
    private LocalDateTime startDatetime;
    private LocalDateTime endDatetime;
    private ReservationStatus status;
    private LocalDateTime createdAt;
}