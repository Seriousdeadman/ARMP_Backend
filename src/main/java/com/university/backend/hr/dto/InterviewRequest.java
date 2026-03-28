package com.university.backend.hr.dto;

import com.university.backend.hr.enums.InterviewStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewRequest {

    @NotNull
    private LocalDateTime interviewDate;

    @NotBlank
    private String location;

    @Min(0)
    @Max(20)
    private Integer score;

    @NotNull
    private InterviewStatus status;

    @NotBlank
    private String candidateId;
}
