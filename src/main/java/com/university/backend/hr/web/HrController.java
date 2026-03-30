package com.university.backend.hr.web;

import com.university.backend.hr.dto.CandidateRecruitmentDto;
import com.university.backend.hr.dto.EmployeeDirectoryDto;
import com.university.backend.hr.dto.LeaveRequestAdminDto;
import com.university.backend.hr.dto.RecruitmentAssignmentDto;
import com.university.backend.hr.entities.LeaveRequest;
import com.university.backend.entities.User;
import com.university.backend.hr.services.HrDashboardService;
import com.university.backend.hr.services.LeaveRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/hr")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('LOGISTICS_STAFF', 'SUPER_ADMIN')")
public class HrController {

    private final HrDashboardService hrDashboardService;
    private final LeaveRequestService leaveRequestService;

    @GetMapping("/candidates/recruitment")
    public ResponseEntity<List<CandidateRecruitmentDto>> listRecruitmentCandidates() {
        return ResponseEntity.ok(hrDashboardService.listCandidatesForRecruitment());
    }

    @GetMapping("/candidates/recruitment/assignments")
    public ResponseEntity<List<RecruitmentAssignmentDto>> listRecruitmentAssignments() {
        return ResponseEntity.ok(hrDashboardService.listRecruitmentAssignments());
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

    /**
     * Approve a leave request. RBAC + self-moderation guard are the security layer;
     * password confirmation has been replaced by role-based access control.
     */
    @PostMapping("/leave-requests/{id}/approve")
    public ResponseEntity<LeaveRequest> approveLeave(
            @AuthenticationPrincipal User user,
            @PathVariable String id
    ) {
        leaveRequestService.assertApproverNotRequester(user, id);
        return ResponseEntity.ok(leaveRequestService.approveLeaveRequest(id));
    }

    @PostMapping("/leave-requests/{id}/reject")
    public ResponseEntity<LeaveRequest> rejectLeave(
            @AuthenticationPrincipal User user,
            @PathVariable String id
    ) {
        leaveRequestService.assertApproverNotRequester(user, id);
        return ResponseEntity.ok(leaveRequestService.rejectLeaveRequest(id));
    }
}
