package com.university.backend.hr.dto.portal;

import lombok.Builder;

@Builder
public record ApplicationStatusResponse(
        boolean candidateFound,
        String candidateStatus,
        String message,
        String interviewScheduledAt,
        String interviewLocation
) {
    public static ApplicationStatusResponse notLinked() {
        return ApplicationStatusResponse.builder()
                .candidateFound(false)
                .build();
    }
}
