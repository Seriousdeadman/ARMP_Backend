package com.university.backend.hr.repositories;

import com.university.backend.hr.entities.Interview;
import com.university.backend.hr.enums.InterviewStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, String> {

    @Override
    @EntityGraph(attributePaths = {"candidate", "candidate.department"})
    List<Interview> findAll();

    @EntityGraph(attributePaths = {"candidate", "candidate.department"})
    List<Interview> findByCandidate_Id(String candidateId);

    @Override
    @EntityGraph(attributePaths = {"candidate", "candidate.department"})
    Optional<Interview> findById(String id);

    List<Interview> findByCandidate_IdAndStatusOrderByInterviewDateAsc(
            String candidateId,
            InterviewStatus status
    );
}
