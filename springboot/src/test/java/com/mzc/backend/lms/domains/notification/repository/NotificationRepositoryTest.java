package com.mzc.backend.lms.domains.notification.repository;

import com.mzc.backend.lms.domains.notification.entity.Notification;
import com.mzc.backend.lms.domains.notification.entity.NotificationType;
import com.mzc.backend.lms.domains.user.user.entity.User;
import com.mzc.backend.lms.domains.user.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * NotificationRepository 테스트
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("NotificationRepository 테스트")
class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationTypeRepository notificationTypeRepository;

    @Autowired
    private UserRepository userRepository;

    private NotificationType notificationType;
    private User sender;
    private User recipient;

    @BeforeEach
    void setUp() {
        notificationType = notificationTypeRepository.save(
                NotificationType.create("ASSIGNMENT_CREATED", "과제 생성", "ASSIGNMENT",
                        "새 과제가 등록되었습니다."));

        sender = userRepository.save(User.create(1001L, "professor@example.com", "password"));
        recipient = userRepository.save(User.create(2001L, "student@example.com", "password"));
    }

    @Test
    @DisplayName("알림 저장 및 조회")
    void saveAndFindNotification() {
        // Given
        Notification notification = Notification.create(
                notificationType, sender, recipient, "테스트 메시지");
        Notification saved = notificationRepository.save(notification);

        // When
        var found = notificationRepository.findById(saved.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getMessage()).isEqualTo("테스트 메시지");
        assertThat(found.get().getIsRead()).isFalse();
    }

    @Test
    @DisplayName("수신자 ID로 알림 목록 조회 (페이징)")
    void findByRecipientIdOrderByCreatedAtDesc() {
        // Given
        for (int i = 0; i < 5; i++) {
            notificationRepository.save(
                    Notification.create(notificationType, sender, recipient, "메시지 " + i));
        }

        // When
        Page<Notification> page = notificationRepository
                .findByRecipientIdOrderByCreatedAtDesc(recipient.getId(), PageRequest.of(0, 3));

        // Then
        assertThat(page.getContent()).hasSize(3);
        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getTotalPages()).isEqualTo(2);
    }

    @Test
    @DisplayName("읽지 않은 알림 목록 조회")
    void findUnreadByRecipientId() {
        // Given
        Notification unread1 = notificationRepository.save(
                Notification.create(notificationType, sender, recipient, "읽지 않은 메시지 1"));
        Notification unread2 = notificationRepository.save(
                Notification.create(notificationType, sender, recipient, "읽지 않은 메시지 2"));

        Notification read = notificationRepository.save(
                Notification.create(notificationType, sender, recipient, "읽은 메시지"));
        read.markAsRead();
        notificationRepository.save(read);

        // When
        List<Notification> unreadList = notificationRepository.findUnreadByRecipientId(recipient.getId());

        // Then
        assertThat(unreadList).hasSize(2);
        assertThat(unreadList).extracting(Notification::getIsRead).containsOnly(false);
    }

    @Test
    @DisplayName("읽지 않은 알림 개수 조회")
    void countUnreadByRecipientId() {
        // Given
        notificationRepository.save(
                Notification.create(notificationType, sender, recipient, "메시지 1"));
        notificationRepository.save(
                Notification.create(notificationType, sender, recipient, "메시지 2"));

        Notification read = notificationRepository.save(
                Notification.create(notificationType, sender, recipient, "읽은 메시지"));
        read.markAsRead();
        notificationRepository.save(read);

        // When
        long unreadCount = notificationRepository.countUnreadByRecipientId(recipient.getId());

        // Then
        assertThat(unreadCount).isEqualTo(2);
    }

    @Test
    @DisplayName("수신자의 모든 알림 읽음 처리 (벌크 업데이트)")
    void markAllAsReadByRecipientId() {
        // Given
        notificationRepository.save(
                Notification.create(notificationType, sender, recipient, "메시지 1"));
        notificationRepository.save(
                Notification.create(notificationType, sender, recipient, "메시지 2"));
        notificationRepository.save(
                Notification.create(notificationType, sender, recipient, "메시지 3"));

        // When
        int updatedCount = notificationRepository.markAllAsReadByRecipientId(recipient.getId());

        // Then
        assertThat(updatedCount).isEqualTo(3);

        long unreadCount = notificationRepository.countUnreadByRecipientId(recipient.getId());
        assertThat(unreadCount).isZero();
    }

    @Test
    @DisplayName("관련 엔티티로 알림 목록 조회")
    void findByRelatedEntityTypeAndRelatedEntityId() {
        // Given
        Notification notification = Notification.createWithRelatedEntity(
                notificationType, sender, recipient, "과제 알림", "새 과제가 등록되었습니다.",
                "ASSIGNMENT", 100L, 10L, "/courses/10/assignments/100");
        notificationRepository.save(notification);

        // When
        List<Notification> found = notificationRepository
                .findByRelatedEntityTypeAndRelatedEntityId("ASSIGNMENT", 100L);

        // Then
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getTitle()).isEqualTo("과제 알림");
    }

    @Test
    @DisplayName("읽음 상태로 알림 필터링 조회")
    void findByRecipientIdAndIsReadOrderByCreatedAtDesc() {
        // Given
        notificationRepository.save(
                Notification.create(notificationType, sender, recipient, "읽지 않은 메시지"));

        Notification read = notificationRepository.save(
                Notification.create(notificationType, sender, recipient, "읽은 메시지"));
        read.markAsRead();
        notificationRepository.save(read);

        // When
        Page<Notification> unreadPage = notificationRepository
                .findByRecipientIdAndIsReadOrderByCreatedAtDesc(
                        recipient.getId(), false, PageRequest.of(0, 10));

        // Then
        assertThat(unreadPage.getContent()).hasSize(1);
        assertThat(unreadPage.getContent().get(0).getMessage()).isEqualTo("읽지 않은 메시지");
    }
}