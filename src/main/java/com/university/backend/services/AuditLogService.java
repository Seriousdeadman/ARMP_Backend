package com.university.backend.services;

import com.university.backend.dto.AuditLogDTO;
import com.university.backend.entities.AuditLog;
import com.university.backend.entities.User;
import com.university.backend.repositories.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Async
    public void log(User user, AuditLogDTO dto, String ipAddress) {
        AuditLog log = AuditLog.builder()
                .user(user)
                .action(dto.getAction())
                .module(dto.getModule())
                .page(dto.getPage())
                .method(dto.getMethod())
                .details(dto.getDetails())
                .ipAddress(ipAddress)
                .build();
        auditLogRepository.save(log);
    }

    public List<AuditLog> getLogsForUser(String userId) {
        return auditLogRepository.findByUserIdOrderByTimestampDesc(userId);
    }

    public List<AuditLog> getLogsForUserByModule(String userId, String module) {
        return auditLogRepository.findByUserIdAndModuleOrderByTimestampDesc(userId, module);
    }
}