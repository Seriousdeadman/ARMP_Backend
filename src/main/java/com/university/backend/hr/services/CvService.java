package com.university.backend.hr.services;

import com.university.backend.hr.dto.CvRequest;
import com.university.backend.hr.entities.Candidate;
import com.university.backend.hr.entities.Cv;
import com.university.backend.hr.repositories.CandidateRepository;
import com.university.backend.hr.repositories.CvRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CvService {

    private final CvRepository cvRepository;
    private final CandidateRepository candidateRepository;

    public List<Cv> findAll() {
        return cvRepository.findAll();
    }

    public Cv findById(String id) {
        return cvRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "CV not found"));
    }

    @Transactional
    public Cv update(String id, CvRequest request) {
        Cv cv = findById(id);
        cv.setSkillsAndExperience(request.getSkillsAndExperience().trim());
        return cvRepository.save(cv);
    }

    @Transactional
    public void delete(String id) {
        Cv cv = findById(id);
        Candidate candidate = cv.getCandidate();
        if (candidate != null) {
            candidate.setCv(null);
            candidateRepository.save(candidate);
        } else {
            cvRepository.delete(cv);
        }
    }
}
