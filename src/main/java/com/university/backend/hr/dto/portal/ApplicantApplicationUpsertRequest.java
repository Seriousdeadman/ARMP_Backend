package com.university.backend.hr.dto.portal;

import jakarta.validation.constraints.NotBlank;

public record ApplicantApplicationUpsertRequest(
        @NotBlank String departmentId,
        @NotBlank String skillsAndExperience
) {}
