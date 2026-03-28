package com.university.backend.hr.controllers;

import com.university.backend.hr.dto.LeaveRequestRequest;
import com.university.backend.hr.entities.LeaveRequest;
import com.university.backend.hr.services.LeaveRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hr/leave-requests")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('LOGISTICS_STAFF', 'SUPER_ADMIN')")
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    @GetMapping
    public ResponseEntity<List<LeaveRequest>> list(@RequestParam(required = false) String employeeId) {
        if (employeeId != null) {
            return ResponseEntity.ok(leaveRequestService.findByEmployeeId(employeeId));
        }
        return ResponseEntity.ok(leaveRequestService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeaveRequest> get(@PathVariable String id) {
        return ResponseEntity.ok(leaveRequestService.findById(id));
    }

    @PostMapping
    public ResponseEntity<LeaveRequest> create(@Valid @RequestBody LeaveRequestRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(leaveRequestService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LeaveRequest> update(
            @PathVariable String id,
            @Valid @RequestBody LeaveRequestRequest request
    ) {
        return ResponseEntity.ok(leaveRequestService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        leaveRequestService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Prefer {@code POST /api/hr/leave-requests/{id}/approve} on {@link com.university.backend.hr.web.HrController}
     * (HR admin dashboard). This endpoint calls the same service method as {@code approve}.
     *
     * @deprecated Retained for backward compatibility; new clients should use {@code /api/hr/leave-requests/{id}/approve}.
     */
    @Deprecated(forRemoval = false)
    @PostMapping("/{id}/process")
    public ResponseEntity<LeaveRequest> processLeaveRequest(@PathVariable String id) {
        return ResponseEntity.ok(leaveRequestService.processLeaveRequest(id));
    }
}
