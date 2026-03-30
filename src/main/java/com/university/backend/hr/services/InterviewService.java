package com.university.backend.hr.services;

import com.university.backend.hr.dto.InterviewRequest;
import com.university.backend.hr.dto.InterviewResponseDto;
import com.university.backend.hr.dto.HrResponseMapper;
import com.university.backend.hr.entities.Candidate;
import com.university.backend.hr.entities.Employee;
import com.university.backend.hr.entities.Interview;
import com.university.backend.hr.enums.EmployeeStatus;
import com.university.backend.hr.enums.InterviewStatus;
import com.university.backend.hr.repositories.CandidateRepository;
import com.university.backend.hr.repositories.EmployeeRepository;
import com.university.backend.hr.repositories.InterviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InterviewService {

    private final InterviewRepository interviewRepository;
    private final CandidateRepository candidateRepository;
    private final EmployeeRepository employeeRepository;
    private final RecruitmentService recruitmentService;

    public List<InterviewResponseDto> findAll() {
        return interviewRepository.findAll().stream()
                .map(HrResponseMapper::toInterviewResponse)
                .collect(Collectors.toList());
    }

    public List<InterviewResponseDto> findByCandidateId(String candidateId) {
        return interviewRepository.findByCandidate_Id(candidateId).stream()
                .map(HrResponseMapper::toInterviewResponse)
                .collect(Collectors.toList());
    }

    public InterviewResponseDto findById(String id) {
        return HrResponseMapper.toInterviewResponse(findEntityById(id));
    }

    public Interview findEntityById(String id) {
        return interviewRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Interview not found"));
    }

    @Transactional
    public InterviewResponseDto create(InterviewRequest request) {
        Candidate candidate = candidateRepository.findById(request.getCandidateId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Candidate not found"));
        Employee interviewer = resolveActiveInterviewer(request.getInterviewerId());
        Integer score = resolvedScore(request.getStatus(), request.getScore());
        Interview interview = Interview.builder()
                .interviewDate(request.getInterviewDate())
                .location(request.getLocation())
                .score(score)
                .status(request.getStatus())
                .candidate(candidate)
                .interviewer(interviewer)
                .build();
        Interview savedInterview = interviewRepository.save(interview);
        if (request.getStatus() == InterviewStatus.PLANNED) {
            recruitmentService.syncCandidateStatusAfterPlannedInterview(candidate.getId());
        }
        return HrResponseMapper.toInterviewResponse(findEntityById(savedInterview.getId()));
    }

    @Transactional
    public InterviewResponseDto update(String id, InterviewRequest request) {
        Interview interview = findEntityById(id);
        Candidate candidate = candidateRepository.findById(request.getCandidateId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Candidate not found"));
        Employee interviewer = resolveActiveInterviewer(request.getInterviewerId());
        Integer score = resolvedScore(request.getStatus(), request.getScore());
        interview.setInterviewDate(request.getInterviewDate());
        interview.setLocation(request.getLocation());
        interview.setScore(score);
        interview.setStatus(request.getStatus());
        interview.setCandidate(candidate);
        interview.setInterviewer(interviewer);
        interviewRepository.save(interview);
        if (request.getStatus() == InterviewStatus.PLANNED) {
            recruitmentService.syncCandidateStatusAfterPlannedInterview(candidate.getId());
        }
        return HrResponseMapper.toInterviewResponse(findEntityById(id));
    }

    @Transactional
    public void delete(String id) {
        if (!interviewRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Interview not found");
        }
        interviewRepository.deleteById(id);
    }

    private Employee resolveActiveInterviewer(String interviewerId) {
        if (!StringUtils.hasText(interviewerId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Interviewer is required");
        }
        Employee employee = employeeRepository.findById(interviewerId.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Interviewer not found"));
        if (employee.getStatus() != EmployeeStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Interviewer must be an ACTIVE employee");
        }
        return employee;
    }

    /**
     * Score is only stored when the interview is completed; otherwise cleared.
     */
    private static Integer resolvedScore(InterviewStatus status, Integer requestedScore) {
        if (status != InterviewStatus.COMPLETED) {
            return null;
        }
        return requestedScore;
    }
}
