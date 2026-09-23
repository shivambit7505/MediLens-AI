package com.medilens.service;

import com.medilens.model.AuditLog;
import com.medilens.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void logEvent(
            UUID userId,
            String action,
            String resourceType,
            String resourceId,
            String ipAddress,
            String userAgent,
            int statusCode,
            String detailsJson
    ) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .userId(userId)
                    .action(action)
                    .resourceType(resourceType)
                    .resourceId(resourceId)
                    .ipAddress(ipAddress != null ? ipAddress : "127.0.0.1")
                    .userAgent(userAgent != null ? (userAgent.length() > 500 ? userAgent.substring(0, 500) : userAgent) : "Unknown")
                    .statusCode(statusCode)
                    .detailsJson(detailsJson)
                    .timestamp(Instant.now())
                    .build();

            auditLogRepository.save(auditLog);
            log.info("Audit event recorded: action={}, user={}, status={}", action, userId, statusCode);
        } catch (Exception ex) {
            log.error("Failed to write audit log entry: {}", ex.getMessage());
        }
    }
}
