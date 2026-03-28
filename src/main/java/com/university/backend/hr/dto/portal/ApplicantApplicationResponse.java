package com.university.backend.hr.dto.portal;

import com.university.backend.hr.enums.CandidateStatus;
import lombok.Builder;

@Builder
public record ApplicantApplicationResponse(
        String candidateId,
        String name,
        String email,
        String phone,
        CandidateStatus status,
        String departmentId,
        String departmentName,
        String skillsAndExperience,
        boolean hasCvFile
) {}
