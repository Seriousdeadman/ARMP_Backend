package com.university.backend.controllers;

import com.university.backend.dto.LaboratoryDTO;
import com.university.backend.entities.Laboratory;
import com.university.backend.enums.LabType;
import com.university.backend.enums.ResourceStatus;
import com.university.backend.services.LaboratoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/laboratories")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LaboratoryController {

    private final LaboratoryService laboratoryService;

    @GetMapping
    public ResponseEntity<List<Laboratory>> getAll() {
        return ResponseEntity.ok(laboratoryService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Laboratory> getById(@PathVariable Long id) {
        return ResponseEntity.ok(laboratoryService.getById(id));
    }

    @PostMapping
    public ResponseEntity<Laboratory> create(@RequestBody LaboratoryDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(laboratoryService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Laboratory> update(@PathVariable Long id, @RequestBody LaboratoryDTO dto) {
        return ResponseEntity.ok(laboratoryService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        laboratoryService.delete(id);
        return ResponseEntity.noContent().build();
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
}