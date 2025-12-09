package com.mzc.backend.lms.domains.notification.service;

import com.mzc.backend.lms.domains.notification.dto.NotificationCursorResponseDto;
import com.mzc.backend.lms.domains.notification.dto.NotificationResponseDto;
import com.mzc.backend.lms.domains.notification.entity.Notification;
import com.mzc.backend.lms.domains.notification.entity.NotificationType;
import com.mzc.backend.lms.domains.notification.repository.NotificationRepository;
import com.mzc.backend.lms.domains.user.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * NotificationServiceImpl 테스트
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("NotificationServiceImpl 테스트")
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    private NotificationServiceImpl notificationService;

    private User mockRecipient;
    private NotificationType mockType;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationServiceImpl(notificationRepository);

        mockRecipient = mock(User.class);
        when(mockRecipient.getId()).thenReturn(100L);

        mockType = mock(NotificationType.class);
        when(mockType.getTypeCode()).thenReturn("TEST_TYPE");
        when(mockType.getTypeName()).thenReturn("테스트 타입");
        when(mockType.getCategory()).thenReturn("테스트");
    }

    @Test
    @DisplayName("커서 기반 알림 목록 조회 - 최초 요청")
    void getNotificationsFirstRequest() {
        // given
        Long userId = 100L;
        List<Notification> notifications = createMockNotifications(3);

        when(notificationRepository.findByRecipientIdOrderByIdDesc(eq(userId), any(Pageable.class)))
                .thenReturn(notifications);
        when(notificationRepository.countUnreadByRecipientId(userId)).thenReturn(2L);

        // when
        NotificationCursorResponseDto response = notificationService.getNotifications(userId, null, 20);

        // then
        assertThat(response.getNotifications()).hasSize(3);
        assertThat(response.getUnreadCount()).isEqualTo(2L);
        assertThat(response.isHasNext()).isFalse();
    }

    @Test
    @DisplayName("커서 기반 알림 목록 조회 - 다음 페이지 요청")
    void getNotificationsWithCursor() {
        // given
        Long userId = 100L;
        Long cursor = 50L;
        List<Notification> notifications = createMockNotifications(3);

        when(notificationRepository.findByRecipientIdAndIdLessThanOrderByIdDesc(
                eq(userId), eq(cursor), any(Pageable.class)))
                .thenReturn(notifications);

        // when
        NotificationCursorResponseDto response = notificationService.getNotifications(userId, cursor, 20);

        // then
        assertThat(response.getNotifications()).hasSize(3);
        assertThat(response.getUnreadCount()).isNull(); // 다음 페이지에서는 조회 안함
        verify(notificationRepository, never()).countUnreadByRecipientId(anyLong());
    }

    @Test
    @DisplayName("읽지 않은 알림 목록 조회 - 최초 요청")
    void getUnreadNotificationsFirstRequest() {
        // given
        Long userId = 100L;
        List<Notification> notifications = createMockNotifications(2);

        when(notificationRepository.findUnreadByRecipientIdOrderByIdDesc(eq(userId), any(Pageable.class)))
                .thenReturn(notifications);
        when(notificationRepository.countUnreadByRecipientId(userId)).thenReturn(2L);

        // when
        NotificationCursorResponseDto response = notificationService.getUnreadNotifications(userId, null, 20);

        // then
        assertThat(response.getNotifications()).hasSize(2);
        assertThat(response.getUnreadCount()).isEqualTo(2L);
    }

    @Test
    @DisplayName("읽지 않은 알림 목록 조회 - 다음 페이지 요청")
    void getUnreadNotificationsWithCursor() {
        // given
        Long userId = 100L;
        Long cursor = 30L;
        List<Notification> notifications = createMockNotifications(2);

        when(notificationRepository.findUnreadByRecipientIdAndIdLessThanOrderByIdDesc(
                eq(userId), eq(cursor), any(Pageable.class)))
                .thenReturn(notifications);

        // when
        NotificationCursorResponseDto response = notificationService.getUnreadNotifications(userId, cursor, 20);

        // then
        assertThat(response.getNotifications()).hasSize(2);
        assertThat(response.getUnreadCount()).isNull();
    }

    @Test
    @DisplayName("알림 상세 조회 성공 - 자동 읽음 처리")
    void getNotificationSuccess() {
        // given
        Long userId = 100L;
        Long notificationId = 1L;
        Notification notification = createMockNotification(notificationId);
        when(notification.isUnread()).thenReturn(true);

        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(notification)).thenReturn(notification);

        // when
        NotificationResponseDto response = notificationService.getNotification(userId, notificationId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(notificationId);
        verify(notification).markAsRead();
        verify(notificationRepository).save(notification);
    }

    @Test
    @DisplayName("알림 상세 조회 - 알림 없음")
    void getNotificationNotFound() {
        // given
        Long userId = 100L;
        Long notificationId = 999L;

        when(notificationRepository.findById(notificationId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> notificationService.getNotification(userId, notificationId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("알림을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("알림 상세 조회 - 권한 없음")
    void getNotificationUnauthorized() {
        // given
        Long userId = 100L;
        Long notificationId = 1L;
        Notification notification = createMockNotification(notificationId);

        // 다른 사용자의 알림
        User otherUser = mock(User.class);
        when(otherUser.getId()).thenReturn(999L);
        when(notification.getRecipient()).thenReturn(otherUser);

        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));

        // when & then
        assertThatThrownBy(() -> notificationService.getNotification(userId, notificationId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("권한이 없습니다");
    }

    @Test
    @DisplayName("알림 읽음 처리 성공")
    void markAsReadSuccess() {
        // given
        Long userId = 100L;
        Long notificationId = 1L;
        Notification notification = createMockNotification(notificationId);

        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(notification)).thenReturn(notification);

        // when
        NotificationResponseDto response = notificationService.markAsRead(userId, notificationId);

        // then
        verify(notification).markAsRead();
        verify(notificationRepository).save(notification);
    }

    @Test
    @DisplayName("모든 알림 읽음 처리")
    void markAllAsRead() {
        // given
        Long userId = 100L;

        when(notificationRepository.markAllAsReadByRecipientId(userId)).thenReturn(5);

        // when
        int updatedCount = notificationService.markAllAsRead(userId);

        // then
        assertThat(updatedCount).isEqualTo(5);
        verify(notificationRepository).markAllAsReadByRecipientId(userId);
    }

    @Test
    @DisplayName("읽지 않은 알림 개수 조회")
    void getUnreadCount() {
        // given
        Long userId = 100L;

        when(notificationRepository.countUnreadByRecipientId(userId)).thenReturn(10L);

        // when
        long count = notificationService.getUnreadCount(userId);

        // then
        assertThat(count).isEqualTo(10L);
    }

    @Test
    @DisplayName("알림 삭제 성공")
    void deleteNotificationSuccess() {
        // given
        Long userId = 100L;
        Long notificationId = 1L;
        Notification notification = createMockNotification(notificationId);

        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));

        // when
        notificationService.deleteNotification(userId, notificationId);

        // then
        verify(notificationRepository).delete(notification);
    }

    @Test
    @DisplayName("알림 삭제 - 권한 없음")
    void deleteNotificationUnauthorized() {
        // given
        Long userId = 100L;
        Long notificationId = 1L;
        Notification notification = createMockNotification(notificationId);

        User otherUser = mock(User.class);
        when(otherUser.getId()).thenReturn(999L);
        when(notification.getRecipient()).thenReturn(otherUser);

        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));

        // when & then
        assertThatThrownBy(() -> notificationService.deleteNotification(userId, notificationId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("권한이 없습니다");

        verify(notificationRepository, never()).delete(any());
    }

    @Test
    @DisplayName("빈 알림 목록 조회")
    void getNotificationsEmpty() {
        // given
        Long userId = 100L;

        when(notificationRepository.findByRecipientIdOrderByIdDesc(eq(userId), any(Pageable.class)))
                .thenReturn(Collections.emptyList());
        when(notificationRepository.countUnreadByRecipientId(userId)).thenReturn(0L);

        // when
        NotificationCursorResponseDto response = notificationService.getNotifications(userId, null, 20);

        // then
        assertThat(response.getNotifications()).isEmpty();
        assertThat(response.isHasNext()).isFalse();
        assertThat(response.getNextCursor()).isNull();
    }

    @Test
    @DisplayName("읽은 알림 일괄 삭제")
    void deleteReadNotifications() {
        // given
        Long userId = 100L;

        when(notificationRepository.deleteReadByRecipientId(userId)).thenReturn(3);

        // when
        int deletedCount = notificationService.deleteReadNotifications(userId);

        // then
        assertThat(deletedCount).isEqualTo(3);
        verify(notificationRepository).deleteReadByRecipientId(userId);
    }

    @Test
    @DisplayName("모든 알림 삭제")
    void deleteAllNotifications() {
        // given
        Long userId = 100L;

        when(notificationRepository.deleteAllByRecipientId(userId)).thenReturn(10);

        // when
        int deletedCount = notificationService.deleteAllNotifications(userId);

        // then
        assertThat(deletedCount).isEqualTo(10);
        verify(notificationRepository).deleteAllByRecipientId(userId);
    }

    @Test
    @DisplayName("읽은 알림이 없는 경우 삭제")
    void deleteReadNotificationsEmpty() {
        // given
        Long userId = 100L;

        when(notificationRepository.deleteReadByRecipientId(userId)).thenReturn(0);

        // when
        int deletedCount = notificationService.deleteReadNotifications(userId);

        // then
        assertThat(deletedCount).isEqualTo(0);
    }

    /**
     * 테스트용 알림 목록 생성
     */
    private List<Notification> createMockNotifications(int count) {
        Notification[] notifications = new Notification[count];
        for (int i = 0; i < count; i++) {
            notifications[i] = createMockNotification((long) (count - i));
        }
        return Arrays.asList(notifications);
    }

    /**
     * 테스트용 알림 생성
     */
    private Notification createMockNotification(Long id) {
        Notification notification = mock(Notification.class);
        when(notification.getId()).thenReturn(id);
        when(notification.getNotificationType()).thenReturn(mockType);
        when(notification.getRecipient()).thenReturn(mockRecipient);
        when(notification.getSender()).thenReturn(null);
        when(notification.getTitle()).thenReturn("테스트 알림");
        when(notification.getMessage()).thenReturn("테스트 메시지");
        when(notification.getIsRead()).thenReturn(false);
        return notification;
    }
}
