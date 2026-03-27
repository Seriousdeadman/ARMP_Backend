package com.university.backend.services;

import com.university.backend.dto.CollaborativeSpaceDTO;
import com.university.backend.entities.CollaborativeSpace;
import com.university.backend.enums.ResourceStatus;
import com.university.backend.enums.SpaceType;
import com.university.backend.repositories.CollaborativeSpaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CollaborativeSpaceService {

    private final CollaborativeSpaceRepository collaborativeSpaceRepository;

    public List<CollaborativeSpace> getAll() {
        return collaborativeSpaceRepository.findAll();
    }

    public CollaborativeSpace getById(Long id) {
        return collaborativeSpaceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Collaborative space not found with id: " + id));
    }

    public CollaborativeSpace create(CollaborativeSpaceDTO dto) {
        CollaborativeSpace space = CollaborativeSpace.builder()
                .name(dto.getName())
                .capacity(dto.getCapacity())
                .building(dto.getBuilding())
                .roomNumber(dto.getRoomNumber())
                .spaceType(dto.getSpaceType())
                .status(dto.getStatus())
                .build();
        return collaborativeSpaceRepository.save(space);
    }

    public CollaborativeSpace update(Long id, CollaborativeSpaceDTO dto) {
        CollaborativeSpace existing = getById(id);
        existing.setName(dto.getName());
        existing.setCapacity(dto.getCapacity());
        existing.setBuilding(dto.getBuilding());
        existing.setRoomNumber(dto.getRoomNumber());
        existing.setSpaceType(dto.getSpaceType());
        existing.setStatus(dto.getStatus());
        return collaborativeSpaceRepository.save(existing);
    }

    public void delete(Long id) {
        collaborativeSpaceRepository.deleteById(id);
    }

    public List<CollaborativeSpace> getByStatus(ResourceStatus status) {
        return collaborativeSpaceRepository.findByStatus(status);
    }

    public List<CollaborativeSpace> getByBuilding(String building) {
        return collaborativeSpaceRepository.findByBuilding(building);
    }

    public List<CollaborativeSpace> getByType(SpaceType type) {
        return collaborativeSpaceRepository.findBySpaceType(type);
    }

    public List<CollaborativeSpace> getByMinCapacity(Integer capacity) {
        return collaborativeSpaceRepository.findByCapacityGreaterThanEqual(capacity);
    }
}