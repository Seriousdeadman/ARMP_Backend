package com.university.backend.hr.repositories;

import com.university.backend.hr.entities.Candidate;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, String> {

    @Override
    @EntityGraph(attributePaths = {"department", "cv"})
    List<Candidate> findAll();

    @EntityGraph(attributePaths = {"department", "cv"})
    List<Candidate> findByDepartmentId(String departmentId);

    @Override
    @EntityGraph(attributePaths = {"department", "cv"})
    Optional<Candidate> findById(String id);

    Optional<Candidate> findByEmailIgnoreCase(String email);
}
