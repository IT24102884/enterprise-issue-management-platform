package com.projectmanager.service;

import com.projectmanager.dto.NotificationResponse;
import com.projectmanager.dto.PagedResponse;
import com.projectmanager.dto.UnreadCountResponse;
import com.projectmanager.entity.User;
import org.springframework.data.domain.Pageable;

public interface NotificationService {
    void sendNotification(User recipient, String message, String type, String referenceId);
    PagedResponse<NotificationResponse> getUserNotifications(Long userId, Boolean unreadOnly, Pageable pageable);
    UnreadCountResponse getUnreadCount(Long userId);
    NotificationResponse markAsRead(Long notificationId, Long userId);
    void markAllAsRead(Long userId);
}
