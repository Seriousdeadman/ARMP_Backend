// com.university.backend.ressource.dto.ResourceSuggestionRequest.java
package com.university.backend.ressource.dto;

import com.university.backend.ressource.enums.ResourceType;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ResourceSuggestionRequest {
    private ResourceType resourceType;
    private Integer minCapacity;
    private Integer maxCapacity;
    private String building;
    private LocalDateTime startDatetime;
    private LocalDateTime endDatetime;
    private Integer limit = 5;
}