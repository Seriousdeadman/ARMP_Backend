package com.university.backend.hr.controllers;

import com.university.backend.hr.dto.EmployeeRequest;
import com.university.backend.hr.dto.EmployeeResponseDto;
import com.university.backend.hr.services.EmployeeService;
import com.university.backend.hr.services.SalaryCalculator;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/hr/employees")
@PreAuthorize("hasAnyRole('LOGISTICS_STAFF', 'SUPER_ADMIN')")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final SalaryCalculator salaryCalculator;

    public EmployeeController(EmployeeService employeeService, SalaryCalculator salaryCalculator) {
        this.employeeService = employeeService;
        this.salaryCalculator = salaryCalculator;
    }

    @GetMapping
    public ResponseEntity<List<EmployeeResponseDto>> list(@RequestParam(required = false) String departmentId) {
        if (departmentId != null) {
            return ResponseEntity.ok(employeeService.findByDepartmentId(departmentId));
        }
        return ResponseEntity.ok(employeeService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponseDto> get(@PathVariable String id) {
        return ResponseEntity.ok(employeeService.findById(id));
    }

    @PostMapping
    public ResponseEntity<EmployeeResponseDto> create(@Valid @RequestBody EmployeeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponseDto> update(
            @PathVariable String id,
            @Valid @RequestBody EmployeeRequest request
    ) {
        return ResponseEntity.ok(employeeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        employeeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/monthly-pay")
    public ResponseEntity<BigDecimal> monthlyPay(@PathVariable String id) {
        return ResponseEntity.ok(salaryCalculator.calculateMonthlyPay(employeeService.findEntityById(id)));
    }
}
