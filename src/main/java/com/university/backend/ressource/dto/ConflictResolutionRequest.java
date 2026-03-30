// com.university.backend.ressource.dto.ConflictResolutionRequest.java
package com.university.backend.ressource.dto;

import com.university.backend.ressource.enums.ResourceType;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ConflictResolutionRequest {
    private ResourceType resourceType;
    private Long resourceId;
    private LocalDateTime startDatetime;
    private LocalDateTime endDatetime;
    private String userId;
    private Integer alternativeCount = 5;
}