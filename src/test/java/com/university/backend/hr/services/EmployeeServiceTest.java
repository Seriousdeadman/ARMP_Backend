package com.university.backend.hr.services;

import com.university.backend.entities.User;
import com.university.backend.enums.UserRole;
import com.university.backend.hr.dto.EmployeeRequest;
import com.university.backend.hr.dto.PayrollResult;
import com.university.backend.hr.entities.Department;
import com.university.backend.hr.entities.Employee;
import com.university.backend.hr.entities.Grade;
import com.university.backend.hr.enums.EmployeeStatus;
import com.university.backend.hr.repositories.DepartmentRepository;
import com.university.backend.hr.repositories.EmployeeRepository;
import com.university.backend.hr.repositories.GradeRepository;
import com.university.backend.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private GradeRepository gradeRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private PayrollService payrollService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EmployeeService employeeService;

    @Test
    void create_superAdminWithGrant_setsMatchingUserToLogisticsStaff() {
        Grade grade = Grade.builder().id("g1").build();
        Department department = Department.builder().id("d1").build();
        when(gradeRepository.findById("g1")).thenReturn(Optional.of(grade));
        when(departmentRepository.findById("d1")).thenReturn(Optional.of(department));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> {
            Employee e = invocation.getArgument(0);
            e.setId("e-new");
            return e;
        });
        Employee persisted = Employee.builder()
                .id("e-new")
                .name("Pat")
                .email("pat@test.com")
                .hireDate(LocalDate.of(2026, 1, 1))
                .leaveBalance(21)
                .grade(grade)
                .department(department)
                .status(EmployeeStatus.ACTIVE)
                .build();
        when(employeeRepository.findById("e-new")).thenReturn(Optional.of(persisted));
        User portalUser = User.builder()
                .id("u1")
                .email("pat@test.com")
                .role(UserRole.STUDENT)
                .build();
        when(userRepository.findByEmailIgnoreCase("pat@test.com")).thenReturn(Optional.of(portalUser));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(payrollService.calculate(any(Employee.class))).thenReturn(PayrollResult.builder()
                .baseSalary(BigDecimal.ONE)
                .dailyRate(BigDecimal.ONE)
                .leaveBalance(21)
                .deduction(BigDecimal.ZERO)
                .calculatedSalary(BigDecimal.ONE)
                .build());

        EmployeeRequest request = EmployeeRequest.builder()
                .name("Pat")
                .email("pat@test.com")
                .hireDate(LocalDate.of(2026, 1, 1))
                .gradeId("g1")
                .departmentId("d1")
                .grantLogisticsStaffRole(true)
                .build();

        employeeService.create(request, UserRole.SUPER_ADMIN);

        ArgumentCaptor<User> userCap = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCap.capture());
        assertThat(userCap.getValue().getRole()).isEqualTo(UserRole.LOGISTICS_STAFF);
    }

    @Test
    void create_logisticsStaff_doesNotGrantHrRoleEvenIfFlagTrue() {
        Grade grade = Grade.builder().id("g1").build();
        Department department = Department.builder().id("d1").build();
        when(gradeRepository.findById("g1")).thenReturn(Optional.of(grade));
        when(departmentRepository.findById("d1")).thenReturn(Optional.of(department));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> {
            Employee e = invocation.getArgument(0);
            e.setId("e2");
            return e;
        });
        Employee persisted = Employee.builder()
                .id("e2")
                .name("Q")
                .email("q@test.com")
                .hireDate(LocalDate.of(2026, 1, 2))
                .leaveBalance(21)
                .grade(grade)
                .department(department)
                .status(EmployeeStatus.PENDING_VALIDATION)
                .build();
        when(employeeRepository.findById("e2")).thenReturn(Optional.of(persisted));
        when(payrollService.calculate(any(Employee.class))).thenReturn(PayrollResult.builder()
                .baseSalary(BigDecimal.ONE)
                .dailyRate(BigDecimal.ONE)
                .leaveBalance(21)
                .deduction(BigDecimal.ZERO)
                .calculatedSalary(BigDecimal.ONE)
                .build());

        EmployeeRequest request = EmployeeRequest.builder()
                .name("Q")
                .email("q@test.com")
                .hireDate(LocalDate.of(2026, 1, 2))
                .gradeId("g1")
                .departmentId("d1")
                .grantLogisticsStaffRole(true)
                .build();

        employeeService.create(request, UserRole.LOGISTICS_STAFF);

        verify(userRepository, never()).findByEmailIgnoreCase(any());
        verify(userRepository, never()).save(any());
    }
}
