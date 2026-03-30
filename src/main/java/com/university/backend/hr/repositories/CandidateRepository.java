package com.university.backend.hr.repositories;

import com.university.backend.hr.entities.Candidate;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    /**
     * Portal step 1: only candidate ids (scalars) — avoids Hibernate 7 {@code StandardRowReader} failures on
     * some JOIN FETCH + enum/uuid row mappings against PostgreSQL.
     */
    @Query("SELECT c.id FROM Candidate c WHERE LOWER(c.email) = LOWER(:email) ORDER BY c.id ASC")
    List<String> findCandidateIdsByEmailIgnoreCase(@Param("email") String email, Pageable pageable);

    /**
     * Portal step 2: load candidate by PK. Do not use {@code JOIN FETCH department} here — Hibernate 7 can throw
     * from {@code StandardRowReader} on that shape with PostgreSQL; {@link Candidate#getDepartment()} is
     * {@link jakarta.persistence.FetchType#EAGER} so the department loads in a follow-up select/batch.
     */
    @Query("SELECT c FROM Candidate c WHERE c.id = :id")
    Optional<Candidate> findPortalById(@Param("id") String id);

    /**
     * Talent board: exclude candidates already promoted to an employee (still ACCEPTED in DB).
     */
    @EntityGraph(attributePaths = {"department", "cv"})
    @Query("SELECT c FROM Candidate c WHERE NOT EXISTS (SELECT 1 FROM Employee e WHERE e.sourceCandidate.id = c.id)")
    List<Candidate> findAllNotYetPromotedToEmployee();
}
