package com.university.backend.hr.dto;

import lombok.Builder;

import java.math.BigDecimal;

/**
 * Full payroll breakdown for one employee for the current month.
 * <p>
 * Business rules:
 * <ul>
 *   <li>monthlyWorkDays = 22</li>
 *   <li>dailyRate       = baseSalary / 22</li>
 *   <li>deduction       = max(0, abs(leaveBalance)) * dailyRate  (only when leaveBalance &lt; 0)</li>
 *   <li>calculatedSalary = baseSalary - deduction</li>
 * </ul>
 */
@Builder
public record PayrollResult(
        BigDecimal baseSalary,
        BigDecimal dailyRate,
        Integer leaveBalance,
        BigDecimal deduction,
        BigDecimal calculatedSalary
) {}
