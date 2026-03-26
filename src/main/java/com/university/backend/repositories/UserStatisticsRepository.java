package com.university.backend.repositories;

import com.university.backend.entities.UserStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserStatisticsRepository extends JpaRepository<UserStatistics, String> {

    Optional<UserStatistics> findByUserId(String userId);
}