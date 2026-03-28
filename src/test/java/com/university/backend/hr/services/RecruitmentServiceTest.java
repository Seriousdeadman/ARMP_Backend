package com.university.backend.hr.services;

import com.university.backend.hr.entities.Candidate;
import com.university.backend.hr.entities.Department;
import com.university.backend.hr.entities.Employee;
import com.university.backend.hr.entities.Grade;
import com.university.backend.hr.enums.CandidateStatus;
import com.university.backend.hr.enums.GradeName;
import com.university.backend.hr.repositories.CandidateRepository;
import com.university.backend.hr.repositories.EmployeeRepository;
import com.university.backend.hr.repositories.GradeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecruitmentServiceTest {

    @Mock
    private CandidateRepository candidateRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private GradeRepository gradeRepository;

    @InjectMocks
    private RecruitmentService recruitmentService;

    @Test
    void promoteToEmployee_createsEmployeeWithAssistantGrade() {
        Department dept = Department.builder().id("d1").name("CS").build();
        Candidate candidate = Candidate.builder()
                .id("c1")
                .name("Alex Doe")
                .email("a@x.com")
                .phone("1")
                .status(CandidateStatus.ACCEPTED)
                .department(dept)
                .build();
        Grade grade = Grade.builder()
                .id("g1")
                .name(GradeName.ASSISTANT)
                .baseSalary(new BigDecimal("3000.00"))
                .hourlyBonus(BigDecimal.ZERO)
                .build();
        when(candidateRepository.findById("c1")).thenReturn(Optional.of(candidate));
        when(employeeRepository.existsBySourceCandidate_Id("c1")).thenReturn(false);
        when(employeeRepository.findByEmail("a@x.com")).thenReturn(Optional.empty());
        when(gradeRepository.findByName(GradeName.ASSISTANT)).thenReturn(Optional.of(grade));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));

        Employee out = recruitmentService.promoteToEmployee("c1");

        assertThat(out.getGrade().getName()).isEqualTo(GradeName.ASSISTANT);
        assertThat(out.getHireDate()).isEqualTo(LocalDate.now());
        assertThat(out.getEmail()).isEqualTo("a@x.com");
        assertThat(out.getLeaveBalance()).isEqualTo(21);
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    void promoteToEmployee_rejectsWhenNotAccepted() {
        Candidate candidate = Candidate.builder()
                .id("c1")
                .name("A")
                .email("a@x.com")
                .phone("1")
                .status(CandidateStatus.PENDING)
                .department(Department.builder().id("d1").name("CS").build())
                .build();
        when(candidateRepository.findById("c1")).thenReturn(Optional.of(candidate));

        assertThatThrownBy(() -> recruitmentService.promoteToEmployee("c1"))
                .isInstanceOf(ResponseStatusException.class);
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void promoteToEmployee_setsSourceCandidate() {
        Department department = Department.builder().id("d1").name("IT").build();
        Candidate candidate = Candidate.builder()
                .id("c1")
                .name("Jane")
                .email("j@x.com")
                .phone("1")
                .status(CandidateStatus.ACCEPTED)
                .department(department)
                .build();
        Grade assistant = Grade.builder()
                .id("g1")
                .name(GradeName.ASSISTANT)
                .baseSalary(new BigDecimal("3000.00"))
                .hourlyBonus(BigDecimal.ZERO)
                .build();
        when(candidateRepository.findById("c1")).thenReturn(Optional.of(candidate));
        when(employeeRepository.existsBySourceCandidate_Id("c1")).thenReturn(false);
        when(employeeRepository.findByEmail("j@x.com")).thenReturn(Optional.empty());
        when(gradeRepository.findByName(GradeName.ASSISTANT)).thenReturn(Optional.of(assistant));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));

        recruitmentService.promoteToEmployee("c1");

        ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
        verify(employeeRepository).save(captor.capture());
        assertThat(captor.getValue().getSourceCandidate()).isEqualTo(candidate);
    }

    @Test
    void updateCandidateStatus_doesNotPromote() {
        Candidate candidate = Candidate.builder()
                .id("c1")
                .name("A")
                .email("a@x.com")
                .phone("1")
                .status(CandidateStatus.PENDING)
                .department(Department.builder().id("d1").name("CS").build())
                .build();
        when(candidateRepository.findById("c1")).thenReturn(Optional.of(candidate));
        when(candidateRepository.save(any(Candidate.class))).thenAnswer(inv -> inv.getArgument(0));

        Candidate updated = recruitmentService.updateCandidateStatus("c1", CandidateStatus.ACCEPTED);

        assertThat(updated.getStatus()).isEqualTo(CandidateStatus.ACCEPTED);
        verify(employeeRepository, never()).save(any());
    }
}
