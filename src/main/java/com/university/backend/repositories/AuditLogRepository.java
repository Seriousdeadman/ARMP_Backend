package com.university.backend.repositories;

import com.university.backend.entities.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, String> {

    List<AuditLog> findByUserIdOrderByTimestampDesc(String userId);

    List<AuditLog> findByUserIdAndModuleOrderByTimestampDesc(String userId, String module);
}