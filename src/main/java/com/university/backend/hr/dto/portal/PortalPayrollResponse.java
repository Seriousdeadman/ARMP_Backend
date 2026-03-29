package com.university.backend.hr.dto.portal;

import lombok.Builder;

import java.math.BigDecimal;

/**
 * Payroll summary returned to the employee self-service portal.
 */
@Builder
public record PortalPayrollResponse(
        boolean employeeFound,
        String displayName,
        String gradeName,
        BigDecimal baseSalary,
        BigDecimal dailyRate,
        Integer leaveBalance,
        BigDecimal deduction,
        BigDecimal calculatedSalary
) {
    public static PortalPayrollResponse notLinked() {
        return PortalPayrollResponse.builder()
                .employeeFound(false)
                .build();
    }
}
