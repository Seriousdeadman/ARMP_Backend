// com.university.backend.ressource.services.ConflictResolutionService.java
package com.university.backend.ressource.services;

import com.university.backend.ressource.dto.ConflictAlternative;
import com.university.backend.ressource.dto.ConflictResolutionRequest;
import com.university.backend.ressource.dto.UserPreference;
import com.university.backend.ressource.entities.*;
import com.university.backend.ressource.enums.ResourceStatus;
import com.university.backend.ressource.enums.ResourceType;
import com.university.backend.ressource.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConflictResolutionService {

    private final ClassroomRepository classroomRepository;
    private final LaboratoryRepository laboratoryRepository;
    private final CollaborativeSpaceRepository collaborativeSpaceRepository;
    private final EquipmentRepository equipmentRepository;
    private final ReservationRepository reservationRepository;
    private final UserPreferenceService userPreferenceService;
    private final ResourceSuggestionService suggestionService; // Your existing service

    /**
     * Main method to resolve conflicts
     */
    public List<ConflictAlternative> resolveConflict(ConflictResolutionRequest request) {
        List<ConflictAlternative> alternatives = new ArrayList<>();

        // Get user preferences for personalized suggestions
        UserPreference preferences = userPreferenceService.getUserPreferences(request.getUserId());

        // Strategy 1: Same time, same type, different resource
        alternatives.addAll(findSameTimeAlternatives(request, preferences));

        // Strategy 2: Time-shifted alternatives (±1, ±2 hours)
        alternatives.addAll(findTimeShiftedAlternatives(request, preferences));

        // Strategy 3: Different resource type alternatives
        alternatives.addAll(findAlternativeTypeAlternatives(request, preferences));

        // Remove duplicates and sort by score
        return alternatives.stream()
                .distinct()
                .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                .limit(request.getAlternativeCount())
                .collect(Collectors.toList());
    }

    /**
     * Strategy 1: Find same time, same resource type alternatives
     */
    private List<ConflictAlternative> findSameTimeAlternatives(
            ConflictResolutionRequest request, UserPreference preferences) {

        List<ConflictAlternative> alternatives = new ArrayList<>();
        List<?> resources = getResourcesByType(request.getResourceType());

        for (Object resource : resources) {
            Long resourceId = getResourceId(resource);

            // Skip the conflicted resource
            if (resourceId.equals(request.getResourceId())) continue;

            // Check availability at same time
            boolean isAvailable = !reservationRepository.existsConflict(
                    request.getResourceType(),
                    resourceId,
                    request.getStartDatetime(),
                    request.getEndDatetime()
            );

            if (isAvailable) {
                double score = calculateScore(resource, request, preferences, "SAME_TIME");

                alternatives.add(buildAlternative(resource, request,
                        request.getStartDatetime(), request.getEndDatetime(),
                        score, "SAME_TIME", "Available at the same time"));
            }
        }

        return alternatives;
    }

    /**
     * Strategy 2: Find time-shifted alternatives (±1, ±2 hours)
     */
    private List<ConflictAlternative> findTimeShiftedAlternatives(
            ConflictResolutionRequest request, UserPreference preferences) {

        List<ConflictAlternative> alternatives = new ArrayList<>();
        int[] shifts = {1, 2, -1, -2}; // +1h, +2h, -1h, -2h

        for (int shift : shifts) {
            LocalDateTime newStart = request.getStartDatetime().plusHours(shift);
            LocalDateTime newEnd = request.getEndDatetime().plusHours(shift);

            // Check if within operating hours (8 AM - 8 PM)
            if (newStart.getHour() < 8 || newEnd.getHour() > 20) continue;
            if (newStart.isBefore(LocalDateTime.now())) continue;

            // Check availability of original resource at new time
            boolean isAvailable = !reservationRepository.existsConflict(
                    request.getResourceType(),
                    request.getResourceId(),
                    newStart,
                    newEnd
            );

            if (isAvailable) {
                String resourceName = getResourceName(request.getResourceType(), request.getResourceId());
                String shiftDesc = shift > 0 ? shift + " hours later" : Math.abs(shift) + " hours earlier";
                double score = calculateTimeShiftScore(shift, preferences);

                alternatives.add(ConflictAlternative.builder()
                        .id(request.getResourceId())
                        .name(resourceName)
                        .resourceType(request.getResourceType())
                        .capacity(getResourceCapacity(request.getResourceType(), request.getResourceId()))
                        .building(getResourceBuilding(request.getResourceType(), request.getResourceId()))
                        .roomNumber(getResourceRoomNumber(request.getResourceType(), request.getResourceId()))
                        .startDatetime(newStart)
                        .endDatetime(newEnd)
                        .score(score)
                        .reason("Same resource, " + shiftDesc)
                        .changeType("TIME_SHIFT")
                        .shiftDescription(shiftDesc)
                        .build());
            }
        }

        return alternatives;
    }

    /**
     * Strategy 3: Find alternative resource types
     */
    private List<ConflictAlternative> findAlternativeTypeAlternatives(
            ConflictResolutionRequest request, UserPreference preferences) {

        List<ConflictAlternative> alternatives = new ArrayList<>();
        ResourceType[] types = ResourceType.values();

        for (ResourceType type : types) {
            if (type == request.getResourceType()) continue;

            List<?> resources = getResourcesByType(type);

            for (Object resource : resources) {
                Long resourceId = getResourceId(resource);

                boolean isAvailable = !reservationRepository.existsConflict(
                        type,
                        resourceId,
                        request.getStartDatetime(),
                        request.getEndDatetime()
                );

                if (isAvailable) {
                    double score = calculateAlternativeTypeScore(resource, request, type, preferences);

                    alternatives.add(buildAlternative(resource, request,
                            request.getStartDatetime(), request.getEndDatetime(),
                            score, "ALTERNATIVE_TYPE", "Different resource type available"));
                }
            }
        }

        return alternatives;
    }

    /**
     * Calculate score based on multiple factors
     */
    private double calculateScore(Object resource, ConflictResolutionRequest request,
                                  UserPreference preferences, String strategy) {
        double score = 70.0; // Base score

        // Capacity matching
        Integer resourceCapacity = getResourceCapacity(
                request.getResourceType(), getResourceId(resource));
        Integer preferredCapacity = userPreferenceService.getPreferredCapacity(request.getUserId());

        if (resourceCapacity != null && preferredCapacity != null) {
            int diff = Math.abs(resourceCapacity - preferredCapacity);
            if (diff == 0) score += 20;
            else if (diff <= 5) score += 15;
            else if (diff <= 10) score += 10;
            else score -= 10;
        }

        // Building preference
        String resourceBuilding = getResourceBuilding(
                request.getResourceType(), getResourceId(resource));
        String preferredBuilding = userPreferenceService.getPreferredBuilding(request.getUserId());

        if (resourceBuilding != null && preferredBuilding != null) {
            if (resourceBuilding.equals(preferredBuilding)) {
                score += 15;
            }
        }

        // Time slot preference
        String timeSlot = getTimeSlot(request.getStartDatetime());
        Map<String, Integer> timePref = preferences.getTimeSlotPreference();
        if (timePref.containsKey(timeSlot)) {
            score += Math.min(10, timePref.get(timeSlot) * 2);
        }

        return Math.min(100, Math.max(0, score));
    }

    private double calculateTimeShiftScore(int shiftHours, UserPreference preferences) {
        double score = 60.0;
        // Smaller shift is better
        score += (shiftHours > 0 ? 15 : 10) - Math.abs(shiftHours) * 5;
        return Math.min(100, Math.max(0, score));
    }

    private double calculateAlternativeTypeScore(Object resource, ConflictResolutionRequest request,
                                                 ResourceType newType, UserPreference preferences) {
        double score = 50.0;

        // Check if user prefers this resource type
        Map<ResourceType, Integer> typePref = preferences.getResourceTypePreference();
        if (typePref.containsKey(newType)) {
            score += Math.min(20, typePref.get(newType) * 5);
        }

        return Math.min(100, Math.max(0, score));
    }

    private String getTimeSlot(LocalDateTime time) {
        int hour = time.getHour();
        if (hour < 12) return "MORNING";
        if (hour < 17) return "AFTERNOON";
        return "EVENING";
    }

    private ConflictAlternative buildAlternative(Object resource, ConflictResolutionRequest request,
                                                 LocalDateTime start, LocalDateTime end,
                                                 double score, String type, String reason) {
        return ConflictAlternative.builder()
                .id(getResourceId(resource))
                .name(getResourceName(request.getResourceType(), getResourceId(resource)))
                .resourceType(request.getResourceType())
                .capacity(getResourceCapacity(request.getResourceType(), getResourceId(resource)))
                .building(getResourceBuilding(request.getResourceType(), getResourceId(resource)))
                .roomNumber(getResourceRoomNumber(request.getResourceType(), getResourceId(resource)))
                .startDatetime(start)
                .endDatetime(end)
                .score(score)
                .reason(reason)
                .changeType(type)
                .build();
    }

    // Helper methods to get resource details
    private List<?> getResourcesByType(ResourceType type) {
        switch (type) {
            case CLASSROOM:
                return classroomRepository.findAll().stream()
                        .filter(c -> c.getStatus() == ResourceStatus.AVAILABLE)
                        .collect(Collectors.toList());
            case LABORATORY:
                return laboratoryRepository.findAll().stream()
                        .filter(l -> l.getStatus() == ResourceStatus.AVAILABLE)
                        .collect(Collectors.toList());
            case COLLABORATIVE_SPACE:
                return collaborativeSpaceRepository.findAll().stream()
                        .filter(s -> s.getStatus() == ResourceStatus.AVAILABLE)
                        .collect(Collectors.toList());
            case EQUIPMENT:
                return equipmentRepository.findAll().stream()
                        .filter(e -> e.getStatus() == ResourceStatus.AVAILABLE)
                        .collect(Collectors.toList());
            default:
                return new ArrayList<>();
        }
    }

    private Long getResourceId(Object resource) {
        if (resource instanceof Classroom) return ((Classroom) resource).getId();
        if (resource instanceof Laboratory) return ((Laboratory) resource).getId();
        if (resource instanceof CollaborativeSpace) return ((CollaborativeSpace) resource).getId();
        if (resource instanceof Equipment) return ((Equipment) resource).getId();
        return null;
    }

    private String getResourceName(ResourceType type, Long resourceId) {
        switch (type) {
            case CLASSROOM:
                return classroomRepository.findById(resourceId)
                        .map(Classroom::getName).orElse("Unknown");
            case LABORATORY:
                return laboratoryRepository.findById(resourceId)
                        .map(Laboratory::getName).orElse("Unknown");
            case COLLABORATIVE_SPACE:
                return collaborativeSpaceRepository.findById(resourceId)
                        .map(CollaborativeSpace::getName).orElse("Unknown");
            case EQUIPMENT:
                return equipmentRepository.findById(resourceId)
                        .map(Equipment::getName).orElse("Unknown");
            default:
                return "Unknown";
        }
    }

    private Integer getResourceCapacity(ResourceType type, Long resourceId) {
        switch (type) {
            case CLASSROOM:
                return classroomRepository.findById(resourceId)
                        .map(Classroom::getCapacity).orElse(null);
            case LABORATORY:
                return laboratoryRepository.findById(resourceId)
                        .map(Laboratory::getCapacity).orElse(null);
            case COLLABORATIVE_SPACE:
                return collaborativeSpaceRepository.findById(resourceId)
                        .map(CollaborativeSpace::getCapacity).orElse(null);
            default:
                return null;
        }
    }

    private String getResourceBuilding(ResourceType type, Long resourceId) {
        switch (type) {
            case CLASSROOM:
                return classroomRepository.findById(resourceId)
                        .map(Classroom::getBuilding).orElse(null);
            case LABORATORY:
                return laboratoryRepository.findById(resourceId)
                        .map(Laboratory::getBuilding).orElse(null);
            case COLLABORATIVE_SPACE:
                return collaborativeSpaceRepository.findById(resourceId)
                        .map(CollaborativeSpace::getBuilding).orElse(null);
            default:
                return null;
        }
    }

    private String getResourceRoomNumber(ResourceType type, Long resourceId) {
        switch (type) {
            case CLASSROOM:
                return classroomRepository.findById(resourceId)
                        .map(Classroom::getRoomNumber).orElse(null);
            case LABORATORY:
                return laboratoryRepository.findById(resourceId)
                        .map(Laboratory::getRoomNumber).orElse(null);
            case COLLABORATIVE_SPACE:
                return collaborativeSpaceRepository.findById(resourceId)
                        .map(CollaborativeSpace::getRoomNumber).orElse(null);
            default:
                return null;
        }
    }
}