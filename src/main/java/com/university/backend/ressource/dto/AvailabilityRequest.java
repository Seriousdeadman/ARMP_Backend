// com.university.backend.ressource.dto.AvailabilityRequest.java
package com.university.backend.ressource.dto;

import com.university.backend.ressource.enums.ResourceType;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AvailabilityRequest {
    private ResourceType resourceType;
    private Long resourceId;
    private LocalDateTime start;
    private LocalDateTime end;
}