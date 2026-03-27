package com.university.backend.ressource.controllers;

import com.university.backend.ressource.dto.EquipmentDTO;
import com.university.backend.ressource.entities.Equipment;
import com.university.backend.ressource.enums.EquipmentType;
import com.university.backend.ressource.enums.ResourceStatus;
import com.university.backend.ressource.services.EquipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/equipment")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EquipmentController {

    private final EquipmentService equipmentService;

    @GetMapping
    public ResponseEntity<List<Equipment>> getAll() {
        return ResponseEntity.ok(equipmentService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Equipment> getById(@PathVariable Long id) {
        return ResponseEntity.ok(equipmentService.getById(id));
    }

    @PostMapping
    public ResponseEntity<Equipment> create(@RequestBody EquipmentDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(equipmentService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Equipment> update(@PathVariable Long id, @RequestBody EquipmentDTO dto) {
        return ResponseEntity.ok(equipmentService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        equipmentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Equipment>> getByStatus(@PathVariable ResourceStatus status) {
        return ResponseEntity.ok(equipmentService.getByStatus(status));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<Equipment>> getByType(@PathVariable EquipmentType type) {
        return ResponseEntity.ok(equipmentService.getByType(type));
    }

    @GetMapping("/brand/{brand}")
    public ResponseEntity<List<Equipment>> getByBrand(@PathVariable String brand) {
        return ResponseEntity.ok(equipmentService.getByBrand(brand));
    }
}