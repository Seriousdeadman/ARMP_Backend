package com.university.backend.hr.services;

import com.university.backend.hr.dto.InterviewRequest;
import com.university.backend.hr.entities.Candidate;
import com.university.backend.hr.entities.Department;
import com.university.backend.hr.entities.Interview;
import com.university.backend.hr.enums.CandidateStatus;
import com.university.backend.hr.enums.InterviewStatus;
import com.university.backend.hr.repositories.CandidateRepository;
import com.university.backend.hr.repositories.InterviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InterviewServiceTest {

    @Mock
    private InterviewRepository interviewRepository;

    @Mock
    private CandidateRepository candidateRepository;

    @Mock
    private RecruitmentService recruitmentService;

    @InjectMocks
    private InterviewService interviewService;

    @Test
    void create_plannedInterview_syncsCandidateToInterviewing() {
        Department dept = Department.builder().id("d1").name("CS").build();
        Candidate candidate = Candidate.builder()
                .id("c1")
                .name("A")
                .email("a@x.com")
                .phone("1")
                .status(CandidateStatus.NEW)
                .department(dept)
                .build();
        when(candidateRepository.findById("c1")).thenReturn(Optional.of(candidate));
        when(interviewRepository.save(any(Interview.class))).thenAnswer(inv -> {
            Interview i = inv.getArgument(0);
            i.setId("i1");
            return i;
        });
        when(interviewRepository.findById("i1")).thenReturn(Optional.of(Interview.builder()
                .id("i1")
                .interviewDate(LocalDateTime.now())
                .location("Room A")
                .status(InterviewStatus.PLANNED)
                .candidate(candidate)
                .build()));

        InterviewRequest req = new InterviewRequest();
        req.setCandidateId("c1");
        req.setInterviewDate(LocalDateTime.now());
        req.setLocation("Room A");
        req.setStatus(InterviewStatus.PLANNED);
        req.setScore(null);

        interviewService.create(req);

        verify(recruitmentService).syncCandidateStatusAfterPlannedInterview("c1");
    }
}
