package com.university.backend.ressource.repositories;

import com.university.backend.ressource.entities.Equipment;
import com.university.backend.ressource.enums.EquipmentType;
import com.university.backend.ressource.enums.ResourceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    List<Equipment> findByStatus(ResourceStatus status);
    List<Equipment> findByEquipmentType(EquipmentType equipmentType);
    List<Equipment> findByBrand(String brand);
}