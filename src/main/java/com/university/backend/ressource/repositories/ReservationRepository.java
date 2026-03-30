// com.university.backend.ressource.repositories.ReservationRepository.java
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

    List<Reservation> findByUserIdOrderByStartDatetimeDesc(String userId);

    List<Reservation> findAllByOrderByStartDatetimeDesc();

    List<Reservation> findByResourceTypeAndResourceIdOrderByStartDatetimeDesc(
            ResourceType resourceType, Long resourceId);

    // Check for conflicts
    @Query("SELECT COUNT(r) > 0 FROM Reservation r " +
            "WHERE r.resourceType = :resourceType " +
            "AND r.resourceId = :resourceId " +
            "AND r.status = 'ACTIVE' " +
            "AND r.startDatetime < :endDateTime " +
            "AND r.endDatetime > :startDateTime")
    boolean existsConflict(@Param("resourceType") ResourceType resourceType,
                           @Param("resourceId") Long resourceId,
                           @Param("startDateTime") LocalDateTime startDateTime,
                           @Param("endDateTime") LocalDateTime endDateTime);

    // Find reservations between dates for a specific resource
    @Query("SELECT r FROM Reservation r " +
            "WHERE r.resourceType = :resourceType " +
            "AND r.resourceId = :resourceId " +
            "AND r.startDatetime >= :start " +
            "AND r.startDatetime <= :end " +
            "AND r.status = 'ACTIVE'")
    List<Reservation> findByResourceTypeAndResourceIdAndStartDatetimeBetween(
            @Param("resourceType") ResourceType resourceType,
            @Param("resourceId") Long resourceId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // Find all active reservations for a resource
    List<Reservation> findByResourceTypeAndResourceIdAndStatus(
            ResourceType resourceType, Long resourceId, ReservationStatus status);

    // Check if resource has any active reservations in time range
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Reservation r " +
            "WHERE r.resourceType = :resourceType " +
            "AND r.resourceId = :resourceId " +
            "AND r.status = 'ACTIVE' " +
            "AND r.startDatetime < :endDateTime " +
            "AND r.endDatetime > :startDateTime")
    boolean hasActiveReservationsInRange(@Param("resourceType") ResourceType resourceType,
                                         @Param("resourceId") Long resourceId,
                                         @Param("startDateTime") LocalDateTime startDateTime,
                                         @Param("endDateTime") LocalDateTime endDateTime);
}