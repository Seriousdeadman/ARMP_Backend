package com.university.backend.hr.controllers;

import com.university.backend.hr.dto.CvRequest;
import com.university.backend.hr.entities.Cv;
import com.university.backend.hr.services.CvService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hr/cvs")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('LOGISTICS_STAFF', 'SUPER_ADMIN')")
public class CvController {

    private final CvService cvService;

    @GetMapping
    public ResponseEntity<List<Cv>> list() {
        return ResponseEntity.ok(cvService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cv> get(@PathVariable String id) {
        return ResponseEntity.ok(cvService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cv> update(
            @PathVariable String id,
            @Valid @RequestBody CvRequest request
    ) {
        return ResponseEntity.ok(cvService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        cvService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
