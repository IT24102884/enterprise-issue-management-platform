package com.projectmanager.service.impl;

import com.projectmanager.dto.NotificationResponse;
import com.projectmanager.dto.PagedResponse;
import com.projectmanager.dto.UnreadCountResponse;
import com.projectmanager.entity.Notification;
import com.projectmanager.entity.User;
import com.projectmanager.exception.ForbiddenException;
import com.projectmanager.exception.ResourceNotFoundException;
import com.projectmanager.mapper.NotificationMapper;
import com.projectmanager.repository.NotificationRepository;
import com.projectmanager.repository.UserRepository;
import com.projectmanager.service.NotificationService;
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
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;

    @Override
    @Transactional
    public void sendNotification(User recipient, String message, String type, String referenceId) {
        if (recipient == null) {
            return;
        }

        try {
            Notification notification = Notification.builder()
                    .user(recipient)
                    .message(message)
                    .type(type)
                    .referenceId(referenceId)
                    .read(false)
                    .build();

            notificationRepository.save(notification);
        } catch (Exception e) {
            log.error("Failed to persist notification for user {}: {}", recipient.getId(), e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<NotificationResponse> getUserNotifications(Long userId, Boolean unreadOnly, Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        Page<Notification> notificationPage;
        if (Boolean.TRUE.equals(unreadOnly)) {
            notificationPage = notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId, pageable);
        } else {
            notificationPage = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        }

        List<NotificationResponse> content = notificationPage.getContent().stream()
                .map(notificationMapper::toResponse)
                .collect(Collectors.toList());

        return PagedResponse.<NotificationResponse>builder()
                .content(content)
                .page(notificationPage.getNumber())
                .size(notificationPage.getSize())
                .totalElements(notificationPage.getTotalElements())
                .totalPages(notificationPage.getTotalPages())
                .last(notificationPage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UnreadCountResponse getUnreadCount(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        long count = notificationRepository.countByUserIdAndReadFalse(userId);
        return new UnreadCountResponse(count);
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));

        if (!notification.getUser().getId().equals(userId)) {
            throw new ForbiddenException("You are not authorized to update this notification");
        }

        notification.setRead(true);
        Notification updated = notificationRepository.save(notification);
        return notificationMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        notificationRepository.markAllAsReadForUser(userId);
    }
}
