package com.university.backend.hr.services;

import com.university.backend.hr.dto.PayrollResult;
import com.university.backend.hr.entities.Employee;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Calculates employee monthly pay using the standard payroll formula:
 * <ul>
 *   <li>monthlyWorkDays = 22</li>
 *   <li>dailyRate       = baseSalary / 22</li>
 *   <li>deduction       = abs(leaveBalance) * dailyRate  (only when leaveBalance &lt; 0)</li>
 *   <li>calculatedSalary = baseSalary - deduction</li>
 * </ul>
 */
@Service
public class PayrollService {

    private static final int MONTHLY_WORK_DAYS = 22;
    private static final int SCALE = 2;

    public PayrollResult calculate(Employee employee) {
        if (employee == null || employee.getGrade() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Employee must have a grade");
        }
        BigDecimal baseSalary = employee.getGrade().getBaseSalary();
        BigDecimal dailyRate = baseSalary.divide(
                BigDecimal.valueOf(MONTHLY_WORK_DAYS), SCALE, RoundingMode.HALF_UP);

        int leaveBalance = employee.getLeaveBalance() != null ? employee.getLeaveBalance() : 0;
        BigDecimal deduction = BigDecimal.ZERO;
        if (leaveBalance < 0) {
            BigDecimal deficit = BigDecimal.valueOf(Math.abs(leaveBalance));
            deduction = deficit.multiply(dailyRate).setScale(SCALE, RoundingMode.HALF_UP);
        }
        BigDecimal calculatedSalary = baseSalary.subtract(deduction).setScale(SCALE, RoundingMode.HALF_UP);

        return PayrollResult.builder()
                .baseSalary(baseSalary)
                .dailyRate(dailyRate)
                .leaveBalance(leaveBalance)
                .deduction(deduction)
                .calculatedSalary(calculatedSalary)
                .build();
    }

    /** Convenience method kept for backward compatibility. */
    public BigDecimal calculateMonthlyPay(Employee employee) {
        return calculate(employee).calculatedSalary();
    }
}
