package com.zosh.service;

import com.zosh.model.AuditLog;

public interface AuditService {
    AuditLog logAction(String performedBy, String action, String targetUser, String oldValue, String newValue);
}
