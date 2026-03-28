package com.university.backend.hr.dto.portal;

import lombok.Builder;

@Builder
public record LeavePreviewResponse(
        int requestedDays,
        int currentRemainingDays,
        int remainingAfterApproval
) {
}
