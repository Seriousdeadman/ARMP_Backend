package com.university.backend.hr.services;

import com.university.backend.hr.dto.InterviewRequest;
import com.university.backend.hr.entities.Candidate;
import com.university.backend.hr.entities.Department;
import com.university.backend.hr.entities.Employee;
import com.university.backend.hr.entities.Grade;
import com.university.backend.hr.entities.Interview;
import com.university.backend.hr.enums.CandidateStatus;
import com.university.backend.hr.enums.EmployeeStatus;
import com.university.backend.hr.enums.GradeName;
import com.university.backend.hr.enums.InterviewStatus;
import com.university.backend.hr.repositories.CandidateRepository;
import com.university.backend.hr.repositories.EmployeeRepository;
import com.university.backend.hr.repositories.InterviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    private EmployeeRepository employeeRepository;

    @Mock
    private RecruitmentService recruitmentService;

    @InjectMocks
    private InterviewService interviewService;

    @Test
    void create_plannedInterview_syncsCandidateToInterviewing_andSetsInterviewer() {
        Department dept = Department.builder().id("d1").name("CS").build();
        Grade grade = Grade.builder()
                .id("g1")
                .name(GradeName.ASSISTANT)
                .baseSalary(BigDecimal.ZERO)
                .hourlyBonus(BigDecimal.ZERO)
                .build();
        Candidate candidate = Candidate.builder()
                .id("c1")
                .name("A")
                .email("a@x.com")
                .phone("1")
                .status(CandidateStatus.NEW)
                .department(dept)
                .build();
        Employee interviewer = Employee.builder()
                .id("e1")
                .name("HR")
                .email("hr@test.com")
                .hireDate(LocalDate.now())
                .leaveBalance(21)
                .grade(grade)
                .department(dept)
                .status(EmployeeStatus.ACTIVE)
                .build();

        when(candidateRepository.findById("c1")).thenReturn(Optional.of(candidate));
        when(employeeRepository.findById("e1")).thenReturn(Optional.of(interviewer));
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
                .interviewer(interviewer)
                .build()));

        InterviewRequest req = new InterviewRequest();
        req.setCandidateId("c1");
        req.setInterviewerId("e1");
        req.setInterviewDate(LocalDateTime.now());
        req.setLocation("Room A");
        req.setStatus(InterviewStatus.PLANNED);
        req.setScore(10);

        interviewService.create(req);

        ArgumentCaptor<Interview> captor = ArgumentCaptor.forClass(Interview.class);
        verify(interviewRepository).save(captor.capture());
        assertThat(captor.getValue().getScore()).isNull();
        assertThat(captor.getValue().getInterviewer()).isEqualTo(interviewer);
        verify(recruitmentService).syncCandidateStatusAfterPlannedInterview("c1");
    }

    @Test
    void create_withoutInterviewerId_throwsBadRequest() {
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

        InterviewRequest req = new InterviewRequest();
        req.setCandidateId("c1");
        req.setInterviewerId(null);
        req.setInterviewDate(LocalDateTime.now());
        req.setLocation("L");
        req.setStatus(InterviewStatus.PLANNED);

        assertThatThrownBy(() -> interviewService.create(req))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
    }
}
