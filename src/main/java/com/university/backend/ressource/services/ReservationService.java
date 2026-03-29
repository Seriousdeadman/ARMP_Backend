package com.university.backend.ressource.services;

import com.university.backend.ressource.dto.ReservationRequest;
import com.university.backend.ressource.dto.ReservationResponse;
import com.university.backend.ressource.entities.Reservation;
import com.university.backend.ressource.enums.ReservationStatus;
import com.university.backend.ressource.enums.ResourceType;
import com.university.backend.ressource.repositories.ReservationRepository;
import com.university.backend.ressource.repositories.ClassroomRepository;
import com.university.backend.ressource.repositories.LaboratoryRepository;
import com.university.backend.ressource.repositories.CollaborativeSpaceRepository;
import com.university.backend.ressource.repositories.EquipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ClassroomRepository classroomRepository;
    private final LaboratoryRepository laboratoryRepository;
    private final CollaborativeSpaceRepository collaborativeSpaceRepository;
    private final EquipmentRepository equipmentRepository;

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

    public List<ReservationResponse> getMyReservations(String userId) {
        return reservationRepository.findByUserIdOrderByStartDatetimeDesc(userId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<ReservationResponse> getAllReservations() {
        return reservationRepository.findAllByOrderByStartDatetimeDesc()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<ReservationResponse> getByResource(ResourceType type, Long resourceId) {
        return reservationRepository
                .findByResourceTypeAndResourceIdOrderByStartDatetimeDesc(type, resourceId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

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
                        .map(c -> c.getName()).orElse("Unknown");
                case LABORATORY -> laboratoryRepository.findById(resourceId)
                        .map(l -> l.getName()).orElse("Unknown");
                case COLLABORATIVE_SPACE -> collaborativeSpaceRepository.findById(resourceId)
                        .map(s -> s.getName()).orElse("Unknown");
                case EQUIPMENT -> equipmentRepository.findById(resourceId)
                        .map(e -> e.getName()).orElse("Unknown");
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