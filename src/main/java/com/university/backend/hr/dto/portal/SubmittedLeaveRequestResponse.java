package com.university.backend.hr.dto.portal;

import java.time.LocalDate;

public record SubmittedLeaveRequestResponse(
        String id,
        LocalDate startDate,
        LocalDate endDate,
        String type,
        String status
) {}
