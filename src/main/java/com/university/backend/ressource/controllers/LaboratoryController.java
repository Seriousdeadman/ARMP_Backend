package com.university.backend.ressource.controllers;

import com.university.backend.ressource.dto.LaboratoryDTO;
import com.university.backend.ressource.entities.Laboratory;
import com.university.backend.ressource.enums.LabType;
import com.university.backend.ressource.enums.ResourceStatus;
import com.university.backend.ressource.services.LaboratoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/laboratories")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LaboratoryController {

    private final LaboratoryService laboratoryService;

    // Anyone authenticated can view
    @GetMapping
    public ResponseEntity<List<Laboratory>> getAll() {
        return ResponseEntity.ok(laboratoryService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Laboratory> getById(@PathVariable Long id) {
        return ResponseEntity.ok(laboratoryService.getById(id));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Laboratory>> getByStatus(@PathVariable ResourceStatus status) {
        return ResponseEntity.ok(laboratoryService.getByStatus(status));
    }

    @GetMapping("/building/{building}")
    public ResponseEntity<List<Laboratory>> getByBuilding(@PathVariable String building) {
        return ResponseEntity.ok(laboratoryService.getByBuilding(building));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<Laboratory>> getByType(@PathVariable LabType type) {
        return ResponseEntity.ok(laboratoryService.getByType(type));
    }

    @GetMapping("/capacity/{min}")
    public ResponseEntity<List<Laboratory>> getByMinCapacity(@PathVariable Integer min) {
        return ResponseEntity.ok(laboratoryService.getByMinCapacity(min));
    }

    // Only LOGISTICS_STAFF and SUPER_ADMIN can create, update, delete
    @PreAuthorize("hasAnyRole('LOGISTICS_STAFF', 'SUPER_ADMIN')")
    @PostMapping
    public ResponseEntity<Laboratory> create(@RequestBody LaboratoryDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(laboratoryService.create(dto));
    }

    @PreAuthorize("hasAnyRole('LOGISTICS_STAFF', 'SUPER_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Laboratory> update(@PathVariable Long id, @RequestBody LaboratoryDTO dto) {
        return ResponseEntity.ok(laboratoryService.update(id, dto));
    }

    @PreAuthorize("hasAnyRole('LOGISTICS_STAFF', 'SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        laboratoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}