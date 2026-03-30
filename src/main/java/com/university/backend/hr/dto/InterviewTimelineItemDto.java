package com.university.backend.hr.dto;

import com.university.backend.hr.enums.InterviewStatus;

import java.time.LocalDateTime;

public record InterviewTimelineItemDto(
        String id,
        LocalDateTime interviewDate,
        String location,
        InterviewStatus status,
        Integer score
) {
}
