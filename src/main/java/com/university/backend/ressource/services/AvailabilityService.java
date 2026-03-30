// Updated AvailabilityService.java - Fixed version
package com.university.backend.ressource.services;

import com.university.backend.ressource.dto.AvailabilitySlot;
import com.university.backend.ressource.dto.AvailabilityRequest;
import com.university.backend.ressource.entities.*;
import com.university.backend.ressource.enums.ResourceStatus;
import com.university.backend.ressource.enums.ResourceType;
import com.university.backend.ressource.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AvailabilityService {

    private final ClassroomRepository classroomRepository;
    private final LaboratoryRepository laboratoryRepository;
    private final CollaborativeSpaceRepository collaborativeSpaceRepository;
    private final EquipmentRepository equipmentRepository;
    private final ReservationRepository reservationRepository;

    private static final LocalTime OPENING_TIME = LocalTime.of(8, 0); // 8 AM
    private static final LocalTime CLOSING_TIME = LocalTime.of(20, 0); // 8 PM

    /**
     * Get availability for a specific resource
     */
    public List<AvailabilitySlot> getResourceAvailability(AvailabilityRequest request) {
        List<AvailabilitySlot> slots = new ArrayList<>();

        // Get existing reservations for the resource
        List<Reservation> reservations = reservationRepository
                .findByResourceTypeAndResourceIdAndStartDatetimeBetween(
                        request.getResourceType(),
                        request.getResourceId(),
                        request.getStart(),
                        request.getEnd()
                );

        String resourceName = getResourceName(request.getResourceType(), request.getResourceId());

        // Generate time slots (1-hour intervals) between start and end
        LocalDateTime current = request.getStart();
        while (current.isBefore(request.getEnd())) {
            final LocalDateTime slotStart = current; // Make effectively final for lambda
            LocalDateTime slotEnd = current.plusHours(1);
            final LocalDateTime slotEndFinal = slotEnd; // Make effectively final for lambda

            boolean isBooked = reservations.stream().anyMatch(r -> {
                LocalDateTime rStart = r.getStartDatetime();
                LocalDateTime rEnd = r.getEndDatetime();
                return (rStart.isBefore(slotEndFinal) && rEnd.isAfter(slotStart));
            });

            slots.add(AvailabilitySlot.builder()
                    .id(UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE)
                    .title(isBooked ? "Booked" : "Available")
                    .start(slotStart)
                    .end(slotEnd)
                    .color(isBooked ? "#ef4444" : "#10b981")
                    .available(!isBooked)
                    .resourceName(resourceName)
                    .resourceId(request.getResourceId())
                    .build());

            current = slotEnd;
        }

        return slots;
    }

    /**
     * Get availability for all resources of a type
     */
    public Map<Long, List<AvailabilitySlot>> getAllResourcesAvailability(
            ResourceType type, LocalDateTime start, LocalDateTime end) {

        Map<Long, List<AvailabilitySlot>> availabilityMap = new HashMap<>();
        List<?> resources = getResourcesByType(type);

        for (Object resource : resources) {
            Long resourceId = getResourceId(resource);
            AvailabilityRequest request = new AvailabilityRequest();
            request.setResourceType(type);
            request.setResourceId(resourceId);
            request.setStart(start);
            request.setEnd(end);

            availabilityMap.put(resourceId, getResourceAvailability(request));
        }

        return availabilityMap;
    }

    /**
     * Check if a specific time slot is available
     */
    public boolean isTimeSlotAvailable(ResourceType type, Long resourceId,
                                       LocalDateTime start, LocalDateTime end) {
        return !reservationRepository.existsConflict(type, resourceId, start, end);
    }

    /**
     * Get available time slots for a given day
     */
    public List<AvailabilitySlot> getAvailableTimeSlotsForDay(ResourceType type,
                                                              Long resourceId,
                                                              LocalDateTime date) {
        LocalDateTime dayStart = date.with(LocalTime.MIN);
        LocalDateTime dayEnd = date.with(LocalTime.MAX);

        List<Reservation> reservations = reservationRepository
                .findByResourceTypeAndResourceIdAndStartDatetimeBetween(
                        type, resourceId, dayStart, dayEnd);

        List<AvailabilitySlot> availableSlots = new ArrayList<>();
        LocalDateTime current = dayStart.with(OPENING_TIME);
        LocalDateTime dayClose = dayStart.with(CLOSING_TIME);

        while (current.isBefore(dayClose)) {
            final LocalDateTime slotStart = current; // Make effectively final
            LocalDateTime slotEnd = current.plusHours(1);
            final LocalDateTime slotEndFinal = slotEnd; // Make effectively final

            boolean isBooked = reservations.stream().anyMatch(r -> {
                LocalDateTime rStart = r.getStartDatetime();
                LocalDateTime rEnd = r.getEndDatetime();
                return (rStart.isBefore(slotEndFinal) && rEnd.isAfter(slotStart));
            });

            if (!isBooked) {
                availableSlots.add(AvailabilitySlot.builder()
                        .id(UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE)
                        .title("Available")
                        .start(slotStart)
                        .end(slotEnd)
                        .color("#10b981")
                        .available(true)
                        .resourceId(resourceId)
                        .build());
            }

            current = slotEnd;
        }

        return availableSlots;
    }

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
}