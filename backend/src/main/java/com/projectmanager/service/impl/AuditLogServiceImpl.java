package com.projectmanager.service.impl;

import com.projectmanager.dto.AuditLogResponse;
import com.projectmanager.dto.PagedResponse;
import com.projectmanager.entity.AuditLog;
import com.projectmanager.entity.User;
import com.projectmanager.mapper.IssueMapper;
import com.projectmanager.repository.AuditLogRepository;
import com.projectmanager.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final IssueMapper issueMapper;

    @Override
    @Transactional
    public void logAction(User user, String action, String entityType, Long entityId, String metadata) {
        try {
            AuditLog logEntry = AuditLog.builder()
                    .user(user)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .metadata(metadata)
                    .build();

            auditLogRepository.save(logEntry);
        } catch (Exception e) {
            log.error("Failed to persist audit log: {}", e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AuditLogResponse> getLogsForEntity(String entityType, Long entityId, Pageable pageable) {
        Page<AuditLog> logPage = auditLogRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc(entityType, entityId, pageable);

        List<AuditLogResponse> content = logPage.getContent().stream()
                .map(issueMapper::toAuditLogResponse)
                .collect(Collectors.toList());

        return PagedResponse.<AuditLogResponse>builder()
                .content(content)
                .page(logPage.getNumber())
                .size(logPage.getSize())
                .totalElements(logPage.getTotalElements())
                .totalPages(logPage.getTotalPages())
                .last(logPage.isLast())
                .build();
    }
}
