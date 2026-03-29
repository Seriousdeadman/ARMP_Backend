package com.university.backend.hr.controllers;

import com.university.backend.hr.dto.EmployeeResponseDto;
import com.university.backend.hr.dto.GradeSummaryDto;
import com.university.backend.hr.dto.HrResponseMapper;
import com.university.backend.hr.enums.EmployeeStatus;
import com.university.backend.hr.services.EmployeeService;
import com.university.backend.hr.services.GradeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/hr/integration")
public class HrIntegrationController {

    private final EmployeeService employeeService;
    private final GradeService gradeService;

    public HrIntegrationController(EmployeeService employeeService, GradeService gradeService) {
        this.employeeService = employeeService;
        this.gradeService = gradeService;
    }

    /**
     * Publicly accessible API for other modules to fetch active employees.
     * Accessible by any authenticated user.
     */
    @GetMapping("/employees")
    public ResponseEntity<List<EmployeeResponseDto>> listActiveEmployees() {
        List<EmployeeResponseDto> activeEmployees = employeeService.findAll().stream()
                .filter(e -> e.status() == EmployeeStatus.ACTIVE)
                .collect(Collectors.toList());
        return ResponseEntity.ok(activeEmployees);
    }

    /**
     * Salary grades for cross-module rules (e.g. parking permit tiers, dining subsidies).
     */
    @GetMapping("/grades")
    public ResponseEntity<List<GradeSummaryDto>> listGrades() {
        List<GradeSummaryDto> grades = gradeService.findAll().stream()
                .map(HrResponseMapper::toGradeSummary)
                .collect(Collectors.toList());
        return ResponseEntity.ok(grades);
    }
}
