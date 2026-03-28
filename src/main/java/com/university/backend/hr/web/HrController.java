package com.university.backend.hr.web;

import com.university.backend.hr.dto.CandidateRecruitmentDto;
import com.university.backend.hr.dto.AdminPasswordRequest;
import com.university.backend.hr.dto.EmployeeDirectoryDto;
import com.university.backend.hr.dto.LeaveRequestAdminDto;
import com.university.backend.hr.entities.LeaveRequest;
import com.university.backend.entities.User;
import com.university.backend.hr.services.HrDashboardService;
import com.university.backend.hr.services.LeaveRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/hr")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('LOGISTICS_STAFF', 'SUPER_ADMIN')")
public class HrController {

    private final HrDashboardService hrDashboardService;
    private final LeaveRequestService leaveRequestService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/candidates/recruitment")
    public ResponseEntity<List<CandidateRecruitmentDto>> listRecruitmentCandidates() {
        return ResponseEntity.ok(hrDashboardService.listCandidatesForRecruitment());
    }

    @GetMapping("/leave-requests/pending")
    public ResponseEntity<List<LeaveRequestAdminDto>> listPendingLeaveRequests(
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(hrDashboardService.listPendingLeaveRequests(user.getEmail()));
    }

    @GetMapping("/employees/directory")
    public ResponseEntity<List<EmployeeDirectoryDto>> listEmployeeDirectory() {
        return ResponseEntity.ok(hrDashboardService.listEmployeesDirectory());
    }

    @PostMapping("/leave-requests/{id}/approve")
    public ResponseEntity<LeaveRequest> approveLeave(
            @AuthenticationPrincipal User user,
            @PathVariable String id,
            @Valid @RequestBody AdminPasswordRequest request
    ) {
        requireAdminPassword(user, request);
        preventSelfLeaveModeration(user, id);
        return ResponseEntity.ok(leaveRequestService.approveLeaveRequest(id));
    }

    @PostMapping("/leave-requests/{id}/reject")
    public ResponseEntity<LeaveRequest> rejectLeave(
            @AuthenticationPrincipal User user,
            @PathVariable String id,
            @Valid @RequestBody AdminPasswordRequest request
    ) {
        requireAdminPassword(user, request);
        preventSelfLeaveModeration(user, id);
        return ResponseEntity.ok(leaveRequestService.rejectLeaveRequest(id));
    }

    private void requireAdminPassword(User user, AdminPasswordRequest request) {
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid admin password");
        }
    }

    private void preventSelfLeaveModeration(User user, String leaveRequestId) {
        LeaveRequest leaveRequest = leaveRequestService.findById(leaveRequestId);
        String employeeEmail = leaveRequest.getEmployee() != null ? leaveRequest.getEmployee().getEmail() : null;
        if (employeeEmail != null && employeeEmail.equalsIgnoreCase(user.getEmail())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot approve or reject your own leave request");
        }
    }
}
