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
public class SessionDTO {

    private String id;
    private String userId;
    private LocalDateTime loginAt;
    private LocalDateTime logoutAt;
    private Long durationMinutes;
    private String ipAddress;
    private String userAgent;
    private String deviceType;
    private String operatingSystem;
    private String browser;
    private String location;
    private Boolean isActive;
}