package com.university.backend.hr.services;

import com.university.backend.hr.dto.EmployeeRequest;
import com.university.backend.hr.dto.EmployeeResponseDto;
import com.university.backend.hr.dto.HrResponseMapper;
import com.university.backend.hr.entities.Department;
import com.university.backend.hr.entities.Employee;
import com.university.backend.hr.entities.Grade;
import com.university.backend.hr.repositories.DepartmentRepository;
import com.university.backend.hr.repositories.EmployeeRepository;
import com.university.backend.hr.repositories.GradeRepository;
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

    public List<EmployeeResponseDto> findAll() {
        return employeeRepository.findAll().stream()
                .map(HrResponseMapper::toEmployeeResponse)
                .collect(Collectors.toList());
    }

    public List<EmployeeResponseDto> findByDepartmentId(String departmentId) {
        return employeeRepository.findByDepartmentId(departmentId).stream()
                .map(HrResponseMapper::toEmployeeResponse)
                .collect(Collectors.toList());
    }

    public EmployeeResponseDto findById(String id) {
        return HrResponseMapper.toEmployeeResponse(findEntityById(id));
    }

    public Employee findEntityById(String id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
    }

    @Transactional
    public EmployeeResponseDto create(EmployeeRequest request) {
        Grade grade = gradeRepository.findById(request.getGradeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Grade not found"));
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found"));
        Employee.EmployeeBuilder builder = Employee.builder()
                .name(request.getName())
                .email(request.getEmail())
                .hireDate(request.getHireDate())
                .grade(grade)
                .department(department);
        if (request.getLeaveBalance() != null) {
            builder.leaveBalance(request.getLeaveBalance());
        } else {
            builder.leaveBalance(21);
        }
        Employee savedEmployee = employeeRepository.save(builder.build());
        return HrResponseMapper.toEmployeeResponse(findEntityById(savedEmployee.getId()));
    }

    @Transactional
    public EmployeeResponseDto update(String id, EmployeeRequest request) {
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
        employee.setGrade(grade);
        employee.setDepartment(department);
        Employee savedEmployee = employeeRepository.save(employee);
        return HrResponseMapper.toEmployeeResponse(findEntityById(savedEmployee.getId()));
    }

    @Transactional
    public void delete(String id) {
        if (!employeeRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found");
        }
        employeeRepository.deleteById(id);
    }
}
