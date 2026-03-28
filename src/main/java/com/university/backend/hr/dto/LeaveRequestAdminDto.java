package com.university.backend.hr.dto;

import com.university.backend.hr.enums.LeaveRequestStatus;
import com.university.backend.hr.enums.LeaveType;

import java.time.LocalDate;

public record LeaveRequestAdminDto(
        String id,
        String employeeId,
        String employeeName,
        String employeeEmail,
        LocalDate startDate,
        LocalDate endDate,
        LeaveType type,
        LeaveRequestStatus status,
        long requestedDayCount
) {}
