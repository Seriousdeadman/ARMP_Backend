package com.university.backend.repositories;

import com.university.backend.entities.Equipment;
import com.university.backend.enums.EquipmentType;
import com.university.backend.enums.ResourceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    List<Equipment> findByStatus(ResourceStatus status);
    List<Equipment> findByEquipmentType(EquipmentType equipmentType);
    List<Equipment> findByBrand(String brand);
}