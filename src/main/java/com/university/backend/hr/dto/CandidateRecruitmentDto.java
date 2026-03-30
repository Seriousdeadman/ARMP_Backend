package com.university.backend.hr.dto;

import com.university.backend.hr.enums.CandidateStatus;

public record CandidateRecruitmentDto(
        String id,
        String name,
        String email,
        String phone,
        CandidateStatus status,
        String departmentId,
        String departmentName,
        Integer interviewScore
) {}
