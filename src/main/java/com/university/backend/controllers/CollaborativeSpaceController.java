package com.university.backend.controllers;

import com.university.backend.dto.CollaborativeSpaceDTO;
import com.university.backend.entities.CollaborativeSpace;
import com.university.backend.enums.ResourceStatus;
import com.university.backend.enums.SpaceType;
import com.university.backend.services.CollaborativeSpaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/collaborative-spaces")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CollaborativeSpaceController {

    private final CollaborativeSpaceService collaborativeSpaceService;

    @GetMapping
    public ResponseEntity<List<CollaborativeSpace>> getAll() {
        return ResponseEntity.ok(collaborativeSpaceService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CollaborativeSpace> getById(@PathVariable Long id) {
        return ResponseEntity.ok(collaborativeSpaceService.getById(id));
    }

    @PostMapping
    public ResponseEntity<CollaborativeSpace> create(@RequestBody CollaborativeSpaceDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(collaborativeSpaceService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CollaborativeSpace> update(@PathVariable Long id, @RequestBody CollaborativeSpaceDTO dto) {
        return ResponseEntity.ok(collaborativeSpaceService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        collaborativeSpaceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<CollaborativeSpace>> getByStatus(@PathVariable ResourceStatus status) {
        return ResponseEntity.ok(collaborativeSpaceService.getByStatus(status));
    }

    @GetMapping("/building/{building}")
    public ResponseEntity<List<CollaborativeSpace>> getByBuilding(@PathVariable String building) {
        return ResponseEntity.ok(collaborativeSpaceService.getByBuilding(building));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<CollaborativeSpace>> getByType(@PathVariable SpaceType type) {
        return ResponseEntity.ok(collaborativeSpaceService.getByType(type));
    }

    @GetMapping("/capacity/{min}")
    public ResponseEntity<List<CollaborativeSpace>> getByMinCapacity(@PathVariable Integer min) {
        return ResponseEntity.ok(collaborativeSpaceService.getByMinCapacity(min));
    }
}