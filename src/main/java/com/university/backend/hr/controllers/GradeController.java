package com.university.backend.hr.controllers;

import com.university.backend.hr.dto.GradeRequest;
import com.university.backend.hr.entities.Grade;
import com.university.backend.hr.services.GradeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hr/grades")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('LOGISTICS_STAFF', 'SUPER_ADMIN')")
public class GradeController {

    private final GradeService gradeService;

    @GetMapping
    public ResponseEntity<List<Grade>> list() {
        return ResponseEntity.ok(gradeService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Grade> get(@PathVariable String id) {
        return ResponseEntity.ok(gradeService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Grade> create(@Valid @RequestBody GradeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(gradeService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Grade> update(
            @PathVariable String id,
            @Valid @RequestBody GradeRequest request
    ) {
        return ResponseEntity.ok(gradeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        gradeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
