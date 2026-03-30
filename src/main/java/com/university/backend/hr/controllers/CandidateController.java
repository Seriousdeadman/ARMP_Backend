package com.university.backend.hr.controllers;

import com.university.backend.hr.dto.CandidateRequest;
import com.university.backend.hr.dto.CandidateResponseDto;
import com.university.backend.hr.dto.CandidateStatusPatchRequest;
import com.university.backend.hr.dto.PromoteCandidateRequest;
import com.university.backend.hr.dto.CvFileMetadataDto;
import com.university.backend.hr.dto.CvRequest;
import com.university.backend.hr.entities.Cv;
import com.university.backend.hr.entities.Employee;
import com.university.backend.entities.User;
import com.university.backend.hr.services.CandidateService;
import com.university.backend.hr.services.RecruitmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/hr/candidates")
@PreAuthorize("hasAnyRole('LOGISTICS_STAFF', 'SUPER_ADMIN')")
public class CandidateController {

    private final CandidateService candidateService;
    private final RecruitmentService recruitmentService;

    public CandidateController(
            CandidateService candidateService,
            RecruitmentService recruitmentService
    ) {
        this.candidateService = candidateService;
        this.recruitmentService = recruitmentService;
    }

    @GetMapping
    public ResponseEntity<List<CandidateResponseDto>> list(
            @RequestParam(required = false) String departmentId,
            @RequestParam(required = false, defaultValue = "false") boolean excludePromoted) {
        if (excludePromoted) {
            return ResponseEntity.ok(candidateService.findAllNotYetPromoted(departmentId));
        }
        if (departmentId != null) {
            return ResponseEntity.ok(candidateService.findByDepartmentId(departmentId));
        }
        return ResponseEntity.ok(candidateService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CandidateResponseDto> get(@PathVariable String id) {
        return ResponseEntity.ok(candidateService.findById(id));
    }

    @PostMapping
    public ResponseEntity<CandidateResponseDto> create(@Valid @RequestBody CandidateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(candidateService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CandidateResponseDto> update(
            @PathVariable String id,
            @Valid @RequestBody CandidateRequest request
    ) {
        return ResponseEntity.ok(candidateService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        candidateService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{candidateId}/cv")
    public ResponseEntity<Cv> getCv(@PathVariable String candidateId) {
        return ResponseEntity.ok(candidateService.getCvForCandidate(candidateId));
    }

    @PutMapping("/{candidateId}/cv")
    public ResponseEntity<Cv> upsertCv(
            @PathVariable String candidateId,
            @Valid @RequestBody CvRequest request
    ) {
        return ResponseEntity.ok(candidateService.upsertCvForCandidate(candidateId, request));
    }

    @PostMapping("/{id}/promote")
    public ResponseEntity<Employee> promoteToEmployee(
            @PathVariable String id,
            @RequestBody(required = false) PromoteCandidateRequest body,
            @AuthenticationPrincipal User user
    ) {
        String gradeId = body != null ? body.getGradeId() : null;
        String departmentId = body != null ? body.getDepartmentId() : null;
        return ResponseEntity.ok(recruitmentService.promoteToEmployee(id, gradeId, departmentId, user));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<CandidateResponseDto> patchStatus(
            @PathVariable String id,
            @Valid @RequestBody CandidateStatusPatchRequest request
    ) {
        recruitmentService.updateCandidateStatus(id, request.getStatus());
        return ResponseEntity.ok(candidateService.findById(id));
    }

    @PostMapping("/{candidateId}/cv-file")
    public ResponseEntity<CvFileMetadataDto> uploadCvFile(
            @PathVariable String candidateId,
            @RequestParam("file") MultipartFile file
    ) {
        Cv cv = candidateService.uploadCvFile(candidateId, file);
        return ResponseEntity.ok(toCvFileMetadataDto(candidateId, cv));
    }

    @GetMapping("/{candidateId}/cv-file")
    public ResponseEntity<byte[]> downloadCvFile(@PathVariable String candidateId) {
        Cv cv = candidateService.findCvForCandidateOrNull(candidateId);
        if (cv == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        if (cv.getFileStoragePath() == null || cv.getFileStoragePath().isBlank()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        byte[] content = candidateService.readCvFile(candidateId);
        MediaType mediaType = cv.getFileContentType() == null
                ? MediaType.APPLICATION_OCTET_STREAM
                : MediaType.parseMediaType(cv.getFileContentType());

        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(content.length)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + cv.getFileName() + "\"")
                .body(content);
    }

    @DeleteMapping("/{candidateId}/cv-file")
    public ResponseEntity<CvFileMetadataDto> deleteCvFile(@PathVariable String candidateId) {
        Cv cv = candidateService.deleteCvFile(candidateId);
        return ResponseEntity.ok(toCvFileMetadataDto(candidateId, cv));
    }

    @GetMapping("/{candidateId}/cv-file/metadata")
    public ResponseEntity<CvFileMetadataDto> getCvFileMetadata(@PathVariable String candidateId) {
        Cv cv = candidateService.findCvForCandidateOrNull(candidateId);
        if (cv == null) {
            return ResponseEntity.ok(CvFileMetadataDto.builder()
                    .candidateId(candidateId)
                    .fileName(null)
                    .contentType(null)
                    .sizeBytes(null)
                    .filePresent(false)
                    .build());
        }
        return ResponseEntity.ok(toCvFileMetadataDto(candidateId, cv));
    }

    private CvFileMetadataDto toCvFileMetadataDto(String candidateId, Cv cv) {
        return CvFileMetadataDto.builder()
                .candidateId(candidateId)
                .fileName(cv.getFileName())
                .contentType(cv.getFileContentType())
                .sizeBytes(cv.getFileSizeBytes())
                .filePresent(cv.getFileStoragePath() != null && !cv.getFileStoragePath().isBlank())
                .build();
    }
}
