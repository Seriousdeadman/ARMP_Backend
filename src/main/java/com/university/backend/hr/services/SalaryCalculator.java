package com.university.backend.hr.services;

import com.university.backend.hr.entities.Employee;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@Service
public class SalaryCalculator {

    public BigDecimal calculateMonthlyPay(Employee employee) {
        if (employee == null || employee.getGrade() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Employee must have a grade");
        }
        return employee.getGrade().getBaseSalary();
    }
}
