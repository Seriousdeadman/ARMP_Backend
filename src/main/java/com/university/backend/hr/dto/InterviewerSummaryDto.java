package com.university.backend.hr.dto;

import lombok.Builder;

@Builder
public record InterviewerSummaryDto(
        String id,
        String name,
        String email
) {
}
