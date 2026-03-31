// com.university.backend.ressource.controllers.ReservationController.java
package com.university.backend.ressource.controllers;

import com.university.backend.entities.User;
import com.university.backend.ressource.dto.*;
import com.university.backend.ressource.enums.ResourceType;
import com.university.backend.ressource.services.ReservationService;
import com.university.backend.enums.UserRole;
import com.university.backend.ressource.services.ConflictResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ReservationController {

    private final ReservationService reservationService;
    private final ConflictResolutionService conflictResolutionService;

    // Any authenticated user can create a reservation
    @PostMapping
    public ResponseEntity<ReservationResponse> create(
            @AuthenticationPrincipal User user,
            @RequestBody ReservationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservationService.create(user.getId(), request));
    }

    // Get current user's reservations - ONLY ACTIVE AND FUTURE
    @GetMapping("/my")
    public ResponseEntity<List<ReservationResponse>> getMyReservations(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(reservationService.getMyReservations(user.getId()));
    }

    // Get user's past reservations (for history)
    @GetMapping("/my/past")
    public ResponseEntity<List<ReservationResponse>> getMyPastReservations(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(reservationService.getMyPastReservations(user.getId()));
    }

    // Staff/Admin can see all reservations (including past and cancelled)
    @PreAuthorize("hasAnyRole('LOGISTICS_STAFF', 'SUPER_ADMIN')")
    @GetMapping
    public ResponseEntity<List<ReservationResponse>> getAll() {
        return ResponseEntity.ok(reservationService.getAllReservationsForAdmin());
    }

    // Staff/Admin can see only active reservations
    @PreAuthorize("hasAnyRole('LOGISTICS_STAFF', 'SUPER_ADMIN')")
    @GetMapping("/active")
    public ResponseEntity<List<ReservationResponse>> getAllActive() {
        return ResponseEntity.ok(reservationService.getAllActiveReservations());
    }

    // Staff/Admin can see upcoming reservations
    @PreAuthorize("hasAnyRole('LOGISTICS_STAFF', 'SUPER_ADMIN')")
    @GetMapping("/upcoming")
    public ResponseEntity<List<ReservationResponse>> getUpcoming() {
        return ResponseEntity.ok(reservationService.getUpcomingReservations());
    }

    // Staff/Admin can see cancelled reservations
    @PreAuthorize("hasAnyRole('LOGISTICS_STAFF', 'SUPER_ADMIN')")
    @GetMapping("/cancelled")
    public ResponseEntity<List<ReservationResponse>> getCancelled() {
        return ResponseEntity.ok(reservationService.getAllCancelledReservations());
    }

    // Get reservations for a specific resource
    @GetMapping("/resource/{type}/{id}")
    public ResponseEntity<List<ReservationResponse>> getByResource(
            @PathVariable ResourceType type,
            @PathVariable Long id) {
        return ResponseEntity.ok(reservationService.getByResource(type, id));
    }

    // Cancel a reservation
    @PutMapping("/{id}/cancel")
    public ResponseEntity<ReservationResponse> cancel(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        boolean isStaff = user.getRole() == UserRole.LOGISTICS_STAFF ||
                user.getRole() == UserRole.SUPER_ADMIN;
        return ResponseEntity.ok(reservationService.cancel(id, user.getId(), isStaff));
    }

    // Resolve a conflict with AI suggestions
    @PostMapping("/resolve-conflict")
    public ResponseEntity<List<ConflictAlternative>> resolveConflict(
            @AuthenticationPrincipal User user,
            @RequestBody ConflictResolutionRequest request) {
        request.setUserId(user.getId());
        List<ConflictAlternative> alternatives = conflictResolutionService.resolveConflict(request);
        return ResponseEntity.ok(alternatives);
    }

    // Smart booking with automatic conflict resolution
    @PostMapping("/smart-book")
    public ResponseEntity<?> smartBook(
            @AuthenticationPrincipal User user,
            @RequestBody SmartBookingRequest request) {
        try {
            ReservationRequest reservationRequest = ReservationRequest.builder()
                    .resourceType(request.getResourceType())
                    .resourceId(request.getResourceId())
                    .startDatetime(request.getStartDatetime())
                    .endDatetime(request.getEndDatetime())
                    .build();

            ReservationResponse reservation = reservationService.create(user.getId(), reservationRequest);
            return ResponseEntity.ok(reservation);

        } catch (RuntimeException e) {
            ConflictResolutionRequest conflictRequest = new ConflictResolutionRequest();
            conflictRequest.setResourceType(request.getResourceType());
            conflictRequest.setResourceId(request.getResourceId());
            conflictRequest.setStartDatetime(request.getStartDatetime());
            conflictRequest.setEndDatetime(request.getEndDatetime());
            conflictRequest.setUserId(user.getId());

            List<ConflictAlternative> alternatives = conflictResolutionService.resolveConflict(conflictRequest);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(alternatives);
        }
    }

    // ========== ADMIN DASHBOARD STATS ENDPOINTS ==========

    @PreAuthorize("hasAnyRole('LOGISTICS_STAFF', 'SUPER_ADMIN')")
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getReservationStats() {
        return ResponseEntity.ok(reservationService.getReservationStats());
    }
}