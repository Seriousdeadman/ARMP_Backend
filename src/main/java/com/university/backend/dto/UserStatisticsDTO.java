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
public class UserStatisticsDTO {

    private String id;
    private String userId;
    private Integer totalLoginCount;
    private Double totalHoursConnected;
    private Double averageSessionDuration;
    private Integer totalDevicesUsed;
    private String mostUsedDevice;
    private String mostVisitedPage;
    private Integer peakUsageHour;
    private LocalDateTime lastUpdated;
}
