package com.university.backend.hr.dto.portal;

import com.university.backend.hr.enums.LeaveType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateLeaveRequestDto(
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        @NotNull LeaveType type
) {}
