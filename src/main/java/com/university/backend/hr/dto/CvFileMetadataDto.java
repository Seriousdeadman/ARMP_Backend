package com.university.backend.hr.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CvFileMetadataDto {

    private String candidateId;
    private String fileName;
    private String contentType;
    private Long sizeBytes;
    private boolean filePresent;
}
