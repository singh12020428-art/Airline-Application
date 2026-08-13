package com.zosh.service.Impl;

import com.zosh.model.AuditLog;
import com.zosh.repository.AuditLogRepository;
import com.zosh.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;

    @Override
    public AuditLog logAction(String performedBy, String action, String targetUser, String oldValue, String newValue) {
        AuditLog auditLog = AuditLog.builder()
                .performedBy(performedBy)
                .action(action)
                .targetUser(targetUser)
                .oldValue(oldValue)
                .newValue(newValue)
                .createdAt(LocalDateTime.now())
                .build();
        return auditLogRepository.save(auditLog);
    }
}
