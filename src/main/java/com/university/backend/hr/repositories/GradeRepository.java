package com.university.backend.hr.repositories;

import com.university.backend.hr.entities.Grade;
import com.university.backend.hr.enums.GradeName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GradeRepository extends JpaRepository<Grade, String> {

    Optional<Grade> findByName(GradeName name);
}
