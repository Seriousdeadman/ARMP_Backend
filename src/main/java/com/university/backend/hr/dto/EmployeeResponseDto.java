package com.university.backend.hr.dto;

import com.university.backend.hr.enums.EmployeeStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record EmployeeResponseDto(
        String id,
        String name,
        String email,
        LocalDate hireDate,
        Integer leaveBalance,
        EmployeeStatus status,
        GradeSummaryDto grade,
        DepartmentSummaryDto department,
        BigDecimal calculatedSalary
) {
}
