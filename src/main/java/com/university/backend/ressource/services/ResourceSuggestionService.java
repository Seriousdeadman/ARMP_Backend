package com.university.backend.ressource.services;

import com.university.backend.ressource.dto.ResourceSuggestionRequest;
import com.university.backend.ressource.dto.ResourceSuggestionResponse;
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
public class ResourceSuggestionService {

    private final ClassroomRepository classroomRepository;
    private final LaboratoryRepository laboratoryRepository;
    private final CollaborativeSpaceRepository collaborativeSpaceRepository;
    private final EquipmentRepository equipmentRepository;
    private final ReservationRepository reservationRepository;

    public List<ResourceSuggestionResponse> suggestResources(ResourceSuggestionRequest request) {
        System.out.println("=== SUGGESTION REQUEST START ===");
        System.out.println("Resource Type: " + request.getResourceType());
        System.out.println("Start: " + request.getStartDatetime());
        System.out.println("End: " + request.getEndDatetime());

        List<ResourceSuggestionResponse> results = new ArrayList<>();

        try {
            switch (request.getResourceType()) {
                case CLASSROOM:
                    results = getClassroomSuggestions(request);
                    break;
                case LABORATORY:
                    results = getLaboratorySuggestions(request);
                    break;
                case COLLABORATIVE_SPACE:
                    results = getCollaborativeSpaceSuggestions(request);
                    break;
                case EQUIPMENT:
                    results = getEquipmentSuggestions(request);
                    break;
            }
        } catch (Exception e) {
            System.err.println("Error in suggestResources: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("Returning " + results.size() + " suggestions");
        results.forEach(r -> System.out.println("  - " + r.getName() + " (Score: " + r.getScore() + ")"));
        System.out.println("=== SUGGESTION REQUEST END ===");

        return results;
    }

    private List<ResourceSuggestionResponse> getClassroomSuggestions(ResourceSuggestionRequest request) {
        List<Classroom> classrooms = classroomRepository.findAll().stream()
                .filter(c -> c.getStatus() == ResourceStatus.AVAILABLE)
                .collect(Collectors.toList());

        System.out.println("Found " + classrooms.size() + " available classrooms");

        return classrooms.stream()
                .limit(request.getLimit())
                .map(c -> {
                    boolean isAvailable = checkAvailability(
                            ResourceType.CLASSROOM,
                            c.getId(),
                            request.getStartDatetime(),
                            request.getEndDatetime()
                    );

                    double score = calculateScore(c.getCapacity(),
                            request.getMinCapacity(),
                            request.getMaxCapacity(),
                            c.getBuilding(),
                            request.getBuilding(),
                            isAvailable);

                    String reason = buildReason(c, request, isAvailable, score);

                    return ResourceSuggestionResponse.builder()
                            .id(c.getId())
                            .name(c.getName())
                            .resourceType(ResourceType.CLASSROOM)
                            .capacity(c.getCapacity())
                            .building(c.getBuilding())
                            .roomNumber(c.getRoomNumber())
                            .score(score)
                            .reason(reason)
                            .available(isAvailable)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<ResourceSuggestionResponse> getLaboratorySuggestions(ResourceSuggestionRequest request) {
        List<Laboratory> labs = laboratoryRepository.findAll().stream()
                .filter(l -> l.getStatus() == ResourceStatus.AVAILABLE)
                .collect(Collectors.toList());

        System.out.println("Found " + labs.size() + " available laboratories");

        return labs.stream()
                .limit(request.getLimit())
                .map(l -> {
                    boolean isAvailable = checkAvailability(
                            ResourceType.LABORATORY,
                            l.getId(),
                            request.getStartDatetime(),
                            request.getEndDatetime()
                    );

                    double score = calculateScore(l.getCapacity(),
                            request.getMinCapacity(),
                            request.getMaxCapacity(),
                            l.getBuilding(),
                            request.getBuilding(),
                            isAvailable);

                    String reason = buildReason(l, request, isAvailable, score);

                    return ResourceSuggestionResponse.builder()
                            .id(l.getId())
                            .name(l.getName())
                            .resourceType(ResourceType.LABORATORY)
                            .capacity(l.getCapacity())
                            .building(l.getBuilding())
                            .roomNumber(l.getRoomNumber())
                            .score(score)
                            .reason(reason)
                            .available(isAvailable)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<ResourceSuggestionResponse> getCollaborativeSpaceSuggestions(ResourceSuggestionRequest request) {
        List<CollaborativeSpace> spaces = collaborativeSpaceRepository.findAll().stream()
                .filter(s -> s.getStatus() == ResourceStatus.AVAILABLE)
                .collect(Collectors.toList());

        System.out.println("Found " + spaces.size() + " available collaborative spaces");

        return spaces.stream()
                .limit(request.getLimit())
                .map(s -> {
                    boolean isAvailable = checkAvailability(
                            ResourceType.COLLABORATIVE_SPACE,
                            s.getId(),
                            request.getStartDatetime(),
                            request.getEndDatetime()
                    );

                    double score = calculateScore(s.getCapacity(),
                            request.getMinCapacity(),
                            request.getMaxCapacity(),
                            s.getBuilding(),
                            request.getBuilding(),
                            isAvailable);

                    String reason = buildReason(s, request, isAvailable, score);

                    return ResourceSuggestionResponse.builder()
                            .id(s.getId())
                            .name(s.getName())
                            .resourceType(ResourceType.COLLABORATIVE_SPACE)
                            .capacity(s.getCapacity())
                            .building(s.getBuilding())
                            .roomNumber(s.getRoomNumber())
                            .score(score)
                            .reason(reason)
                            .available(isAvailable)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<ResourceSuggestionResponse> getEquipmentSuggestions(ResourceSuggestionRequest request) {
        List<Equipment> equipmentList = equipmentRepository.findAll().stream()
                .filter(e -> e.getStatus() == ResourceStatus.AVAILABLE)
                .collect(Collectors.toList());

        System.out.println("Found " + equipmentList.size() + " available equipment items");

        return equipmentList.stream()
                .limit(request.getLimit())
                .map(e -> {
                    boolean isAvailable = checkAvailability(
                            ResourceType.EQUIPMENT,
                            e.getId(),
                            request.getStartDatetime(),
                            request.getEndDatetime()
                    );

                    // Equipment doesn't have capacity/building constraints
                    double score = isAvailable ? 85.0 : 0.0;

                    String reason = isAvailable ?
                            "Available for the requested time" :
                            "Not available during requested time";

                    return ResourceSuggestionResponse.builder()
                            .id(e.getId())
                            .name(e.getName())
                            .resourceType(ResourceType.EQUIPMENT)
                            .capacity(null)
                            .building(null)
                            .roomNumber(null)
                            .score(score)
                            .reason(reason)
                            .available(isAvailable)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private double calculateScore(Integer resourceCapacity,
                                  Integer minCapacity,
                                  Integer maxCapacity,
                                  String resourceBuilding,
                                  String preferredBuilding,
                                  boolean isAvailable) {
        if (!isAvailable) return 0.0;

        double score = 70.0; // Base score for available resources

        // Capacity scoring
        if (resourceCapacity != null) {
            if (minCapacity != null && resourceCapacity < minCapacity) {
                score -= 30;
            } else if (maxCapacity != null && resourceCapacity > maxCapacity) {
                score -= 20;
            } else if (minCapacity != null) {
                // Bonus for good capacity match
                int diff = Math.abs(resourceCapacity - minCapacity);
                if (diff == 0) score += 20;
                else if (diff <= 5) score += 10;
                else if (diff <= 10) score += 5;
            }
        }

        // Building scoring
        if (preferredBuilding != null && !preferredBuilding.isEmpty() && resourceBuilding != null) {
            if (resourceBuilding.equalsIgnoreCase(preferredBuilding)) {
                score += 15;
            } else {
                score -= 5;
            }
        }

        return Math.max(0, Math.min(100, score));
    }

    private String buildReason(Object resource,
                               ResourceSuggestionRequest request,
                               boolean isAvailable,
                               double score) {
        if (!isAvailable) {
            return "This resource is already booked for your selected time";
        }

        List<String> reasons = new ArrayList<>();

        if (resource instanceof Classroom) {
            Classroom c = (Classroom) resource;
            if (request.getMinCapacity() != null && c.getCapacity() >= request.getMinCapacity()) {
                reasons.add("✓ Capacity fits your needs (" + c.getCapacity() + " seats)");
            }
            if (request.getBuilding() != null && c.getBuilding().equalsIgnoreCase(request.getBuilding())) {
                reasons.add("✓ Located in your preferred building");
            }
        } else if (resource instanceof Laboratory) {
            Laboratory l = (Laboratory) resource;
            if (request.getMinCapacity() != null && l.getCapacity() >= request.getMinCapacity()) {
                reasons.add("✓ Capacity fits your needs (" + l.getCapacity() + " seats)");
            }
            if (request.getBuilding() != null && l.getBuilding().equalsIgnoreCase(request.getBuilding())) {
                reasons.add("✓ Located in your preferred building");
            }
        } else if (resource instanceof CollaborativeSpace) {
            CollaborativeSpace s = (CollaborativeSpace) resource;
            if (request.getMinCapacity() != null && s.getCapacity() >= request.getMinCapacity()) {
                reasons.add("✓ Capacity fits your needs (" + s.getCapacity() + " seats)");
            }
            if (request.getBuilding() != null && s.getBuilding().equalsIgnoreCase(request.getBuilding())) {
                reasons.add("✓ Located in your preferred building");
            }
        }

        if (reasons.isEmpty()) {
            reasons.add("✓ Available for your requested time");
        }

        reasons.add(String.format("✓ Match score: %.0f%%", score));

        return String.join(" • ", reasons);
    }

    private boolean checkAvailability(ResourceType type, Long resourceId, LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return true;
        return !reservationRepository.existsConflict(type, resourceId, start, end);
    }
}