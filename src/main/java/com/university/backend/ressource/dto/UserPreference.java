// com.university.backend.ressource.dto.UserPreference.java
package com.university.backend.ressource.dto;

import com.university.backend.ressource.enums.ResourceType;
import lombok.Data;
import java.util.Map;

@Data
public class UserPreference {
    private String userId;
    private Map<ResourceType, Integer> resourceTypePreference;
    private Map<String, Integer> buildingPreference;
    private Map<Integer, Integer> capacityPreference;
    private Map<String, Integer> timeSlotPreference; // "MORNING", "AFTERNOON", "EVENING"
}