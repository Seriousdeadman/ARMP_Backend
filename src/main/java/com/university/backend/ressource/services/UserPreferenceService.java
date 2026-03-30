// com.university.backend.ressource.services.UserPreferenceService.java
package com.university.backend.ressource.services;

import com.university.backend.ressource.dto.UserPreference;
import com.university.backend.ressource.entities.Reservation;
import com.university.backend.ressource.enums.ResourceType;
import com.university.backend.ressource.repositories.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserPreferenceService {

    private final ReservationRepository reservationRepository;

    // In-memory cache for user preferences (would use Redis in production)
    private final Map<String, UserPreference> userPreferencesCache = new ConcurrentHashMap<>();

    /**
     * Get or build user preferences based on booking history
     */
    public UserPreference getUserPreferences(String userId) {
        // Return cached preferences if exists
        if (userPreferencesCache.containsKey(userId)) {
            return userPreferencesCache.get(userId);
        }

        // Build preferences from booking history
        UserPreference preferences = buildPreferencesFromHistory(userId);
        userPreferencesCache.put(userId, preferences);

        return preferences;
    }

    /**
     * Update preferences after a new booking
     */
    public void updatePreferences(String userId, Reservation reservation) {
        UserPreference preferences = getUserPreferences(userId);

        // Update resource type preference
        Map<ResourceType, Integer> resourcePref = preferences.getResourceTypePreference();
        resourcePref.merge(reservation.getResourceType(), 1, Integer::sum);

        // Get resource building and update
        String building = getResourceBuilding(reservation.getResourceType(), reservation.getResourceId());
        if (building != null) {
            Map<String, Integer> buildingPref = preferences.getBuildingPreference();
            buildingPref.merge(building, 1, Integer::sum);
        }

        // Update time slot preference
        String timeSlot = getTimeSlot(reservation.getStartDatetime());
        Map<String, Integer> timePref = preferences.getTimeSlotPreference();
        timePref.merge(timeSlot, 1, Integer::sum);

        // Update capacity preference
        Integer capacity = getResourceCapacity(reservation.getResourceType(), reservation.getResourceId());
        if (capacity != null) {
            Map<Integer, Integer> capacityPref = preferences.getCapacityPreference();
            capacityPref.merge(capacity, 1, Integer::sum);
        }

        // Save updated preferences
        userPreferencesCache.put(userId, preferences);
    }

    /**
     * Get user's most preferred building
     */
    public String getPreferredBuilding(String userId) {
        UserPreference preferences = getUserPreferences(userId);
        return preferences.getBuildingPreference().entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    /**
     * Get user's most preferred resource type
     */
    public ResourceType getPreferredResourceType(String userId) {
        UserPreference preferences = getUserPreferences(userId);
        return preferences.getResourceTypePreference().entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(ResourceType.CLASSROOM);
    }

    /**
     * Get user's preferred capacity range
     */
    public Integer getPreferredCapacity(String userId) {
        UserPreference preferences = getUserPreferences(userId);
        return preferences.getCapacityPreference().entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private UserPreference buildPreferencesFromHistory(String userId) {
        List<Reservation> userReservations = reservationRepository
                .findByUserIdOrderByStartDatetimeDesc(userId);

        UserPreference preferences = new UserPreference();
        preferences.setUserId(userId);
        preferences.setResourceTypePreference(new HashMap<>());
        preferences.setBuildingPreference(new HashMap<>());
        preferences.setCapacityPreference(new HashMap<>());
        preferences.setTimeSlotPreference(new HashMap<>());

        // Only analyze last 20 reservations for relevance
        userReservations.stream().limit(20).forEach(reservation -> {
            // Resource type preference
            preferences.getResourceTypePreference()
                    .merge(reservation.getResourceType(), 1, Integer::sum);

            // Building preference
            String building = getResourceBuilding(reservation.getResourceType(),
                    reservation.getResourceId());
            if (building != null) {
                preferences.getBuildingPreference()
                        .merge(building, 1, Integer::sum);
            }

            // Time slot preference
            String timeSlot = getTimeSlot(reservation.getStartDatetime());
            preferences.getTimeSlotPreference()
                    .merge(timeSlot, 1, Integer::sum);

            // Capacity preference
            Integer capacity = getResourceCapacity(reservation.getResourceType(),
                    reservation.getResourceId());
            if (capacity != null) {
                preferences.getCapacityPreference()
                        .merge(capacity, 1, Integer::sum);
            }
        });

        return preferences;
    }

    private String getResourceBuilding(ResourceType type, Long resourceId) {
        // This would need repository access - simplified for now
        // You'll implement this properly with your repositories
        return null; // Placeholder
    }

    private Integer getResourceCapacity(ResourceType type, Long resourceId) {
        // This would need repository access - simplified for now
        return null; // Placeholder
    }

    private String getTimeSlot(LocalDateTime time) {
        int hour = time.getHour();
        if (hour < 12) return "MORNING";
        if (hour < 17) return "AFTERNOON";
        return "EVENING";
    }
}
