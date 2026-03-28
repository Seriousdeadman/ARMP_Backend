package com.university.backend.hr.controllers;

import com.university.backend.hr.dto.DepartmentRequest;
import com.university.backend.hr.entities.Department;
import com.university.backend.hr.services.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hr/departments")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('LOGISTICS_STAFF', 'SUPER_ADMIN')")
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping
    public ResponseEntity<List<Department>> list() {
        return ResponseEntity.ok(departmentService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Department> get(@PathVariable String id) {
        return ResponseEntity.ok(departmentService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Department> create(@Valid @RequestBody DepartmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(departmentService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Department> update(
            @PathVariable String id,
            @Valid @RequestBody DepartmentRequest request
    ) {
        return ResponseEntity.ok(departmentService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        departmentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
