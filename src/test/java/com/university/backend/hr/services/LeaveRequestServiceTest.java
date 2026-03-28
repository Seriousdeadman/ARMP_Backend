package com.university.backend.hr.services;

import com.university.backend.hr.entities.Employee;
import com.university.backend.hr.entities.LeaveRequest;
import com.university.backend.hr.enums.LeaveRequestStatus;
import com.university.backend.hr.enums.LeaveType;
import com.university.backend.hr.exception.InsufficientLeaveBalanceException;
import com.university.backend.hr.repositories.EmployeeRepository;
import com.university.backend.hr.repositories.LeaveRequestRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeaveRequestServiceTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private LeaveRequestService leaveRequestService;

    @Test
    void countLeaveDays_inclusive() {
        assertThat(LeaveRequestService.countLeaveDays(
                LocalDate.of(2025, 3, 1),
                LocalDate.of(2025, 3, 3)
        )).isEqualTo(3);
        assertThat(LeaveRequestService.countLeaveDays(
                LocalDate.of(2025, 3, 1),
                LocalDate.of(2025, 3, 1)
        )).isEqualTo(1);
    }

    @Test
    void approveLeaveRequest_approvesAndDeducts() {
        Employee employee = Employee.builder()
                .id("e1")
                .leaveBalance(10)
                .build();
        LeaveRequest request = LeaveRequest.builder()
                .id("lr1")
                .employee(employee)
                .startDate(LocalDate.of(2025, 6, 1))
                .endDate(LocalDate.of(2025, 6, 3))
                .type(LeaveType.ANNUAL)
                .status(LeaveRequestStatus.PENDING)
                .build();
        when(leaveRequestRepository.findById("lr1")).thenReturn(Optional.of(request));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(inv -> inv.getArgument(0));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));

        LeaveRequest result = leaveRequestService.approveLeaveRequest("lr1");

        assertThat(result.getStatus()).isEqualTo(LeaveRequestStatus.APPROVED);
        assertThat(result.getRequestedDays()).isEqualTo(3);
        ArgumentCaptor<Employee> empCap = ArgumentCaptor.forClass(Employee.class);
        verify(employeeRepository).save(empCap.capture());
        assertThat(empCap.getValue().getLeaveBalance()).isEqualTo(7);
    }

    @Test
    void approveLeaveRequest_insufficientBalance_rejectsAndThrows() {
        Employee employee = Employee.builder()
                .id("e1")
                .leaveBalance(1)
                .build();
        LeaveRequest request = LeaveRequest.builder()
                .id("lr1")
                .employee(employee)
                .startDate(LocalDate.of(2025, 6, 1))
                .endDate(LocalDate.of(2025, 6, 5))
                .type(LeaveType.ANNUAL)
                .status(LeaveRequestStatus.PENDING)
                .build();
        when(leaveRequestRepository.findById("lr1")).thenReturn(Optional.of(request));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThatThrownBy(() -> leaveRequestService.approveLeaveRequest("lr1"))
                .isInstanceOf(InsufficientLeaveBalanceException.class)
                .hasMessageContaining("Insufficient leave balance");

        ArgumentCaptor<LeaveRequest> lrCap = ArgumentCaptor.forClass(LeaveRequest.class);
        verify(leaveRequestRepository, atLeastOnce()).save(lrCap.capture());
        assertThat(lrCap.getAllValues().get(lrCap.getAllValues().size() - 1).getStatus())
                .isEqualTo(LeaveRequestStatus.REJECTED);
        verify(employeeRepository, never()).save(any());
    }
}
