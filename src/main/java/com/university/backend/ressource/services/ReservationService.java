// com.university.backend.ressource.services.ReservationService.java
package com.university.backend.ressource.services;

import com.university.backend.ressource.dto.ReservationRequest;
import com.university.backend.ressource.dto.ReservationResponse;
import com.university.backend.ressource.entities.*;
import com.university.backend.ressource.enums.ReservationStatus;
import com.university.backend.ressource.enums.ResourceType;
import com.university.backend.ressource.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ClassroomRepository classroomRepository;
    private final LaboratoryRepository laboratoryRepository;
    private final CollaborativeSpaceRepository collaborativeSpaceRepository;
    private final EquipmentRepository equipmentRepository;

    // Create a new reservation
    public ReservationResponse create(String userId, ReservationRequest request) {
        // 1. Validate time range
        if (request.getStartDatetime().isAfter(request.getEndDatetime()) ||
                request.getStartDatetime().isEqual(request.getEndDatetime())) {
            throw new RuntimeException("Start time must be before end time.");
        }

        if (request.getStartDatetime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Cannot reserve in the past.");
        }

        // 2. Check resource exists and is AVAILABLE
        validateResourceAvailable(request.getResourceType(), request.getResourceId());

        // 3. Check for time slot conflicts
        boolean conflict = reservationRepository.existsConflict(
                request.getResourceType(),
                request.getResourceId(),
                request.getStartDatetime(),
                request.getEndDatetime()
        );

        if (conflict) {
            throw new RuntimeException("This resource is already reserved for the selected time slot.");
        }

        // 4. Create the reservation
        Reservation reservation = Reservation.builder()
                .userId(userId)
                .resourceType(request.getResourceType())
                .resourceId(request.getResourceId())
                .startDatetime(request.getStartDatetime())
                .endDatetime(request.getEndDatetime())
                .status(ReservationStatus.ACTIVE)
                .build();

        return toResponse(reservationRepository.save(reservation));
    }

    // Get user's reservations - ONLY ACTIVE AND FUTURE
    public List<ReservationResponse> getMyReservations(String userId) {
        LocalDateTime now = LocalDateTime.now();

        return reservationRepository.findActiveFutureReservations(userId, now)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // Get user's past reservations (for history/stats)
    public List<ReservationResponse> getMyPastReservations(String userId) {
        LocalDateTime now = LocalDateTime.now();

        return reservationRepository.findPastReservations(userId, now)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // Get all reservations for admin (includes past and cancelled)
    public List<ReservationResponse> getAllReservationsForAdmin() {
        return reservationRepository.findAllByOrderByStartDatetimeDesc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // Get all active reservations for admin
    public List<ReservationResponse> getAllActiveReservations() {
        return reservationRepository.findAllActiveReservations()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // Get upcoming active reservations
    public List<ReservationResponse> getUpcomingReservations() {
        LocalDateTime now = LocalDateTime.now();
        return reservationRepository.findUpcomingActiveReservations(now)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // Get all cancelled reservations
    public List<ReservationResponse> getAllCancelledReservations() {
        return reservationRepository.findAllCancelledReservations()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // Get reservations by resource
    public List<ReservationResponse> getByResource(ResourceType type, Long resourceId) {
        return reservationRepository
                .findByResourceTypeAndResourceIdOrderByStartDatetimeDesc(type, resourceId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // Cancel a reservation
    public ReservationResponse cancel(Long reservationId, String userId, boolean isStaff) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found."));

        // Only the owner or staff can cancel
        if (!reservation.getUserId().equals(userId) && !isStaff) {
            throw new RuntimeException("You are not allowed to cancel this reservation.");
        }

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new RuntimeException("Reservation is already cancelled.");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        return toResponse(reservationRepository.save(reservation));
    }

    // ========== ADMIN DASHBOARD STATS METHODS ==========

    public Map<String, Object> getReservationStats() {
        Map<String, Object> stats = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekAgo = now.minusDays(7);
        LocalDateTime monthAgo = now.minusDays(30);

        // Total reservations (all time)
        stats.put("totalReservations", reservationRepository.count());

        // Active reservations count
        stats.put("activeReservations", reservationRepository.countByStatus(ReservationStatus.ACTIVE));

        // Cancelled reservations count
        stats.put("cancelledReservations", reservationRepository.countByStatus(ReservationStatus.CANCELLED));

        // Upcoming reservations count
        stats.put("upcomingReservations", (long) reservationRepository.findUpcomingActiveReservations(now).size());

        // Reservations by resource type
        Map<String, Long> byType = new HashMap<>();
        for (ResourceType type : ResourceType.values()) {
            byType.put(type.name(), reservationRepository.countActiveByResourceType(type));
        }
        stats.put("reservationsByType", byType);

        // Weekly stats
        stats.put("weeklyReservations", reservationRepository.countByStartDatetimeBetween(weekAgo, now));

        // Monthly stats
        stats.put("monthlyReservations", reservationRepository.countByStartDatetimeBetween(monthAgo, now));

        // Get recent reservations (last 10)
        List<Reservation> recent = reservationRepository.findAllByOrderByStartDatetimeDesc()
                .stream()
                .limit(10)
                .collect(Collectors.toList());
        stats.put("recentReservations", recent.stream().map(this::toResponse).collect(Collectors.toList()));

        return stats;
    }

    private void validateResourceAvailable(ResourceType type, Long resourceId) {
        String status = switch (type) {
            case CLASSROOM -> classroomRepository.findById(resourceId)
                    .orElseThrow(() -> new RuntimeException("Classroom not found."))
                    .getStatus().name();
            case LABORATORY -> laboratoryRepository.findById(resourceId)
                    .orElseThrow(() -> new RuntimeException("Laboratory not found."))
                    .getStatus().name();
            case COLLABORATIVE_SPACE -> collaborativeSpaceRepository.findById(resourceId)
                    .orElseThrow(() -> new RuntimeException("Collaborative space not found."))
                    .getStatus().name();
            case EQUIPMENT -> equipmentRepository.findById(resourceId)
                    .orElseThrow(() -> new RuntimeException("Equipment not found."))
                    .getStatus().name();
        };

        if (!status.equals("AVAILABLE")) {
            throw new RuntimeException("This resource is not available for reservation.");
        }
    }

    private String getResourceName(ResourceType type, Long resourceId) {
        try {
            return switch (type) {
                case CLASSROOM -> classroomRepository.findById(resourceId)
                        .map(Classroom::getName).orElse("Unknown");
                case LABORATORY -> laboratoryRepository.findById(resourceId)
                        .map(Laboratory::getName).orElse("Unknown");
                case COLLABORATIVE_SPACE -> collaborativeSpaceRepository.findById(resourceId)
                        .map(CollaborativeSpace::getName).orElse("Unknown");
                case EQUIPMENT -> equipmentRepository.findById(resourceId)
                        .map(Equipment::getName).orElse("Unknown");
            };
        } catch (Exception e) {
            return "Unknown";
        }
    }

    private ReservationResponse toResponse(Reservation r) {
        return ReservationResponse.builder()
                .id(r.getId())
                .userId(r.getUserId())
                .resourceType(r.getResourceType())
                .resourceId(r.getResourceId())
                .resourceName(getResourceName(r.getResourceType(), r.getResourceId()))
                .startDatetime(r.getStartDatetime())
                .endDatetime(r.getEndDatetime())
                .status(r.getStatus())
                .createdAt(r.getCreatedAt())
                .build();
    }
}