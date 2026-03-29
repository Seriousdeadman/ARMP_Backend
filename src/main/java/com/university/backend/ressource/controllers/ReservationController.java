package com.university.backend.ressource.controllers;

import com.university.backend.entities.User;
import com.university.backend.ressource.dto.ReservationRequest;
import com.university.backend.ressource.dto.ReservationResponse;
import com.university.backend.ressource.enums.ResourceType;
import com.university.backend.ressource.services.ReservationService;
import com.university.backend.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ReservationController {

    private final ReservationService reservationService;

    // Any authenticated user can create a reservation
    @PostMapping
    public ResponseEntity<ReservationResponse> create(
            @AuthenticationPrincipal User user,
            @RequestBody ReservationRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservationService.create(user.getId(), request));
    }

    // Get current user's own reservations
    @GetMapping("/my")
    public ResponseEntity<List<ReservationResponse>> getMyReservations(
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(reservationService.getMyReservations(user.getId()));
    }

    // Staff/Admin can see all reservations
    @PreAuthorize("hasAnyRole('LOGISTICS_STAFF', 'SUPER_ADMIN')")
    @GetMapping
    public ResponseEntity<List<ReservationResponse>> getAll() {
        return ResponseEntity.ok(reservationService.getAllReservations());
    }

    // Get reservations for a specific resource
    @GetMapping("/resource/{type}/{id}")
    public ResponseEntity<List<ReservationResponse>> getByResource(
            @PathVariable ResourceType type,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(reservationService.getByResource(type, id));
    }

    // Cancel a reservation
    @PutMapping("/{id}/cancel")
    public ResponseEntity<ReservationResponse> cancel(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        boolean isStaff = user.getRole() == UserRole.LOGISTICS_STAFF ||
                user.getRole() == UserRole.SUPER_ADMIN;
        return ResponseEntity.ok(reservationService.cancel(id, user.getId(), isStaff));
    }
}