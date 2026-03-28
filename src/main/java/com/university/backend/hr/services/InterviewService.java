package com.university.backend.hr.services;

import com.university.backend.hr.dto.InterviewRequest;
import com.university.backend.hr.dto.InterviewResponseDto;
import com.university.backend.hr.dto.HrResponseMapper;
import com.university.backend.hr.entities.Candidate;
import com.university.backend.hr.entities.Interview;
import com.university.backend.hr.repositories.CandidateRepository;
import com.university.backend.hr.repositories.InterviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InterviewService {

    private final InterviewRepository interviewRepository;
    private final CandidateRepository candidateRepository;

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
        Interview interview = Interview.builder()
                .interviewDate(request.getInterviewDate())
                .location(request.getLocation())
                .score(request.getScore())
                .status(request.getStatus())
                .candidate(candidate)
                .build();
        Interview savedInterview = interviewRepository.save(interview);
        return HrResponseMapper.toInterviewResponse(findEntityById(savedInterview.getId()));
    }

    @Transactional
    public InterviewResponseDto update(String id, InterviewRequest request) {
        Interview interview = findEntityById(id);
        Candidate candidate = candidateRepository.findById(request.getCandidateId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Candidate not found"));
        interview.setInterviewDate(request.getInterviewDate());
        interview.setLocation(request.getLocation());
        interview.setScore(request.getScore());
        interview.setStatus(request.getStatus());
        interview.setCandidate(candidate);
        Interview savedInterview = interviewRepository.save(interview);
        return HrResponseMapper.toInterviewResponse(findEntityById(savedInterview.getId()));
    }

    @Transactional
    public void delete(String id) {
        if (!interviewRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Interview not found");
        }
        interviewRepository.deleteById(id);
    }
}
