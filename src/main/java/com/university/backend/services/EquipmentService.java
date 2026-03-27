package com.university.backend.services;

import com.university.backend.dto.EquipmentDTO;
import com.university.backend.entities.Equipment;
import com.university.backend.enums.EquipmentType;
import com.university.backend.enums.ResourceStatus;
import com.university.backend.repositories.EquipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;

    public List<Equipment> getAll() {
        return equipmentRepository.findAll();
    }

    public Equipment getById(Long id) {
        return equipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipment not found with id: " + id));
    }

    public Equipment create(EquipmentDTO dto) {
        Equipment equipment = Equipment.builder()
                .name(dto.getName())
                .brand(dto.getBrand())
                .model(dto.getModel())
                .equipmentType(dto.getEquipmentType())
                .status(dto.getStatus())
                .build();
        return equipmentRepository.save(equipment);
    }

    public Equipment update(Long id, EquipmentDTO dto) {
        Equipment existing = getById(id);
        existing.setName(dto.getName());
        existing.setBrand(dto.getBrand());
        existing.setModel(dto.getModel());
        existing.setEquipmentType(dto.getEquipmentType());
        existing.setStatus(dto.getStatus());
        return equipmentRepository.save(existing);
    }

    public void delete(Long id) {
        equipmentRepository.deleteById(id);
    }

    public List<Equipment> getByStatus(ResourceStatus status) {
        return equipmentRepository.findByStatus(status);
    }

    public List<Equipment> getByType(EquipmentType type) {
        return equipmentRepository.findByEquipmentType(type);
    }

    public List<Equipment> getByBrand(String brand) {
        return equipmentRepository.findByBrand(brand);
    }
}