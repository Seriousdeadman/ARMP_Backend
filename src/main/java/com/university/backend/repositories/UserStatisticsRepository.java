package com.university.backend.repositories;

import com.university.backend.entities.UserStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserStatisticsRepository extends JpaRepository<UserStatistics, String> {

    @Query("SELECT s FROM UserStatistics s WHERE s.user.id = :userId")
    Optional<UserStatistics> findByUserId(@Param("userId") String userId);
}