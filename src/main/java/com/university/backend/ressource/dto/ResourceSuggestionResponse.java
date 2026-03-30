// com.university.backend.ressource.dto.ResourceSuggestionResponse.java
package com.university.backend.ressource.dto;

import com.university.backend.ressource.enums.ResourceType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResourceSuggestionResponse {
    private Long id;
    private String name;
    private ResourceType resourceType;
    private Integer capacity;
    private String building;
    private String roomNumber;
    private Double score;
    private String reason;
    private Boolean available;
}