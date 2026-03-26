package com.university.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponseDTO {

    private String id;
    private String userId;
    private String action;
    private String module;
    private String page;
    private String method;
    private String details;
    private String ipAddress;
    private LocalDateTime timestamp;
}