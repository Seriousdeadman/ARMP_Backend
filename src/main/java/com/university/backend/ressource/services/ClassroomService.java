package com.university.backend.ressource.services;

import com.university.backend.ressource.dto.ClassroomDTO;
import com.university.backend.ressource.entities.Classroom;
import com.university.backend.ressource.enums.ClassroomType;
import com.university.backend.ressource.enums.ResourceStatus;
import com.university.backend.ressource.repositories.ClassroomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClassroomService {

    private final ClassroomRepository classroomRepository;

    public List<Classroom> getAll() {
        return classroomRepository.findAll();
    }

    public Classroom getById(Long id) {
        return classroomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Classroom not found with id: " + id));
    }

    public Classroom create(ClassroomDTO dto) {
        Classroom classroom = Classroom.builder()
                .name(dto.getName())
                .capacity(dto.getCapacity())
                .building(dto.getBuilding())
                .roomNumber(dto.getRoomNumber())
                .classroomType(dto.getClassroomType())
                .status(dto.getStatus())
                .build();
        return classroomRepository.save(classroom);
    }

    public Classroom update(Long id, ClassroomDTO dto) {
        Classroom existing = getById(id);
        existing.setName(dto.getName());
        existing.setCapacity(dto.getCapacity());
        existing.setBuilding(dto.getBuilding());
        existing.setRoomNumber(dto.getRoomNumber());
        existing.setClassroomType(dto.getClassroomType());
        existing.setStatus(dto.getStatus());
        return classroomRepository.save(existing);
    }

    public void delete(Long id) {
        classroomRepository.deleteById(id);
    }

    public List<Classroom> getByStatus(ResourceStatus status) {
        return classroomRepository.findByStatus(status);
    }

    public List<Classroom> getByBuilding(String building) {
        return classroomRepository.findByBuilding(building);
    }

    public List<Classroom> getByType(ClassroomType type) {
        return classroomRepository.findByClassroomType(type);
    }

    public List<Classroom> getByMinCapacity(Integer capacity) {
        return classroomRepository.findByCapacityGreaterThanEqual(capacity);
    }
}