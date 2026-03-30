package com.university.backend.hr.services;

import com.university.backend.hr.entities.Candidate;
import com.university.backend.hr.entities.Department;
import com.university.backend.hr.entities.Employee;
import com.university.backend.hr.entities.Grade;
import com.university.backend.hr.enums.CandidateStatus;
import com.university.backend.hr.enums.EmployeeStatus;
import com.university.backend.hr.enums.GradeName;
import com.university.backend.enums.UserRole;
import com.university.backend.hr.repositories.CandidateRepository;
import com.university.backend.hr.repositories.DepartmentRepository;
import com.university.backend.hr.repositories.EmployeeRepository;
import com.university.backend.hr.repositories.GradeRepository;
import com.university.backend.entities.User;
import com.university.backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RecruitmentService {

    private static final int DEFAULT_LEAVE_BALANCE = 21;

    private final CandidateRepository candidateRepository;
    private final EmployeeRepository employeeRepository;
    private final GradeRepository gradeRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    /**
     * Creates an {@link Employee} from an ACCEPTED {@link Candidate}: copies name, email,
     * department from {@code departmentId} or the candidate's application department when omitted,
     * grade from {@code gradeId} or {@link GradeName#ASSISTANT} when omitted, hire date today, default leave balance.
     */
    @Transactional
    public Employee promoteToEmployee(String candidateId, String gradeId) {
        return promoteToEmployee(candidateId, gradeId, null, null);
    }

    @Transactional
    public Employee promoteToEmployee(String candidateId, String gradeId, User promotedBy) {
        return promoteToEmployee(candidateId, gradeId, null, promotedBy);
    }

    @Transactional
    public Employee promoteToEmployee(String candidateId, String gradeId, String departmentId, User promotedBy) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Candidate not found"));
        if (candidate.getStatus() != CandidateStatus.ACCEPTED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Candidate must be ACCEPTED before promotion; current status: " + candidate.getStatus());
        }
        Optional<Employee> alreadyPromoted = employeeRepository.findBySourceCandidateId(candidateId);
        if (alreadyPromoted.isPresent()) {
            return alreadyPromoted.get();
        }
        if (employeeRepository.findByEmailIgnoreCase(candidate.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "An employee with this email already exists (not linked to this candidate). "
                            + "Use a different candidate email or remove/rename the existing employee.");
        }
        Grade grade;
        if (gradeId != null && !gradeId.isBlank()) {
            grade = gradeRepository.findById(gradeId.trim())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Grade not found"));
        } else {
            grade = gradeRepository.findByName(GradeName.ASSISTANT)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                            "ASSISTANT grade is not configured in the system"));
        }
        Department department = candidate.getDepartment();
        if (departmentId != null && !departmentId.isBlank()) {
            department = departmentRepository.findById(departmentId.trim())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found"));
        }
        Instant now = Instant.now();
        Employee employee = Employee.builder()
                .name(candidate.getName())
                .email(candidate.getEmail())
                .hireDate(LocalDate.now())
                .leaveBalance(DEFAULT_LEAVE_BALANCE)
                .status(EmployeeStatus.PENDING_VALIDATION)
                .grade(grade)
                .department(department)
                .sourceCandidate(candidate)
                .promotedAt(now)
                .promotedBy(promotedBy)
                .build();
        Employee saved = employeeRepository.save(employee);
        userRepository.findByEmailIgnoreCase(candidate.getEmail())
                .ifPresent(user -> {
                    user.setRole(UserRole.TEACHER);
                    userRepository.save(user);
                });
        return saved;
    }

    @Transactional
    public Candidate updateCandidateStatus(String candidateId, CandidateStatus newStatus) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Candidate not found"));
        assertValidKanbanTransition(candidate.getStatus(), newStatus);
        candidate.setStatus(newStatus);
        return candidateRepository.save(candidate);
    }

    /**
     * Sets candidate to INTERVIEWING when HR schedules a PLANNED interview, unless already ACCEPTED
     * (hire decision — do not move backward in the pipeline).
     */
    @Transactional
    public void syncCandidateStatusAfterPlannedInterview(String candidateId) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Candidate not found"));
        if (candidate.getStatus() == CandidateStatus.ACCEPTED) {
            return;
        }
        candidate.setStatus(CandidateStatus.INTERVIEWING);
        candidateRepository.save(candidate);
    }

    private static void assertValidKanbanTransition(CandidateStatus from, CandidateStatus to) {
        if (from == to) {
            return;
        }
        if (from == CandidateStatus.ACCEPTED
                && (to == CandidateStatus.NEW || to == CandidateStatus.INTERVIEWING)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid transition from ACCEPTED to " + to);
        }
    }
}
