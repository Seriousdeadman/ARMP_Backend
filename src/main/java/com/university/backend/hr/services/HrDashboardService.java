package com.university.backend.hr.services;

import com.university.backend.hr.dto.CandidateRecruitmentDto;
import com.university.backend.hr.dto.EmployeeDirectoryDto;
import com.university.backend.hr.dto.LeaveRequestAdminDto;
import com.university.backend.hr.entities.Candidate;
import com.university.backend.hr.entities.Employee;
import com.university.backend.hr.entities.Interview;
import com.university.backend.hr.entities.LeaveRequest;
import com.university.backend.hr.enums.LeaveRequestStatus;
import com.university.backend.hr.repositories.CandidateRepository;
import com.university.backend.hr.repositories.EmployeeRepository;
import com.university.backend.hr.repositories.InterviewRepository;
import com.university.backend.hr.repositories.LeaveRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HrDashboardService {

    private final CandidateRepository candidateRepository;
    private final InterviewRepository interviewRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public List<CandidateRecruitmentDto> listCandidatesForRecruitment() {
        return candidateRepository.findAll().stream()
                .map(this::toCandidateRecruitmentDto)
                .toList();
    }

    private CandidateRecruitmentDto toCandidateRecruitmentDto(Candidate c) {
        List<Interview> interviews = interviewRepository.findByCandidate_Id(c.getId());
        Integer score = interviews.stream()
                .filter(i -> i.getScore() != null)
                .max(Comparator.comparing(Interview::getInterviewDate))
                .map(Interview::getScore)
                .orElse(null);
        if (score == null) {
            score = interviews.stream()
                    .map(Interview::getScore)
                    .filter(s -> s != null)
                    .max(Integer::compareTo)
                    .orElse(null);
        }
        String dept = c.getDepartment() != null ? c.getDepartment().getName() : null;
        return new CandidateRecruitmentDto(
                c.getId(),
                c.getName(),
                c.getEmail(),
                c.getPhone(),
                c.getStatus(),
                dept,
                score
        );
    }

    @Transactional(readOnly = true)
    public List<LeaveRequestAdminDto> listPendingLeaveRequests(String currentUserEmail) {
        return leaveRequestRepository.findByStatusOrderByStartDateAsc(LeaveRequestStatus.PENDING).stream()
                .filter(r -> r.getEmployee() != null
                        && r.getEmployee().getEmail() != null
                        && !r.getEmployee().getEmail().equalsIgnoreCase(currentUserEmail))
                .map(this::toLeaveAdminDto)
                .toList();
    }

    private LeaveRequestAdminDto toLeaveAdminDto(LeaveRequest r) {
        Employee e = r.getEmployee();
        long days = LeaveRequestService.countLeaveDays(r.getStartDate(), r.getEndDate());
        return new LeaveRequestAdminDto(
                r.getId(),
                e.getId(),
                e.getName(),
                e.getEmail(),
                r.getStartDate(),
                r.getEndDate(),
                r.getType(),
                r.getStatus(),
                days
        );
    }

    @Transactional(readOnly = true)
    public List<EmployeeDirectoryDto> listEmployeesDirectory() {
        return employeeRepository.findAll().stream()
                .map(emp -> new EmployeeDirectoryDto(
                        emp.getId(),
                        emp.getName(),
                        emp.getEmail(),
                        emp.getGrade().getName(),
                        emp.getLeaveBalance(),
                        emp.getDepartment().getName()
                ))
                .toList();
    }
}
