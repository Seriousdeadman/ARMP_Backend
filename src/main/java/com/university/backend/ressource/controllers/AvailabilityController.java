// com.university.backend.ressource.controllers.AvailabilityController.java
package com.university.backend.ressource.controllers;

import com.university.backend.ressource.dto.AvailabilityRequest;
import com.university.backend.ressource.dto.AvailabilitySlot;
import com.university.backend.ressource.enums.ResourceType;
import com.university.backend.ressource.services.AvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/availability")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    /**
     * Get availability for a specific resource (for calendar view)
     */
    @PostMapping("/resource")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AvailabilitySlot>> getResourceAvailability(
            @RequestBody AvailabilityRequest request) {
        return ResponseEntity.ok(availabilityService.getResourceAvailability(request));
    }

    /**
     * Get availability for all resources of a type
     */
    @GetMapping("/all/{type}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<Long, List<AvailabilitySlot>>> getAllResourcesAvailability(
            @PathVariable ResourceType type,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(availabilityService.getAllResourcesAvailability(type, start, end));
    }

    /**
     * Check if a specific time slot is available
     */
    @GetMapping("/check")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Boolean> checkAvailability(
            @RequestParam ResourceType type,
            @RequestParam Long resourceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(availabilityService.isTimeSlotAvailable(type, resourceId, start, end));
    }

    /**
     * Get available time slots for a specific day
     */
    @GetMapping("/day-slots")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AvailabilitySlot>> getAvailableDaySlots(
            @RequestParam ResourceType type,
            @RequestParam Long resourceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date) {
        return ResponseEntity.ok(availabilityService.getAvailableTimeSlotsForDay(type, resourceId, date));
    }
}