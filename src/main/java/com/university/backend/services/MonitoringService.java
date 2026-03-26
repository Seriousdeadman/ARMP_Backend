package com.university.backend.services;

import com.university.backend.entities.Session;
import com.university.backend.entities.UserStatistics;
import com.university.backend.repositories.SessionRepository;
import com.university.backend.repositories.UserStatisticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MonitoringService {

    private final SessionRepository sessionRepository;
    private final UserStatisticsRepository userStatisticsRepository;

    @Async
    @Transactional
    public void updateStatisticsForUser(String userId) {

        List<Session> sessions = sessionRepository.findByUserId(userId);

        if (sessions.isEmpty()) return;

        long totalLogins = sessions.size();

        double totalMinutes = sessions.stream()
                .filter(s -> s.getDurationMinutes() != null)
                .mapToLong(Session::getDurationMinutes)
                .sum();

        double totalHours = totalMinutes / 60.0;

        double avgDuration = sessions.stream()
                .filter(s -> s.getDurationMinutes() != null)
                .mapToLong(Session::getDurationMinutes)
                .average()
                .orElse(0.0);

        long distinctDevices = sessions.stream()
                .filter(s -> s.getDeviceType() != null)
                .map(Session::getDeviceType)
                .distinct()
                .count();

        String mostUsedDevice = sessions.stream()
                .filter(s -> s.getDeviceType() != null)
                .collect(Collectors.groupingBy(
                        Session::getDeviceType,
                        Collectors.counting()
                ))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Unknown");

        int peakHour = sessions.stream()
                .filter(s -> s.getLoginAt() != null)
                .collect(Collectors.groupingBy(
                        s -> s.getLoginAt().getHour(),
                        Collectors.counting()
                ))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(0);

        userStatisticsRepository.findByUserId(userId).ifPresent(stats -> {
            stats.setTotalLoginCount((int) totalLogins);
            stats.setTotalHoursConnected(totalHours);
            stats.setAverageSessionDuration(avgDuration);
            stats.setTotalDevicesUsed((int) distinctDevices);
            stats.setMostUsedDevice(mostUsedDevice);
            stats.setPeakUsageHour(peakHour);
            stats.setLastUpdated(LocalDateTime.now());
            userStatisticsRepository.save(stats);
        });
    }

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void archiveOldSessions() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(90);
        List<Session> oldSessions = sessionRepository
                .findByUserId("all")
                .stream()
                .filter(s -> s.getLoginAt().isBefore(cutoff))
                .toList();

        if (!oldSessions.isEmpty()) {
            sessionRepository.deleteAll(oldSessions);
        }
    }
}