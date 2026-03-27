package com.university.backend.repositories;

import com.university.backend.entities.Laboratory;
import com.university.backend.enums.LabType;
import com.university.backend.enums.ResourceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LaboratoryRepository extends JpaRepository<Laboratory, Long> {
    List<Laboratory> findByStatus(ResourceStatus status);
    List<Laboratory> findByBuilding(String building);
    List<Laboratory> findByLabType(LabType labType);
    List<Laboratory> findByCapacityGreaterThanEqual(Integer capacity);
}