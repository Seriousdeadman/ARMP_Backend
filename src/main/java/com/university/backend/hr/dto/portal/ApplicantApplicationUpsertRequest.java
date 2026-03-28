package com.university.backend.hr.dto.portal;

import jakarta.validation.constraints.NotBlank;

public record ApplicantApplicationUpsertRequest(
        @NotBlank String name,
        @NotBlank String phone,
        @NotBlank String departmentId,
        @NotBlank String skillsAndExperience
) {}
