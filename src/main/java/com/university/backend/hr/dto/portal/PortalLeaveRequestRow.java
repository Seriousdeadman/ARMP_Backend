package com.university.backend.hr.dto.portal;

import java.time.LocalDate;

public record PortalLeaveRequestRow(
        String id,
        LocalDate startDate,
        LocalDate endDate,
        String type,
        String status,
        Integer requestedDays,
        String reason,
        String statusMessage
) {
}
