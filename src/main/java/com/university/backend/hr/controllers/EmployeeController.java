package com.university.backend.hr.controllers;

import com.university.backend.hr.dto.EmployeeRequest;
import com.university.backend.hr.dto.EmployeeResponseDto;
import com.university.backend.hr.dto.PayrollResult;
import com.university.backend.entities.User;
import com.university.backend.hr.services.EmployeeService;
import com.university.backend.hr.services.PayrollService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hr/employees")
@PreAuthorize("hasAnyRole('LOGISTICS_STAFF', 'SUPER_ADMIN')")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final PayrollService payrollService;

    public EmployeeController(EmployeeService employeeService, PayrollService payrollService) {
        this.employeeService = employeeService;
        this.payrollService = payrollService;
    }

    @GetMapping
    public ResponseEntity<List<EmployeeResponseDto>> list(@RequestParam(required = false) String departmentId) {
        if (departmentId != null) {
            return ResponseEntity.ok(employeeService.findByDepartmentId(departmentId));
        }
        return ResponseEntity.ok(employeeService.findAll());
    }

    /**
     * Returns only PENDING_VALIDATION employees for the Super Admin onboarding approvals view.
     */
    @GetMapping("/pending")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<EmployeeResponseDto>> listPending() {
        return ResponseEntity.ok(employeeService.findPendingValidation());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponseDto> get(@PathVariable String id) {
        return ResponseEntity.ok(employeeService.findById(id));
    }

    @PostMapping
    public ResponseEntity<EmployeeResponseDto> create(
            @Valid @RequestBody EmployeeRequest request,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.create(request, user.getRole()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponseDto> update(
            @PathVariable String id,
            @Valid @RequestBody EmployeeRequest request,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(employeeService.update(id, request, user.getRole()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        employeeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Activates a PENDING_VALIDATION employee — Super Admin only.
     */
    @PostMapping("/{id}/activate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<EmployeeResponseDto> activate(@PathVariable String id) {
        return ResponseEntity.ok(employeeService.activate(id));
    }

    /**
     * Full payroll breakdown (baseSalary, dailyRate, deduction, calculatedSalary).
     */
    @GetMapping("/{id}/monthly-pay")
    public ResponseEntity<PayrollResult> monthlyPay(@PathVariable String id) {
        return ResponseEntity.ok(payrollService.calculate(employeeService.findEntityById(id)));
    }
}
