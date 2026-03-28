package com.university.backend.hr.dto;

import com.university.backend.hr.enums.InterviewStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record InterviewResponseDto(
        String id,
        LocalDateTime interviewDate,
        String location,
        Integer score,
        InterviewStatus status,
        CandidateResponseDto candidate
) {
}
