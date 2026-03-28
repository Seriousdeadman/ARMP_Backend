package com.university.backend.hr.repositories;

import com.university.backend.hr.entities.Cv;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CvRepository extends JpaRepository<Cv, String> {

    Optional<Cv> findByCandidateId(String candidateId);
}
