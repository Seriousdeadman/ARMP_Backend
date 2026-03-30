// com.university.backend.ressource.dto.SmartBookingRequest.java
package com.university.backend.ressource.dto;

import com.university.backend.ressource.enums.ResourceType;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SmartBookingRequest {
    private ResourceType resourceType;
    private Long resourceId;
    private LocalDateTime startDatetime;
    private LocalDateTime endDatetime;
}