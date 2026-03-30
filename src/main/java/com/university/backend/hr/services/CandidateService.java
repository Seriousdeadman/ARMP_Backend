package com.university.backend.hr.services;

import com.university.backend.hr.dto.CandidateRequest;
import com.university.backend.hr.dto.CandidateResponseDto;
import com.university.backend.hr.dto.CvRequest;
import com.university.backend.hr.dto.HrResponseMapper;
import com.university.backend.hr.entities.Candidate;
import com.university.backend.hr.entities.Cv;
import com.university.backend.hr.entities.Department;
import com.university.backend.hr.repositories.CandidateRepository;
import com.university.backend.hr.repositories.CvRepository;
import com.university.backend.hr.repositories.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CandidateService {

    private final CandidateRepository candidateRepository;
    private final DepartmentRepository departmentRepository;
    private final CvRepository cvRepository;
    private final CvFileStorageService cvFileStorageService;

    public List<CandidateResponseDto> findAll() {
        return candidateRepository.findAll().stream()
                .map(HrResponseMapper::toCandidateResponse)
                .collect(Collectors.toList());
    }

    public List<CandidateResponseDto> findByDepartmentId(String departmentId) {
        return candidateRepository.findByDepartmentId(departmentId).stream()
                .map(HrResponseMapper::toCandidateResponse)
                .collect(Collectors.toList());
    }

    /**
     * Candidates still in the hiring pipeline (not yet promoted to an employee). Use for interview scheduling
     * so hired people only appear as employees/interviewers, not as interview subjects.
     */
    public List<CandidateResponseDto> findAllNotYetPromoted(String departmentIdFilter) {
        List<Candidate> rows = candidateRepository.findAllNotYetPromotedToEmployee();
        if (departmentIdFilter != null && !departmentIdFilter.isBlank()) {
            String id = departmentIdFilter.trim();
            rows = rows.stream()
                    .filter(c -> c.getDepartment() != null && id.equals(c.getDepartment().getId()))
                    .collect(Collectors.toList());
        }
        return rows.stream()
                .map(HrResponseMapper::toCandidateResponse)
                .collect(Collectors.toList());
    }

    public CandidateResponseDto findById(String id) {
        return HrResponseMapper.toCandidateResponse(findEntityById(id));
    }

    public Candidate findEntityById(String id) {
        return candidateRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Candidate not found"));
    }

    @Transactional
    public CandidateResponseDto create(CandidateRequest request) {
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found"));
        Candidate candidate = Candidate.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .status(request.getStatus())
                .department(department)
                .build();
        if (StringUtils.hasText(request.getSkillsAndExperience())) {
            Cv cv = Cv.builder()
                    .skillsAndExperience(request.getSkillsAndExperience().trim())
                    .candidate(candidate)
                    .build();
            candidate.setCv(cv);
        }
        Candidate savedCandidate = candidateRepository.save(candidate);
        return HrResponseMapper.toCandidateResponse(findEntityById(savedCandidate.getId()));
    }

    @Transactional
    public CandidateResponseDto update(String id, CandidateRequest request) {
        Candidate candidate = findEntityById(id);
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found"));
        candidate.setName(request.getName());
        candidate.setEmail(request.getEmail());
        candidate.setPhone(request.getPhone());
        candidate.setStatus(request.getStatus());
        candidate.setDepartment(department);
        if (request.getSkillsAndExperience() != null) {
            if (StringUtils.hasText(request.getSkillsAndExperience())) {
                if (candidate.getCv() != null) {
                    candidate.getCv().setSkillsAndExperience(request.getSkillsAndExperience().trim());
                } else {
                    Cv cv = Cv.builder()
                            .skillsAndExperience(request.getSkillsAndExperience().trim())
                            .candidate(candidate)
                            .build();
                    candidate.setCv(cv);
                }
            } else if (candidate.getCv() != null) {
                cvRepository.delete(candidate.getCv());
                candidate.setCv(null);
            }
        }
        Candidate savedCandidate = candidateRepository.save(candidate);
        return HrResponseMapper.toCandidateResponse(findEntityById(savedCandidate.getId()));
    }

    @Transactional
    public void delete(String id) {
        if (!candidateRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Candidate not found");
        }
        candidateRepository.deleteById(id);
    }

    public Cv getCvForCandidate(String candidateId) {
        Candidate candidate = findEntityById(candidateId);
        if (candidate.getCv() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "CV not found for candidate");
        }
        return candidate.getCv();
    }

    public Cv findCvForCandidateOrNull(String candidateId) {
        return findEntityById(candidateId).getCv();
    }

    @Transactional
    public Cv upsertCvForCandidate(String candidateId, CvRequest request) {
        Candidate candidate = findEntityById(candidateId);
        Cv cv = candidate.getCv();
        if (cv != null) {
            cv.setSkillsAndExperience(request.getSkillsAndExperience().trim());
            return candidateRepository.save(candidate).getCv();
        }
        Cv created = Cv.builder()
                .skillsAndExperience(request.getSkillsAndExperience().trim())
                .candidate(candidate)
                .build();
        candidate.setCv(created);
        return candidateRepository.save(candidate).getCv();
    }

    @Transactional
    public Cv uploadCvFile(String candidateId, MultipartFile multipartFile) {
        Candidate candidate = findEntityById(candidateId);
        Cv cv = candidate.getCv();
        if (cv == null) {
            cv = Cv.builder()
                    .skillsAndExperience("No text CV provided yet.")
                    .candidate(candidate)
                    .build();
            candidate.setCv(cv);
        }

        String previousPath = cv.getFileStoragePath();
        CvFileStorageService.StoredCvFile stored = cvFileStorageService.store(candidateId, multipartFile);
        cv.setFileName(stored.originalFileName());
        cv.setFileContentType(stored.contentType());
        cv.setFileSizeBytes(stored.sizeBytes());
        cv.setFileStoragePath(stored.absolutePath());

        Cv saved = candidateRepository.save(candidate).getCv();
        if (previousPath != null && !previousPath.isBlank() && !previousPath.equals(stored.absolutePath())) {
            cvFileStorageService.deleteIfExists(previousPath);
        }
        return saved;
    }

    public byte[] readCvFile(String candidateId) {
        Candidate candidate = findEntityById(candidateId);
        Cv cv = candidate.getCv();
        if (cv == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "CV not found for candidate");
        }
        return cvFileStorageService.read(cv.getFileStoragePath());
    }

    @Transactional
    public Cv deleteCvFile(String candidateId) {
        Candidate candidate = findEntityById(candidateId);
        Cv cv = candidate.getCv();
        if (cv == null || cv.getFileStoragePath() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "CV file not found for candidate");
        }
        String oldPath = cv.getFileStoragePath();
        cv.setFileName(null);
        cv.setFileContentType(null);
        cv.setFileSizeBytes(null);
        cv.setFileStoragePath(null);
        Cv saved = candidateRepository.save(candidate).getCv();
        cvFileStorageService.deleteIfExists(oldPath);
        return saved;
    }
}
