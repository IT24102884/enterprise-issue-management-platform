package com.projectmanager.service;

import com.projectmanager.dto.AuditLogResponse;
import com.projectmanager.dto.PagedResponse;
import com.projectmanager.entity.User;
import org.springframework.data.domain.Pageable;

public interface AuditLogService {
    void logAction(User user, String action, String entityType, Long entityId, String metadata);
    PagedResponse<AuditLogResponse> getLogsForEntity(String entityType, Long entityId, Pageable pageable);
}
