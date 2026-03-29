package com.university.backend.hr.services;

import com.university.backend.hr.entities.Employee;
import com.university.backend.hr.entities.Grade;
import com.university.backend.hr.enums.GradeName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class PayrollServiceTest {

    private final PayrollService payrollService = new PayrollService();

    @Test
    void calculateMonthlyPay_fromEmployee() {
        Grade grade = Grade.builder()
                .name(GradeName.ASSISTANT)
                .baseSalary(new BigDecimal("3000.00"))
                .hourlyBonus(BigDecimal.ZERO)
                .build();
        Employee employee = Employee.builder()
                .grade(grade)
                .build();
        assertThat(payrollService.calculateMonthlyPay(employee))
                .isEqualByComparingTo(new BigDecimal("3000.00"));
    }
}
