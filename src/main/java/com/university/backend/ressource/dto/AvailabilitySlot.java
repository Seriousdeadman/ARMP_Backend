// com.university.backend.ressource.dto.AvailabilitySlot.java
package com.university.backend.ressource.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class AvailabilitySlot {
    private Long id;
    private String title;
    private LocalDateTime start;
    private LocalDateTime end;
    private String color;
    private Boolean available;
    private String resourceName;
    private Long resourceId;
}