package com.university.backend.controllers;

import com.university.backend.dto.SessionDTO;
import com.university.backend.dto.UserStatisticsDTO;
import com.university.backend.entities.Session;
import com.university.backend.entities.UserStatistics;
import com.university.backend.repositories.SessionRepository;
import com.university.backend.repositories.UserStatisticsRepository;
import com.university.backend.repositories.RefreshTokenRepository;
import com.university.backend.entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.university.backend.repositories.UserRepository;

import java.util.List;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final SessionRepository sessionRepository;
    private final UserStatisticsRepository userStatisticsRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/stats")
    public ResponseEntity<UserStatisticsDTO> getMyStatistics(
            @AuthenticationPrincipal User user
    ) {
        UserStatisticsDTO dto = userStatisticsRepository
                .findByUserId(user.getId())
                .map(this::toStatisticsDTO)
                .orElseGet(() -> emptyStatisticsDTO(user.getId()));
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/sessions/active")
    public ResponseEntity<List<SessionDTO>> getMyActiveSessions(
            @AuthenticationPrincipal User user
    ) {
        List<SessionDTO> sessions = sessionRepository
                .findByUserIdAndIsActiveTrue(user.getId())
                .stream()
                .map(this::toSessionDTO)
                .toList();
        return ResponseEntity.ok(sessions);
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Void> revokeSession(
            @AuthenticationPrincipal User user,
            @PathVariable String sessionId
    ) {
        sessionRepository.findById(sessionId).ifPresent(session -> {
            if (session.getUser().getId().equals(user.getId())) {
                session.setIsActive(false);
                sessionRepository.save(session);

                refreshTokenRepository.findByUserId(user.getId())
                        .forEach(token -> {
                            token.setIsRevoked(true);
                            refreshTokenRepository.save(token);
                        });
            }
        });
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<SessionDTO>> getMySessions(
            @AuthenticationPrincipal User user
    ) {
        List<SessionDTO> sessions = sessionRepository
                .findByUserId(user.getId())
                .stream()
                .map(this::toSessionDTO)
                .toList();
        return ResponseEntity.ok(sessions);
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal User user,
            @RequestParam String oldPassword,
            @RequestParam String newPassword
    ) {
        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            return ResponseEntity.badRequest().build();
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        refreshTokenRepository.findByUserId(user.getId())
                .forEach(token -> {
                    token.setIsRevoked(true);
                    refreshTokenRepository.save(token);
                });

        return ResponseEntity.noContent().build();
    }

    private UserStatisticsDTO toStatisticsDTO(UserStatistics entity) {
        return UserStatisticsDTO.builder()
                .id(entity.getId())
                .userId(entity.getUser().getId())
                .totalLoginCount(entity.getTotalLoginCount())
                .totalHoursConnected(entity.getTotalHoursConnected())
                .averageSessionDuration(entity.getAverageSessionDuration())
                .totalDevicesUsed(entity.getTotalDevicesUsed())
                .mostUsedDevice(entity.getMostUsedDevice())
                .mostVisitedPage(entity.getMostVisitedPage())
                .peakUsageHour(entity.getPeakUsageHour())
                .lastUpdated(entity.getLastUpdated())
                .build();
    }

    private UserStatisticsDTO emptyStatisticsDTO(String userId) {
        return UserStatisticsDTO.builder()
                .id(null)
                .userId(userId)
                .totalLoginCount(0)
                .totalHoursConnected(0.0)
                .averageSessionDuration(0.0)
                .totalDevicesUsed(0)
                .mostUsedDevice(null)
                .mostVisitedPage(null)
                .peakUsageHour(null)
                .lastUpdated(null)
                .build();
    }

    private SessionDTO toSessionDTO(Session session) {
        return SessionDTO.builder()
                .id(session.getId())
                .userId(session.getUser().getId())
                .loginAt(session.getLoginAt())
                .logoutAt(session.getLogoutAt())
                .durationMinutes(session.getDurationMinutes())
                .ipAddress(session.getIpAddress())
                .userAgent(session.getUserAgent())
                .deviceType(session.getDeviceType())
                .operatingSystem(session.getOperatingSystem())
                .browser(session.getBrowser())
                .location(session.getLocation())
                .isActive(session.getIsActive())
                .build();
    }
}