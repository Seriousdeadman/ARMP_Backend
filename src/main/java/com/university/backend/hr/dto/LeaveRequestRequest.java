package com.university.backend.hr.dto;

import com.university.backend.hr.enums.LeaveRequestStatus;
import com.university.backend.hr.enums.LeaveType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveRequestRequest {

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    @NotNull
    private LeaveType type;

    @NotNull
    private LeaveRequestStatus status;

    @NotBlank
    private String employeeId;
}
