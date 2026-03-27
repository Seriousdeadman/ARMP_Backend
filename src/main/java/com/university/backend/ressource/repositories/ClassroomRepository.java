package com.university.backend.ressource.repositories;

import com.university.backend.ressource.entities.Classroom;
import com.university.backend.ressource.enums.ClassroomType;
import com.university.backend.ressource.enums.ResourceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ClassroomRepository extends JpaRepository<Classroom, Long> {
    List<Classroom> findByStatus(ResourceStatus status);
    List<Classroom> findByBuilding(String building);
    List<Classroom> findByClassroomType(ClassroomType classroomType);
    List<Classroom> findByCapacityGreaterThanEqual(Integer capacity);
}