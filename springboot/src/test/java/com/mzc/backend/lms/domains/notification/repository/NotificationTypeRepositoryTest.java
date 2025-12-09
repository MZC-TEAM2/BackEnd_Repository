package com.mzc.backend.lms.domains.notification.repository;

import com.mzc.backend.lms.domains.notification.entity.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * NotificationTypeRepository 테스트
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("NotificationTypeRepository 테스트")
class NotificationTypeRepositoryTest {

    @Autowired
    private NotificationTypeRepository notificationTypeRepository;

    private NotificationType assignmentType;
    private NotificationType commentType;

    @BeforeEach
    void setUp() {
        assignmentType = NotificationType.create(
                "ASSIGNMENT_CREATED", "과제 생성", "ASSIGNMENT",
                "{courseName} 강의에 새 과제가 등록되었습니다.");

        commentType = NotificationType.create(
                "COMMENT_ADDED", "댓글 추가", "COMMENT",
                "게시글에 새 댓글이 달렸습니다.");
    }

    @Test
    @DisplayName("알림 타입 저장 및 조회")
    void saveAndFindNotificationType() {
        // Given
        NotificationType saved = notificationTypeRepository.save(assignmentType);

        // When
        Optional<NotificationType> found = notificationTypeRepository.findById(saved.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getTypeCode()).isEqualTo("ASSIGNMENT_CREATED");
        assertThat(found.get().getTypeName()).isEqualTo("과제 생성");
    }

    @Test
    @DisplayName("타입 코드로 알림 타입 조회")
    void findByTypeCode() {
        // Given
        notificationTypeRepository.save(assignmentType);

        // When
        Optional<NotificationType> found = notificationTypeRepository.findByTypeCode("ASSIGNMENT_CREATED");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getCategory()).isEqualTo("ASSIGNMENT");
    }

    @Test
    @DisplayName("타입 코드 존재 여부 확인")
    void existsByTypeCode() {
        // Given
        notificationTypeRepository.save(assignmentType);

        // When & Then
        assertThat(notificationTypeRepository.existsByTypeCode("ASSIGNMENT_CREATED")).isTrue();
        assertThat(notificationTypeRepository.existsByTypeCode("NOT_EXIST")).isFalse();
    }

    @Test
    @DisplayName("카테고리로 알림 타입 목록 조회")
    void findByCategory() {
        // Given
        notificationTypeRepository.save(assignmentType);
        notificationTypeRepository.save(commentType);

        NotificationType assignmentDueType = NotificationType.create(
                "ASSIGNMENT_DUE", "과제 마감 임박", "ASSIGNMENT",
                "과제 마감이 임박했습니다.");
        notificationTypeRepository.save(assignmentDueType);

        // When
        List<NotificationType> assignmentTypes = notificationTypeRepository.findByCategory("ASSIGNMENT");

        // Then
        assertThat(assignmentTypes).hasSize(2);
        assertThat(assignmentTypes).extracting(NotificationType::getCategory)
                .containsOnly("ASSIGNMENT");
    }

    @Test
    @DisplayName("활성화된 알림 타입 목록 조회")
    void findByIsActiveTrue() {
        // Given
        notificationTypeRepository.save(assignmentType);

        NotificationType inactiveType = NotificationType.create(
                "INACTIVE_TYPE", "비활성 타입", "TEST", "비활성 메시지");
        inactiveType.deactivate();
        notificationTypeRepository.save(inactiveType);

        // When
        List<NotificationType> activeTypes = notificationTypeRepository.findByIsActiveTrue();

        // Then
        assertThat(activeTypes).hasSize(1);
        assertThat(activeTypes.get(0).getTypeCode()).isEqualTo("ASSIGNMENT_CREATED");
    }

    @Test
    @DisplayName("카테고리별 활성화된 알림 타입 조회")
    void findActiveByCateogry() {
        // Given
        notificationTypeRepository.save(assignmentType);

        NotificationType inactiveAssignment = NotificationType.create(
                "ASSIGNMENT_INACTIVE", "비활성 과제", "ASSIGNMENT", "비활성");
        inactiveAssignment.deactivate();
        notificationTypeRepository.save(inactiveAssignment);

        notificationTypeRepository.save(commentType);

        // When
        List<NotificationType> activeAssignmentTypes =
                notificationTypeRepository.findActiveByCateogry("ASSIGNMENT");

        // Then
        assertThat(activeAssignmentTypes).hasSize(1);
        assertThat(activeAssignmentTypes.get(0).getTypeCode()).isEqualTo("ASSIGNMENT_CREATED");
    }
}