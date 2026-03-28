package com.university.backend.hr.controllers;

import com.university.backend.hr.dto.InterviewRequest;
import com.university.backend.hr.dto.InterviewResponseDto;
import com.university.backend.hr.services.InterviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hr/interviews")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_LOGISTICS_STAFF', 'ROLE_SUPER_ADMIN', 'LOGISTICS_STAFF', 'SUPER_ADMIN')")
public class InterviewController {

    private final InterviewService interviewService;

    @GetMapping
    public ResponseEntity<List<InterviewResponseDto>> list(@RequestParam(required = false) String candidateId) {
        if (candidateId != null) {
            return ResponseEntity.ok(interviewService.findByCandidateId(candidateId));
        }
        return ResponseEntity.ok(interviewService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InterviewResponseDto> get(@PathVariable String id) {
        return ResponseEntity.ok(interviewService.findById(id));
    }

    @PostMapping
    public ResponseEntity<InterviewResponseDto> create(@Valid @RequestBody InterviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(interviewService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InterviewResponseDto> update(
            @PathVariable String id,
            @Valid @RequestBody InterviewRequest request
    ) {
        return ResponseEntity.ok(interviewService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        interviewService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
