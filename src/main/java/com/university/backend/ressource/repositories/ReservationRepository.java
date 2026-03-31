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

    // Get reservations by user (all)
    List<Reservation> findByUserIdOrderByStartDatetimeDesc(String userId);

    // Get all reservations ordered by start time
    List<Reservation> findAllByOrderByStartDatetimeDesc();

    // Get reservations by resource
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

    // ========== NEW METHODS FOR FILTERING ==========

    // Get only active and future reservations for a user
    @Query("SELECT r FROM Reservation r " +
            "WHERE r.userId = :userId " +
            "AND r.status = 'ACTIVE' " +
            "AND r.endDatetime > :now " +
            "ORDER BY r.startDatetime DESC")
    List<Reservation> findActiveFutureReservations(@Param("userId") String userId,
                                                   @Param("now") LocalDateTime now);

    // Get past reservations (ended or cancelled)
    @Query("SELECT r FROM Reservation r " +
            "WHERE r.userId = :userId " +
            "AND (r.status = 'CANCELLED' OR r.endDatetime < :now) " +
            "ORDER BY r.startDatetime DESC")
    List<Reservation> findPastReservations(@Param("userId") String userId,
                                           @Param("now") LocalDateTime now);

    // Get all active reservations (for admin)
    @Query("SELECT r FROM Reservation r " +
            "WHERE r.status = 'ACTIVE' " +
            "ORDER BY r.startDatetime DESC")
    List<Reservation> findAllActiveReservations();

    // Get upcoming active reservations (future)
    @Query("SELECT r FROM Reservation r " +
            "WHERE r.status = 'ACTIVE' " +
            "AND r.endDatetime > :now " +
            "ORDER BY r.startDatetime ASC")
    List<Reservation> findUpcomingActiveReservations(@Param("now") LocalDateTime now);

    // Count active reservations by resource type
    @Query("SELECT COUNT(r) FROM Reservation r " +
            "WHERE r.resourceType = :type " +
            "AND r.status = 'ACTIVE'")
    long countActiveByResourceType(@Param("type") ResourceType type);

    // Count reservations by status
    long countByStatus(ReservationStatus status);

    // Count reservations between dates
    long countByStartDatetimeBetween(LocalDateTime start, LocalDateTime end);

    // Get all cancelled reservations
    @Query("SELECT r FROM Reservation r " +
            "WHERE r.status = 'CANCELLED' " +
            "ORDER BY r.startDatetime DESC")
    List<Reservation> findAllCancelledReservations();

    // Get reservations by date range for admin dashboard
    @Query("SELECT r FROM Reservation r " +
            "WHERE r.startDatetime BETWEEN :start AND :end " +
            "ORDER BY r.startDatetime DESC")
    List<Reservation> findReservationsBetweenDates(@Param("start") LocalDateTime start,
                                                   @Param("end") LocalDateTime end);
}