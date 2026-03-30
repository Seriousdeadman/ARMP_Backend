package com.university.backend.hr.dto;

import com.university.backend.hr.enums.EmployeeStatus;
import jakarta.validation.constraints.Email;
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
public class EmployeeRequest {

    @NotBlank
    private String name;

    @NotBlank
    @Email
    private String email;

    @NotNull
    private LocalDate hireDate;

    private Integer leaveBalance;

    /** Null means ACTIVE (backward compat). HR Staff sets PENDING_VALIDATION when drafting. */
    private EmployeeStatus status;

    @NotBlank
    private String gradeId;

    @NotBlank
    private String departmentId;

    /**
     * When true and the creator is SUPER_ADMIN, sets the {@code users} row with the same email
     * (case-insensitive) to {@code LOGISTICS_STAFF}. Ignored for other creators. Never changes {@code SUPER_ADMIN}.
     */
    private Boolean grantLogisticsStaffRole;
}
