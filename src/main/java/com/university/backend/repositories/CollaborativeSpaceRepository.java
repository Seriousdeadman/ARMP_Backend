package com.university.backend.repositories;

import com.university.backend.entities.CollaborativeSpace;
import com.university.backend.enums.ResourceStatus;
import com.university.backend.enums.SpaceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CollaborativeSpaceRepository extends JpaRepository<CollaborativeSpace, Long> {
    List<CollaborativeSpace> findByStatus(ResourceStatus status);
    List<CollaborativeSpace> findByBuilding(String building);
    List<CollaborativeSpace> findBySpaceType(SpaceType spaceType);
    List<CollaborativeSpace> findByCapacityGreaterThanEqual(Integer capacity);
}