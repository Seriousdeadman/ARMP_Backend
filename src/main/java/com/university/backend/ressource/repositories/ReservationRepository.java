package com.university.backend.ressource.repositories;

import com.university.backend.ressource.entities.Reservation;
import com.university.backend.ressource.enums.ReservationStatus;
import com.university.backend.ressource.enums.ResourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // Get all reservations for a specific user
    List<Reservation> findByUserIdOrderByStartDatetimeDesc(String userId);

    // Get all reservations for a specific resource
    List<Reservation> findByResourceTypeAndResourceIdOrderByStartDatetimeDesc(
            ResourceType resourceType, Long resourceId
    );

    // Get all active reservations for a user
    List<Reservation> findByUserIdAndStatus(String userId, ReservationStatus status);

    // Check for time slot conflicts on a specific resource
    @Query("""
        SELECT COUNT(r) > 0 FROM Reservation r
        WHERE r.resourceType = :type
        AND r.resourceId = :resourceId
        AND r.status = 'ACTIVE'
        AND r.startDatetime < :end
        AND r.endDatetime > :start
    """)
    boolean existsConflict(
            @Param("type") ResourceType type,
            @Param("resourceId") Long resourceId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // Get all reservations (for admin/staff view)
    List<Reservation> findAllByOrderByStartDatetimeDesc();

    // Get all active reservations
    List<Reservation> findByStatusOrderByStartDatetimeDesc(ReservationStatus status);
}