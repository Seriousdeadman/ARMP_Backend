package com.university.backend.hr.services;

import com.university.backend.hr.dto.EmployeeRequest;
import com.university.backend.hr.dto.EmployeeResponseDto;
import com.university.backend.hr.dto.HrResponseMapper;
import com.university.backend.hr.dto.PayrollResult;
import com.university.backend.hr.entities.Department;
import com.university.backend.hr.entities.Employee;
import com.university.backend.hr.entities.Grade;
import com.university.backend.hr.enums.EmployeeStatus;
import com.university.backend.enums.UserRole;
import com.university.backend.hr.repositories.DepartmentRepository;
import com.university.backend.hr.repositories.EmployeeRepository;
import com.university.backend.hr.repositories.GradeRepository;
import com.university.backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final GradeRepository gradeRepository;
    private final DepartmentRepository departmentRepository;
    private final PayrollService payrollService;
    private final UserRepository userRepository;

    public List<EmployeeResponseDto> findAll() {
        return employeeRepository.findAll().stream()
                .map(e -> toResponseWithPayroll(e))
                .collect(Collectors.toList());
    }

    public List<EmployeeResponseDto> findByDepartmentId(String departmentId) {
        return employeeRepository.findByDepartmentId(departmentId).stream()
                .map(e -> toResponseWithPayroll(e))
                .collect(Collectors.toList());
    }

    public EmployeeResponseDto findById(String id) {
        return toResponseWithPayroll(findEntityById(id));
    }

    public Employee findEntityById(String id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
    }

    @Transactional
    public EmployeeResponseDto create(EmployeeRequest request, UserRole creatorRole) {
        Grade grade = gradeRepository.findById(request.getGradeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Grade not found"));
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found"));

        EmployeeStatus status;
        if (creatorRole == UserRole.LOGISTICS_STAFF) {
            status = EmployeeStatus.PENDING_VALIDATION;
        } else if (creatorRole == UserRole.SUPER_ADMIN) {
            status = request.getStatus() != null ? request.getStatus() : EmployeeStatus.ACTIVE;
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot create employees with this role");
        }

        Employee.EmployeeBuilder builder = Employee.builder()
                .name(request.getName())
                .email(request.getEmail())
                .hireDate(request.getHireDate())
                .grade(grade)
                .department(department)
                .status(status);

        if (request.getLeaveBalance() != null) {
            builder.leaveBalance(request.getLeaveBalance());
        } else {
            builder.leaveBalance(21);
        }
        Employee savedEmployee = employeeRepository.save(builder.build());
        applyLogisticsStaffGrantIfRequested(request.getEmail(), request.getGrantLogisticsStaffRole(), creatorRole);
        return toResponseWithPayroll(findEntityById(savedEmployee.getId()));
    }

    @Transactional
    public EmployeeResponseDto update(String id, EmployeeRequest request, UserRole updaterRole) {
        Employee employee = findEntityById(id);
        Grade grade = gradeRepository.findById(request.getGradeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Grade not found"));
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found"));
        employee.setName(request.getName());
        employee.setEmail(request.getEmail());
        employee.setHireDate(request.getHireDate());
        if (request.getLeaveBalance() != null) {
            employee.setLeaveBalance(request.getLeaveBalance());
        }
        if (request.getStatus() != null) {
            if (updaterRole == UserRole.LOGISTICS_STAFF) {
                if (request.getStatus() == EmployeeStatus.ACTIVE) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                            "Only a Super Admin can activate an employee; use the activate endpoint.");
                }
                employee.setStatus(request.getStatus());
            } else {
                employee.setStatus(request.getStatus());
            }
        }
        employee.setGrade(grade);
        employee.setDepartment(department);
        Employee savedEmployee = employeeRepository.save(employee);
        applyLogisticsStaffGrantIfRequested(request.getEmail(), request.getGrantLogisticsStaffRole(), updaterRole);
        return toResponseWithPayroll(findEntityById(savedEmployee.getId()));
    }

    private void applyLogisticsStaffGrantIfRequested(String email, Boolean grant, UserRole actorRole) {
        if (actorRole != UserRole.SUPER_ADMIN || !Boolean.TRUE.equals(grant) || email == null || email.isBlank()) {
            return;
        }
        userRepository.findByEmailIgnoreCase(email.trim()).ifPresent(u -> {
            if (u.getRole() == UserRole.SUPER_ADMIN) {
                return;
            }
            u.setRole(UserRole.LOGISTICS_STAFF);
            userRepository.save(u);
        });
    }

    /**
     * Activates a PENDING_VALIDATION employee. Only SUPER_ADMIN may call this.
     */
    @Transactional
    public EmployeeResponseDto activate(String id) {
        Employee employee = findEntityById(id);
        if (employee.getStatus() == EmployeeStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Employee is already ACTIVE");
        }
        employee.setStatus(EmployeeStatus.ACTIVE);
        employeeRepository.save(employee);
        return toResponseWithPayroll(findEntityById(id));
    }

    @Transactional
    public void delete(String id) {
        if (!employeeRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found");
        }
        employeeRepository.deleteById(id);
    }

    private EmployeeResponseDto toResponseWithPayroll(Employee employee) {
        PayrollResult payroll = payrollService.calculate(employee);
        return HrResponseMapper.toEmployeeResponse(employee, payroll.calculatedSalary());
    }
}
