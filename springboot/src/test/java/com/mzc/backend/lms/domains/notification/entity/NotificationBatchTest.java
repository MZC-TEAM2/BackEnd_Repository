package com.mzc.backend.lms.domains.notification.entity;

import com.mzc.backend.lms.domains.notification.entity.NotificationBatch.BatchStatus;
import com.mzc.backend.lms.domains.user.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * NotificationBatch 엔티티 테스트
 */
@DisplayName("NotificationBatch 엔티티 테스트")
class NotificationBatchTest {

    private NotificationType notificationType;
    private User sender;

    @BeforeEach
    void setUp() {
        notificationType = NotificationType.create(
                "ASSIGNMENT_CREATED", "과제 생성", "ASSIGNMENT",
                "새 과제가 등록되었습니다.");
        sender = User.create(1001L, "professor@example.com", "password");
    }

    @Test
    @DisplayName("배치 알림 생성")
    void createNotificationBatch() {
        // Given
        Long courseId = 10L;
        String title = "과제 1: 연결 리스트 구현";
        String message = "자료구조 강의에 새 과제가 등록되었습니다.";
        Integer totalRecipients = 50;

        // When
        NotificationBatch batch = NotificationBatch.create(
                notificationType, sender, courseId, title, message, totalRecipients);

        // Then
        assertThat(batch.getNotificationType()).isEqualTo(notificationType);
        assertThat(batch.getSender()).isEqualTo(sender);
        assertThat(batch.getCourseId()).isEqualTo(courseId);
        assertThat(batch.getTitle()).isEqualTo(title);
        assertThat(batch.getMessage()).isEqualTo(message);
        assertThat(batch.getTotalRecipients()).isEqualTo(totalRecipients);
        assertThat(batch.getStatus()).isEqualTo(BatchStatus.PENDING);
        assertThat(batch.getProcessedAt()).isNull();
    }

    @Test
    @DisplayName("배치 처리 시작")
    void startProcessing() {
        // Given
        NotificationBatch batch = NotificationBatch.create(
                notificationType, sender, 10L, "제목", "메시지", 50);

        // When
        batch.startProcessing();

        // Then
        assertThat(batch.getStatus()).isEqualTo(BatchStatus.PROCESSING);
        assertThat(batch.isProcessing()).isTrue();
    }

    @Test
    @DisplayName("배치 처리 완료")
    void completeBatch() {
        // Given
        NotificationBatch batch = NotificationBatch.create(
                notificationType, sender, 10L, "제목", "메시지", 50);
        batch.startProcessing();

        // When
        batch.complete();

        // Then
        assertThat(batch.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(batch.isCompleted()).isTrue();
        assertThat(batch.getProcessedAt()).isNotNull();
    }

    @Test
    @DisplayName("배치 처리 실패")
    void failBatch() {
        // Given
        NotificationBatch batch = NotificationBatch.create(
                notificationType, sender, 10L, "제목", "메시지", 50);
        batch.startProcessing();

        // When
        batch.fail();

        // Then
        assertThat(batch.getStatus()).isEqualTo(BatchStatus.FAILED);
        assertThat(batch.getProcessedAt()).isNotNull();
    }

    @Test
    @DisplayName("배치 상태 확인 메소드")
    void checkBatchStatus() {
        // Given
        NotificationBatch batch = NotificationBatch.create(
                notificationType, sender, 10L, "제목", "메시지", 50);

        // When & Then - PENDING
        assertThat(batch.isPending()).isTrue();
        assertThat(batch.isProcessing()).isFalse();
        assertThat(batch.isCompleted()).isFalse();

        // When & Then - PROCESSING
        batch.startProcessing();
        assertThat(batch.isPending()).isFalse();
        assertThat(batch.isProcessing()).isTrue();
        assertThat(batch.isCompleted()).isFalse();

        // When & Then - COMPLETED
        batch.complete();
        assertThat(batch.isPending()).isFalse();
        assertThat(batch.isProcessing()).isFalse();
        assertThat(batch.isCompleted()).isTrue();
    }
}