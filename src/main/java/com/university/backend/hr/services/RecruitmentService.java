package com.university.backend.hr.services;

import com.university.backend.hr.entities.Candidate;
import com.university.backend.hr.entities.Employee;
import com.university.backend.hr.enums.CandidateStatus;
import com.university.backend.hr.enums.GradeName;
import com.university.backend.hr.repositories.CandidateRepository;
import com.university.backend.hr.repositories.EmployeeRepository;
import com.university.backend.hr.repositories.GradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class RecruitmentService {

    private static final int DEFAULT_LEAVE_BALANCE = 21;

    private final CandidateRepository candidateRepository;
    private final EmployeeRepository employeeRepository;
    private final GradeRepository gradeRepository;

    /**
     * Creates an {@link Employee} from an ACCEPTED {@link Candidate}: copies name, email, same department,
     * {@link GradeName#ASSISTANT}, {@link Employee#getHireDate()} set to today, default leave balance.
     */
    @Transactional
    public Employee promoteToEmployee(String candidateId) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Candidate not found"));
        if (candidate.getStatus() != CandidateStatus.ACCEPTED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Candidate must be ACCEPTED before promotion; current status: " + candidate.getStatus());
        }
        if (employeeRepository.existsBySourceCandidate_Id(candidateId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Candidate already promoted to employee");
        }
        if (employeeRepository.findByEmail(candidate.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "An employee with this email already exists");
        }
        var assistantGrade = gradeRepository.findByName(GradeName.ASSISTANT)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "ASSISTANT grade is not configured in the system"));
        Employee employee = Employee.builder()
                .name(candidate.getName())
                .email(candidate.getEmail())
                .hireDate(LocalDate.now())
                .leaveBalance(DEFAULT_LEAVE_BALANCE)
                .grade(assistantGrade)
                .department(candidate.getDepartment())
                .sourceCandidate(candidate)
                .build();
        return employeeRepository.save(employee);
    }

    @Transactional
    public Candidate updateCandidateStatus(String candidateId, CandidateStatus newStatus) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Candidate not found"));
        candidate.setStatus(newStatus);
        return candidateRepository.save(candidate);
    }
}
