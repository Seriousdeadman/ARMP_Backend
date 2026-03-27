package com.university.backend.ressource.controllers;

import com.university.backend.ressource.dto.ClassroomDTO;
import com.university.backend.ressource.entities.Classroom;
import com.university.backend.ressource.enums.ClassroomType;
import com.university.backend.ressource.enums.ResourceStatus;
import com.university.backend.ressource.services.ClassroomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/classrooms")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ClassroomController {

    private final ClassroomService classroomService;

    // Anyone authenticated can view
    @GetMapping
    public ResponseEntity<List<Classroom>> getAll() {
        return ResponseEntity.ok(classroomService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Classroom> getById(@PathVariable Long id) {
        return ResponseEntity.ok(classroomService.getById(id));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Classroom>> getByStatus(@PathVariable ResourceStatus status) {
        return ResponseEntity.ok(classroomService.getByStatus(status));
    }

    @GetMapping("/building/{building}")
    public ResponseEntity<List<Classroom>> getByBuilding(@PathVariable String building) {
        return ResponseEntity.ok(classroomService.getByBuilding(building));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<Classroom>> getByType(@PathVariable ClassroomType type) {
        return ResponseEntity.ok(classroomService.getByType(type));
    }

    @GetMapping("/capacity/{min}")
    public ResponseEntity<List<Classroom>> getByMinCapacity(@PathVariable Integer min) {
        return ResponseEntity.ok(classroomService.getByMinCapacity(min));
    }

    // Only LOGISTICS_STAFF and SUPER_ADMIN can create, update, delete
    @PreAuthorize("hasAnyRole('LOGISTICS_STAFF', 'SUPER_ADMIN')")
    @PostMapping
    public ResponseEntity<Classroom> create(@RequestBody ClassroomDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(classroomService.create(dto));
    }

    @PreAuthorize("hasAnyRole('LOGISTICS_STAFF', 'SUPER_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Classroom> update(@PathVariable Long id, @RequestBody ClassroomDTO dto) {
        return ResponseEntity.ok(classroomService.update(id, dto));
    }

    @PreAuthorize("hasAnyRole('LOGISTICS_STAFF', 'SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        classroomService.delete(id);
        return ResponseEntity.noContent().build();
    }
}