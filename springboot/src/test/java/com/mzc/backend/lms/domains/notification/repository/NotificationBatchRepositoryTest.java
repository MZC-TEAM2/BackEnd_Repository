package com.mzc.backend.lms.domains.notification.repository;

import com.mzc.backend.lms.domains.notification.entity.NotificationBatch;
import com.mzc.backend.lms.domains.notification.entity.NotificationBatch.BatchStatus;
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
 * NotificationBatchRepository 테스트
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("NotificationBatchRepository 테스트")
class NotificationBatchRepositoryTest {

    @Autowired
    private NotificationBatchRepository batchRepository;

    @Autowired
    private NotificationTypeRepository notificationTypeRepository;

    @Autowired
    private UserRepository userRepository;

    private NotificationType notificationType;
    private User sender;

    @BeforeEach
    void setUp() {
        notificationType = notificationTypeRepository.save(
                NotificationType.create("ASSIGNMENT_CREATED", "과제 생성", "ASSIGNMENT",
                        "새 과제가 등록되었습니다."));

        sender = userRepository.save(User.create(1001L, "professor@example.com", "password"));
    }

    @Test
    @DisplayName("배치 알림 저장 및 조회")
    void saveAndFindBatch() {
        // Given
        NotificationBatch batch = NotificationBatch.create(
                notificationType, sender, 10L, "과제 알림", "새 과제가 등록되었습니다.", 50);
        NotificationBatch saved = batchRepository.save(batch);

        // When
        var found = batchRepository.findById(saved.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("과제 알림");
        assertThat(found.get().getStatus()).isEqualTo(BatchStatus.PENDING);
    }

    @Test
    @DisplayName("상태로 배치 목록 조회")
    void findByStatus() {
        // Given
        NotificationBatch pending = batchRepository.save(
                NotificationBatch.create(notificationType, sender, 10L, "대기중", "메시지", 50));

        NotificationBatch processing = batchRepository.save(
                NotificationBatch.create(notificationType, sender, 11L, "처리중", "메시지", 30));
        processing.startProcessing();
        batchRepository.save(processing);

        // When
        List<NotificationBatch> pendingBatches = batchRepository.findByStatus(BatchStatus.PENDING);
        List<NotificationBatch> processingBatches = batchRepository.findByStatus(BatchStatus.PROCESSING);

        // Then
        assertThat(pendingBatches).hasSize(1);
        assertThat(processingBatches).hasSize(1);
    }

    @Test
    @DisplayName("처리 대기 중인 배치 목록 조회")
    void findPendingBatches() {
        // Given
        batchRepository.save(
                NotificationBatch.create(notificationType, sender, 10L, "대기1", "메시지", 50));
        batchRepository.save(
                NotificationBatch.create(notificationType, sender, 11L, "대기2", "메시지", 30));

        NotificationBatch completed = batchRepository.save(
                NotificationBatch.create(notificationType, sender, 12L, "완료", "메시지", 20));
        completed.startProcessing();
        completed.complete();
        batchRepository.save(completed);

        // When
        List<NotificationBatch> pendingBatches = batchRepository.findPendingBatches();

        // Then
        assertThat(pendingBatches).hasSize(2);
        assertThat(pendingBatches).extracting(NotificationBatch::getStatus)
                .containsOnly(BatchStatus.PENDING);
    }

    @Test
    @DisplayName("강의 ID로 배치 목록 조회")
    void findByCourseIdOrderByCreatedAtDesc() {
        // Given
        batchRepository.save(
                NotificationBatch.create(notificationType, sender, 10L, "강의10 알림1", "메시지", 50));
        batchRepository.save(
                NotificationBatch.create(notificationType, sender, 10L, "강의10 알림2", "메시지", 30));
        batchRepository.save(
                NotificationBatch.create(notificationType, sender, 20L, "강의20 알림", "메시지", 40));

        // When
        Page<NotificationBatch> batches = batchRepository
                .findByCourseIdOrderByCreatedAtDesc(10L, PageRequest.of(0, 10));

        // Then
        assertThat(batches.getContent()).hasSize(2);
        assertThat(batches.getContent()).extracting(NotificationBatch::getCourseId)
                .containsOnly(10L);
    }

    @Test
    @DisplayName("발신자 ID로 배치 목록 조회")
    void findBySenderIdOrderByCreatedAtDesc() {
        // Given
        User sender2 = userRepository.save(User.create(1002L, "professor2@example.com", "password"));

        batchRepository.save(
                NotificationBatch.create(notificationType, sender, 10L, "발신자1 알림", "메시지", 50));
        batchRepository.save(
                NotificationBatch.create(notificationType, sender2, 11L, "발신자2 알림", "메시지", 30));

        // When
        Page<NotificationBatch> batches = batchRepository
                .findBySenderIdOrderByCreatedAtDesc(sender.getId(), PageRequest.of(0, 10));

        // Then
        assertThat(batches.getContent()).hasSize(1);
        assertThat(batches.getContent().get(0).getTitle()).isEqualTo("발신자1 알림");
    }

    @Test
    @DisplayName("특정 상태의 배치 개수 조회")
    void countByStatus() {
        // Given
        batchRepository.save(
                NotificationBatch.create(notificationType, sender, 10L, "대기1", "메시지", 50));
        batchRepository.save(
                NotificationBatch.create(notificationType, sender, 11L, "대기2", "메시지", 30));

        NotificationBatch completed = batchRepository.save(
                NotificationBatch.create(notificationType, sender, 12L, "완료", "메시지", 20));
        completed.startProcessing();
        completed.complete();
        batchRepository.save(completed);

        // When
        long pendingCount = batchRepository.countByStatus(BatchStatus.PENDING);
        long completedCount = batchRepository.countByStatus(BatchStatus.COMPLETED);

        // Then
        assertThat(pendingCount).isEqualTo(2);
        assertThat(completedCount).isEqualTo(1);
    }

    @Test
    @DisplayName("처리 중인 배치 목록 조회")
    void findProcessingBatches() {
        // Given
        batchRepository.save(
                NotificationBatch.create(notificationType, sender, 10L, "대기", "메시지", 50));

        NotificationBatch processing = batchRepository.save(
                NotificationBatch.create(notificationType, sender, 11L, "처리중", "메시지", 30));
        processing.startProcessing();
        batchRepository.save(processing);

        // When
        List<NotificationBatch> processingBatches = batchRepository.findProcessingBatches();

        // Then
        assertThat(processingBatches).hasSize(1);
        assertThat(processingBatches.get(0).getTitle()).isEqualTo("처리중");
    }
}
