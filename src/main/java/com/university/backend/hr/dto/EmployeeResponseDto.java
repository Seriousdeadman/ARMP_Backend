package com.university.backend.hr.dto;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record EmployeeResponseDto(
        String id,
        String name,
        String email,
        LocalDate hireDate,
        Integer leaveBalance,
        GradeSummaryDto grade,
        DepartmentSummaryDto department
) {
}
