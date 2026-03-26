package com.university.backend.controllers;

import com.university.backend.dto.AuditLogDTO;
import com.university.backend.dto.AuditLogResponseDTO;
import com.university.backend.entities.AuditLog;
import com.university.backend.entities.User;
import com.university.backend.services.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @PostMapping("/log")
    public ResponseEntity<Void> logAction(
            @AuthenticationPrincipal User user,
            @RequestBody AuditLogDTO dto,
            HttpServletRequest request
    ) {
        String ipAddress = request.getRemoteAddr();
        auditLogService.log(user, dto, ipAddress);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my-logs")
    public ResponseEntity<List<AuditLogResponseDTO>> getMyLogs(
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(
                auditLogService.getLogsForUser(user.getId())
                        .stream()
                        .map(this::toDTO)
                        .toList()
        );
    }

    @GetMapping("/my-logs/{module}")
    public ResponseEntity<List<AuditLogResponseDTO>> getMyLogsByModule(
            @AuthenticationPrincipal User user,
            @PathVariable String module
    ) {
        return ResponseEntity.ok(
                auditLogService.getLogsForUserByModule(user.getId(), module)
                        .stream()
                        .map(this::toDTO)
                        .toList()
        );
    }
    private AuditLogResponseDTO toDTO(AuditLog log) {
        return AuditLogResponseDTO.builder()
                .id(log.getId())
                .userId(log.getUser().getId())
                .action(log.getAction())
                .module(log.getModule())
                .page(log.getPage())
                .method(log.getMethod())
                .details(log.getDetails())
                .ipAddress(log.getIpAddress())
                .timestamp(log.getTimestamp())
                .build();
    }
}