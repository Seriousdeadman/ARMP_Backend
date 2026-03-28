package com.university.backend.hr.dto;

import lombok.Builder;

@Builder
public record DepartmentSummaryDto(
        String id,
        String name
) {
}
