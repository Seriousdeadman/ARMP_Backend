package com.university.backend.hr.dto;

import com.university.backend.hr.enums.CandidateStatus;
import lombok.Builder;

@Builder
public record CandidateResponseDto(
        String id,
        String name,
        String email,
        String phone,
        CandidateStatus status,
        DepartmentSummaryDto department,
        CvResponseDto cv
) {
}
