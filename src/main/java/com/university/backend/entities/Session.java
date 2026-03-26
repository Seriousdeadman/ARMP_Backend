package com.university.backend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime loginAt;

    @Column
    private LocalDateTime logoutAt;

    @Column
    private Long durationMinutes;

    @Column
    private String ipAddress;

    @Column
    private String userAgent;

    @Column
    private String deviceType;

    @Column
    private String operatingSystem;

    @Column
    private String browser;

    @Column
    private String location;

    @Column(nullable = false)
    private Boolean isActive = true;

    @PrePersist
    protected void onCreate() {
        loginAt = LocalDateTime.now();
    }
}