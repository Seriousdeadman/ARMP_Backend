package com.university.backend.hr.repositories;

import com.university.backend.hr.entities.Employee;
import com.university.backend.hr.enums.EmployeeStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, String> {

    @Override
    @EntityGraph(attributePaths = {"grade", "department"})
    List<Employee> findAll();

    @EntityGraph(attributePaths = {"grade", "department"})
    List<Employee> findByDepartmentId(String departmentId);

    @Override
    @EntityGraph(attributePaths = {"grade", "department"})
    Optional<Employee> findById(String id);

    Optional<Employee> findByEmail(String email);

    Optional<Employee> findByEmailIgnoreCase(String email);

    boolean existsBySourceCandidate_Id(String candidateId);

    Optional<Employee> findBySourceCandidateId(String candidateId);

    @EntityGraph(attributePaths = {"grade", "department", "sourceCandidate", "sourceCandidate.department", "promotedBy"})
    @Query("SELECT e FROM Employee e WHERE e.sourceCandidate IS NOT NULL")
    List<Employee> findAllPromotedFromCandidate();

    @EntityGraph(attributePaths = {"grade", "department"})
    List<Employee> findByStatus(EmployeeStatus status);
}
