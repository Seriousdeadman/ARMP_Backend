package com.university.backend.hr.dto;

import com.university.backend.hr.enums.GradeName;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradeRequest {

    @NotNull
    private GradeName name;

    @NotNull
    private BigDecimal baseSalary;

    @NotNull
    private BigDecimal hourlyBonus;
}
