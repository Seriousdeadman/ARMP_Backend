package com.university.backend.hr.dto;

import com.university.backend.hr.enums.CandidateStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record RecruitmentAssignmentDto(
        String employeeId,
        String candidateId,
        String name,
        String email,
        String departmentName,
        String gradeName,
        LocalDate hireDate,
        Instant promotedAt,
        String promotedByLabel,
        CandidateStatus candidatePipelineStatus,
        List<InterviewTimelineItemDto> interviews
) {
}
