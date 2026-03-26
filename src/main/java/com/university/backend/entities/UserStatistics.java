package com.university.backend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_statistics")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private Integer totalLoginCount = 0;

    @Column(nullable = false)
    private Double totalHoursConnected = 0.0;

    @Column(nullable = false)
    private Double averageSessionDuration = 0.0;

    @Column(nullable = false)
    private Integer totalDevicesUsed = 0;

    @Column
    private String mostUsedDevice;

    @Column
    private String mostVisitedPage;

    @Column
    private Integer peakUsageHour;

    @Column
    private LocalDateTime lastUpdated;

    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }
}