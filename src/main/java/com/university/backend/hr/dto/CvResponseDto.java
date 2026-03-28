package com.university.backend.hr.dto;

import lombok.Builder;

@Builder
public record CvResponseDto(
        String id,
        String skillsAndExperience,
        String fileName,
        String fileContentType,
        Long fileSizeBytes,
        String fileStoragePath
) {
}
