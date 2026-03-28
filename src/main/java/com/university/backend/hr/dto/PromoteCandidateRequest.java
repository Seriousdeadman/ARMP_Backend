package com.university.backend.hr.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromoteCandidateRequest {

    /**
     * Optional grade id; when omitted, defaults to ASSISTANT grade (legacy behavior).
     */
    private String gradeId;
}
