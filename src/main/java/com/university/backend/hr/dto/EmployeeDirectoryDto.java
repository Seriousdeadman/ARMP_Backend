package com.university.backend.hr.dto;

import com.university.backend.hr.enums.GradeName;

public record EmployeeDirectoryDto(
        String id,
        String name,
        String email,
        GradeName gradeName,
        int leaveBalanceDays,
        String departmentName
) {}
