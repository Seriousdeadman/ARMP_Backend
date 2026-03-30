// com.university.backend.ressource.dto.ConflictAlternative.java
package com.university.backend.ressource.dto;

import com.university.backend.ressource.enums.ResourceType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ConflictAlternative {
    private Long id;
    private String name;
    private ResourceType resourceType;
    private Integer capacity;
    private String building;
    private String roomNumber;
    private LocalDateTime startDatetime;
    private LocalDateTime endDatetime;
    private Double score;
    private String reason;
    private String changeType; // "SAME_TIME", "TIME_SHIFT", "ALTERNATIVE_TYPE"
    private String shiftDescription; // "1 hour later", "Different building", etc.
}