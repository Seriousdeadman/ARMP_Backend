package com.university.backend.repositories;

import com.university.backend.entities.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionRepository extends JpaRepository<Session, String> {

    List<Session> findByUserId(String userId);

    List<Session> findByUserIdAndIsActiveTrue(String userId);
}