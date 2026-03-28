package com.university.backend.controllers;

import com.university.backend.entities.User;
import com.university.backend.hr.dto.CvFileMetadataDto;
import com.university.backend.hr.dto.DepartmentSummaryDto;
import com.university.backend.hr.dto.portal.ApplicantApplicationResponse;
import com.university.backend.hr.dto.portal.ApplicantApplicationUpsertRequest;
import com.university.backend.hr.dto.portal.ApplicationStatusResponse;
import com.university.backend.hr.dto.portal.CreateLeaveRequestDto;
import com.university.backend.hr.dto.portal.LeavePreviewResponse;
import com.university.backend.hr.dto.portal.LeaveSummaryResponse;
import com.university.backend.hr.dto.portal.PortalLeaveRequestRow;
import com.university.backend.hr.dto.portal.SubmittedLeaveRequestResponse;
import com.university.backend.hr.services.HrPortalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/hr/portal")
@RequiredArgsConstructor
public class HrPortalController {

    private final HrPortalService hrPortalService;

    @GetMapping("/application-status")
    public ResponseEntity<ApplicationStatusResponse> getApplicationStatus(
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(hrPortalService.getApplicationStatus(user));
    }

    @GetMapping("/departments")
    public ResponseEntity<List<DepartmentSummaryDto>> listCareerDepartments() {
        return ResponseEntity.ok(hrPortalService.listCareerDepartments());
    }

    @GetMapping("/my-application")
    public ResponseEntity<ApplicantApplicationResponse> getMyApplication(
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(hrPortalService.getMyApplication(user));
    }

    @PutMapping("/my-application")
    public ResponseEntity<ApplicantApplicationResponse> saveMyApplication(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ApplicantApplicationUpsertRequest body
    ) {
        return ResponseEntity.ok(hrPortalService.saveMyApplication(user, body));
    }

    @PostMapping("/my-application/cv-file")
    public ResponseEntity<CvFileMetadataDto> uploadMyApplicationCvFile(
            @AuthenticationPrincipal User user,
            @RequestParam("file") MultipartFile file
    ) {
        return ResponseEntity.ok(hrPortalService.uploadMyCvFile(user, file));
    }

    @GetMapping("/my-application/cv-file")
    public ResponseEntity<byte[]> downloadMyApplicationCvFile(
            @AuthenticationPrincipal User user
    ) {
        CvFileMetadataDto metadata = hrPortalService.getMyCvMetadata(user);
        if (!metadata.isFilePresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        byte[] content = hrPortalService.readMyCvFile(user);
        MediaType mediaType = metadata.getContentType() == null
                ? MediaType.APPLICATION_OCTET_STREAM
                : MediaType.parseMediaType(metadata.getContentType());
        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(content.length)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + metadata.getFileName() + "\"")
                .body(content);
    }

    @DeleteMapping("/my-application/cv-file")
    public ResponseEntity<CvFileMetadataDto> deleteMyApplicationCvFile(
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(hrPortalService.deleteMyCvFile(user));
    }

    @GetMapping("/my-application/cv-file/metadata")
    public ResponseEntity<CvFileMetadataDto> getMyApplicationCvFileMetadata(
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(hrPortalService.getMyCvMetadata(user));
    }

    @GetMapping("/leave-summary")
    public ResponseEntity<LeaveSummaryResponse> getLeaveSummary(
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(hrPortalService.getLeaveSummary(user));
    }

    @GetMapping("/my-leave-requests")
    public ResponseEntity<List<PortalLeaveRequestRow>> listMyLeaveRequests(
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(hrPortalService.listMyLeaveRequests(user));
    }

    @GetMapping("/leave-preview")
    public ResponseEntity<LeavePreviewResponse> previewLeave(
            @AuthenticationPrincipal User user,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ) {
        return ResponseEntity.ok(hrPortalService.previewLeave(user, startDate, endDate));
    }

    @PostMapping("/leave-requests")
    public ResponseEntity<SubmittedLeaveRequestResponse> submitLeaveRequest(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateLeaveRequestDto body
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(hrPortalService.submitLeaveRequest(user, body));
    }
}
