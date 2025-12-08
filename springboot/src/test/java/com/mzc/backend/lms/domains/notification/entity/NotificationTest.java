package com.mzc.backend.lms.domains.notification.entity;

import com.mzc.backend.lms.domains.user.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Notification 엔티티 테스트
 */
@DisplayName("Notification 엔티티 테스트")
class NotificationTest {

    private NotificationType notificationType;
    private User sender;
    private User recipient;

    @BeforeEach
    void setUp() {
        notificationType = NotificationType.create(
                "ASSIGNMENT_CREATED", "과제 생성", "ASSIGNMENT",
                "{courseName} 강의에 새 과제가 등록되었습니다.");

        sender = User.create(1001L, "professor@example.com", "password");
        recipient = User.create(2001L, "student@example.com", "password");
    }

    @Test
    @DisplayName("알림 생성 - 기본 팩토리 메소드")
    void createNotification() {
        // Given
        String message = "자료구조 강의에 새 과제가 등록되었습니다.";

        // When
        Notification notification = Notification.create(
                notificationType, sender, recipient, message);

        // Then
        assertThat(notification.getNotificationType()).isEqualTo(notificationType);
        assertThat(notification.getSender()).isEqualTo(sender);
        assertThat(notification.getRecipient()).isEqualTo(recipient);
        assertThat(notification.getMessage()).isEqualTo(message);
        assertThat(notification.getIsRead()).isFalse();
        assertThat(notification.getReadAt()).isNull();
    }

    @Test
    @DisplayName("알림 생성 - 관련 엔티티 정보 포함")
    void createNotificationWithRelatedEntity() {
        // Given
        String title = "과제 1: 연결 리스트 구현";
        String message = "자료구조 강의에 새 과제가 등록되었습니다.";
        String relatedEntityType = "ASSIGNMENT";
        Long relatedEntityId = 100L;
        Long courseId = 10L;
        String actionUrl = "/courses/10/assignments/100";

        // When
        Notification notification = Notification.createWithRelatedEntity(
                notificationType, sender, recipient, title, message,
                relatedEntityType, relatedEntityId, courseId, actionUrl);

        // Then
        assertThat(notification.getTitle()).isEqualTo(title);
        assertThat(notification.getRelatedEntityType()).isEqualTo(relatedEntityType);
        assertThat(notification.getRelatedEntityId()).isEqualTo(relatedEntityId);
        assertThat(notification.getCourseId()).isEqualTo(courseId);
        assertThat(notification.getActionUrl()).isEqualTo(actionUrl);
    }

    @Test
    @DisplayName("알림 읽음 처리")
    void markAsRead() {
        // Given
        Notification notification = Notification.create(
                notificationType, sender, recipient, "테스트 메시지");

        // When
        notification.markAsRead();

        // Then
        assertThat(notification.getIsRead()).isTrue();
        assertThat(notification.getReadAt()).isNotNull();
    }

    @Test
    @DisplayName("이미 읽은 알림 다시 읽음 처리 - readAt 변경 안됨")
    void markAsReadTwice() {
        // Given
        Notification notification = Notification.create(
                notificationType, sender, recipient, "테스트 메시지");
        notification.markAsRead();
        var firstReadAt = notification.getReadAt();

        // When
        notification.markAsRead();

        // Then
        assertThat(notification.getReadAt()).isEqualTo(firstReadAt);
    }

    @Test
    @DisplayName("알림 읽지 않음 처리")
    void markAsUnread() {
        // Given
        Notification notification = Notification.create(
                notificationType, sender, recipient, "테스트 메시지");
        notification.markAsRead();

        // When
        notification.markAsUnread();

        // Then
        assertThat(notification.getIsRead()).isFalse();
        assertThat(notification.getReadAt()).isNull();
    }

    @Test
    @DisplayName("읽지 않은 알림 확인")
    void isUnread() {
        // Given
        Notification notification = Notification.create(
                notificationType, sender, recipient, "테스트 메시지");

        // When & Then
        assertThat(notification.isUnread()).isTrue();

        notification.markAsRead();
        assertThat(notification.isUnread()).isFalse();
    }
}