package com.projectmanager.service;

import com.projectmanager.dto.NotificationResponse;
import com.projectmanager.dto.PagedResponse;
import com.projectmanager.dto.UnreadCountResponse;
import com.projectmanager.entity.Notification;
import com.projectmanager.entity.User;
import com.projectmanager.entity.enums.Role;
import com.projectmanager.exception.ForbiddenException;
import com.projectmanager.mapper.NotificationMapper;
import com.projectmanager.repository.NotificationRepository;
import com.projectmanager.repository.UserRepository;
import com.projectmanager.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Spy
    private NotificationMapper notificationMapper = new NotificationMapper();

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private User testUser;
    private User otherUser;
    private Notification testNotification;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("Alice")
                .email("alice@acme.com")
                .role(Role.DEVELOPER)
                .build();

        otherUser = User.builder()
                .id(2L)
                .name("Bob")
                .email("bob@acme.com")
                .role(Role.DEVELOPER)
                .build();

        testNotification = Notification.builder()
                .id(1L)
                .user(testUser)
                .message("Bob assigned you issue EIM-1: Fix login")
                .type("ISSUE_ASSIGNED")
                .referenceId("EIM-1")
                .read(false)
                .createdAt(OffsetDateTime.now())
                .build();
    }

    @Test
    @DisplayName("sendNotification - Saves notification when recipient is present")
    void sendNotification_Success() {
        notificationService.sendNotification(testUser, "Bob assigned you issue EIM-1", "ISSUE_ASSIGNED", "EIM-1");

        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    @DisplayName("getUserNotifications - Returns paginated notifications (all)")
    void getUserNotifications_All_Success() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(testNotification)));

        PagedResponse<NotificationResponse> response = notificationService.getUserNotifications(
                1L, false, PageRequest.of(0, 10)
        );

        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getMessage()).isEqualTo("Bob assigned you issue EIM-1: Fix login");
    }

    @Test
    @DisplayName("getUserNotifications - Returns only unread notifications when unreadOnly=true")
    void getUserNotifications_UnreadOnly_Success() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(testNotification)));

        PagedResponse<NotificationResponse> response = notificationService.getUserNotifications(
                1L, true, PageRequest.of(0, 10)
        );

        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        verify(notificationRepository).findByUserIdAndReadFalseOrderByCreatedAtDesc(eq(1L), any(Pageable.class));
    }

    @Test
    @DisplayName("getUnreadCount - Returns unread count for badge")
    void getUnreadCount_Success() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(notificationRepository.countByUserIdAndReadFalse(1L)).thenReturn(5L);

        UnreadCountResponse response = notificationService.getUnreadCount(1L);

        assertThat(response).isNotNull();
        assertThat(response.getUnreadCount()).isEqualTo(5L);
    }

    @Test
    @DisplayName("markAsRead - Marks user notification as read")
    void markAsRead_Success() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(testNotification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        NotificationResponse response = notificationService.markAsRead(1L, 1L);

        assertThat(response).isNotNull();
        assertThat(testNotification.isRead()).isTrue();
    }

    @Test
    @DisplayName("markAsRead - Throws ForbiddenException if notification belongs to another user")
    void markAsRead_Unauthorized_ThrowsException() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(testNotification));

        assertThatThrownBy(() -> notificationService.markAsRead(1L, 2L))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("not authorized");
    }

    @Test
    @DisplayName("markAllAsRead - Calls repository batch update")
    void markAllAsRead_Success() {
        when(userRepository.existsById(1L)).thenReturn(true);

        notificationService.markAllAsRead(1L);

        verify(notificationRepository).markAllAsReadForUser(1L);
    }
}
