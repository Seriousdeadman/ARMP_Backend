package com.university.backend.hr.dto;

import com.university.backend.hr.enums.GradeName;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record GradeSummaryDto(
        String id,
        GradeName name,
        BigDecimal baseSalary,
        BigDecimal hourlyBonus
) {
}
