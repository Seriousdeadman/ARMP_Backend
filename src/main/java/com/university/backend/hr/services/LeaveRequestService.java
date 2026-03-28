package com.university.backend.hr.services;

import com.university.backend.hr.dto.LeaveRequestRequest;
import com.university.backend.hr.entities.Employee;
import com.university.backend.hr.entities.LeaveRequest;
import com.university.backend.hr.enums.LeaveRequestStatus;
import com.university.backend.hr.exception.InsufficientLeaveBalanceException;
import com.university.backend.hr.repositories.EmployeeRepository;
import com.university.backend.hr.repositories.LeaveRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;

    public List<LeaveRequest> findAll() {
        return leaveRequestRepository.findAll();
    }

    public List<LeaveRequest> findByEmployeeId(String employeeId) {
        return leaveRequestRepository.findByEmployee_Id(employeeId);
    }

    public LeaveRequest findById(String id) {
        return leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Leave request not found"));
    }

    @Transactional
    public LeaveRequest create(LeaveRequestRequest request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
        LeaveRequest leaveRequest = LeaveRequest.builder()
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .type(request.getType())
                .status(request.getStatus())
                .employee(employee)
                .build();
        return leaveRequestRepository.save(leaveRequest);
    }

    @Transactional
    public LeaveRequest update(String id, LeaveRequestRequest request) {
        LeaveRequest leaveRequest = findById(id);
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
        leaveRequest.setStartDate(request.getStartDate());
        leaveRequest.setEndDate(request.getEndDate());
        leaveRequest.setType(request.getType());
        leaveRequest.setStatus(request.getStatus());
        leaveRequest.setEmployee(employee);
        return leaveRequestRepository.save(leaveRequest);
    }

    @Transactional
    public void delete(String id) {
        if (!leaveRequestRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Leave request not found");
        }
        leaveRequestRepository.deleteById(id);
    }

    /**
     * Inclusive calendar days from {@code startDate} through {@code endDate} (same day = 1).
     */
    public static long countLeaveDays(LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date must be on or after start date");
        }
        return ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    /**
     * Computes duration; approves and deducts {@link Employee#getLeaveBalance()} if sufficient.
     * On insufficient balance, persists REJECTED and throws {@link InsufficientLeaveBalanceException}.
     */
    @Transactional(noRollbackFor = InsufficientLeaveBalanceException.class)
    public LeaveRequest processLeaveRequest(String leaveRequestId) {
        LeaveRequest request = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Leave request not found"));
        if (request.getStatus() != LeaveRequestStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Only PENDING leave requests can be processed; status=" + request.getStatus());
        }
        long days = countLeaveDays(request.getStartDate(), request.getEndDate());
        int needed = Math.toIntExact(days);
        Employee employee = request.getEmployee();
        if (employee.getLeaveBalance() < needed) {
            request.setStatus(LeaveRequestStatus.REJECTED);
            request.setRequestedDays(needed);
            String msg = "Insufficient leave balance: need " + needed + " day(s), available "
                    + employee.getLeaveBalance();
            request.setStatusMessage(msg);
            leaveRequestRepository.save(request);
            throw new InsufficientLeaveBalanceException(msg);
        }
        employee.setLeaveBalance(employee.getLeaveBalance() - needed);
        employeeRepository.save(employee);
        request.setStatus(LeaveRequestStatus.APPROVED);
        request.setRequestedDays(needed);
        request.setStatusMessage("Approved");
        return leaveRequestRepository.save(request);
    }

    @Transactional
    public LeaveRequest approveLeaveRequest(String leaveRequestId) {
        return processLeaveRequest(leaveRequestId);
    }

    @Transactional
    public LeaveRequest rejectLeaveRequest(String leaveRequestId) {
        LeaveRequest request = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Leave request not found"));
        if (request.getStatus() != LeaveRequestStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Only PENDING leave requests can be rejected; status=" + request.getStatus());
        }
        request.setStatus(LeaveRequestStatus.REJECTED);
        request.setStatusMessage("Rejected by HR");
        return leaveRequestRepository.save(request);
    }
}
