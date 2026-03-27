package com.university.backend.services;

import com.university.backend.dto.LaboratoryDTO;
import com.university.backend.entities.Laboratory;
import com.university.backend.enums.LabType;
import com.university.backend.enums.ResourceStatus;
import com.university.backend.repositories.LaboratoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LaboratoryService {

    private final LaboratoryRepository laboratoryRepository;

    public List<Laboratory> getAll() {
        return laboratoryRepository.findAll();
    }

    public Laboratory getById(Long id) {
        return laboratoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Laboratory not found with id: " + id));
    }

    public Laboratory create(LaboratoryDTO dto) {
        Laboratory laboratory = Laboratory.builder()
                .name(dto.getName())
                .capacity(dto.getCapacity())
                .building(dto.getBuilding())
                .roomNumber(dto.getRoomNumber())
                .labType(dto.getLabType())
                .status(dto.getStatus())
                .build();
        return laboratoryRepository.save(laboratory);
    }

    public Laboratory update(Long id, LaboratoryDTO dto) {
        Laboratory existing = getById(id);
        existing.setName(dto.getName());
        existing.setCapacity(dto.getCapacity());
        existing.setBuilding(dto.getBuilding());
        existing.setRoomNumber(dto.getRoomNumber());
        existing.setLabType(dto.getLabType());
        existing.setStatus(dto.getStatus());
        return laboratoryRepository.save(existing);
    }

    public void delete(Long id) {
        laboratoryRepository.deleteById(id);
    }

    public List<Laboratory> getByStatus(ResourceStatus status) {
        return laboratoryRepository.findByStatus(status);
    }

    public List<Laboratory> getByBuilding(String building) {
        return laboratoryRepository.findByBuilding(building);
    }

    public List<Laboratory> getByType(LabType type) {
        return laboratoryRepository.findByLabType(type);
    }

    public List<Laboratory> getByMinCapacity(Integer capacity) {
        return laboratoryRepository.findByCapacityGreaterThanEqual(capacity);
    }
}