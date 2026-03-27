package com.university.backend.repositories;

import com.university.backend.entities.Classroom;
import com.university.backend.enums.ClassroomType;
import com.university.backend.enums.ResourceStatus;
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