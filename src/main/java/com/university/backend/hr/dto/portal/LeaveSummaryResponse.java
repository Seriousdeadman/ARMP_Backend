package com.university.backend.hr.dto.portal;

import lombok.Builder;

@Builder
public record LeaveSummaryResponse(
        boolean employeeFound,
        String displayName,
        Integer remainingLeaveDays
) {
    public static LeaveSummaryResponse notLinked() {
        return LeaveSummaryResponse.builder()
                .employeeFound(false)
                .build();
    }
}
