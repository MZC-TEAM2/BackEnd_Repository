package com.mzc.backend.lms.domains.notification.repository;

import com.mzc.backend.lms.domains.notification.entity.NotificationPreference;
import com.mzc.backend.lms.domains.notification.entity.NotificationType;
import com.mzc.backend.lms.domains.user.user.entity.User;
import com.mzc.backend.lms.domains.user.user.repository.UserRepository;
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
 * NotificationPreferenceRepository 테스트
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("NotificationPreferenceRepository 테스트")
class NotificationPreferenceRepositoryTest {

    @Autowired
    private NotificationPreferenceRepository preferenceRepository;

    @Autowired
    private NotificationTypeRepository notificationTypeRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;
    private NotificationType assignmentType;
    private NotificationType commentType;

    @BeforeEach
    void setUp() {
        user = userRepository.save(User.create(1001L, "user@example.com", "password"));

        assignmentType = notificationTypeRepository.save(
                NotificationType.create("ASSIGNMENT_CREATED", "과제 생성", "ASSIGNMENT",
                        "새 과제가 등록되었습니다."));

        commentType = notificationTypeRepository.save(
                NotificationType.create("COMMENT_ADDED", "댓글 추가", "COMMENT",
                        "새 댓글이 달렸습니다."));
    }

    @Test
    @DisplayName("알림 수신 설정 저장 및 조회")
    void saveAndFindPreference() {
        // Given
        NotificationPreference preference = NotificationPreference.create(user, assignmentType);
        NotificationPreference saved = preferenceRepository.save(preference);

        // When
        Optional<NotificationPreference> found = preferenceRepository.findById(saved.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getIsEnabled()).isTrue();
        assertThat(found.get().getEmailEnabled()).isFalse();
    }

    @Test
    @DisplayName("사용자 ID로 알림 수신 설정 목록 조회")
    void findByUserId() {
        // Given
        preferenceRepository.save(NotificationPreference.create(user, assignmentType));
        preferenceRepository.save(NotificationPreference.create(user, commentType));

        // When
        List<NotificationPreference> preferences = preferenceRepository.findByUserId(user.getId());

        // Then
        assertThat(preferences).hasSize(2);
    }

    @Test
    @DisplayName("사용자 ID와 알림 타입 ID로 수신 설정 조회")
    void findByUserIdAndNotificationTypeId() {
        // Given
        preferenceRepository.save(NotificationPreference.create(user, assignmentType));

        // When
        Optional<NotificationPreference> found = preferenceRepository
                .findByUserIdAndNotificationTypeId(user.getId(), assignmentType.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getNotificationType().getTypeCode()).isEqualTo("ASSIGNMENT_CREATED");
    }

    @Test
    @DisplayName("사용자의 활성화된 알림 수신 설정 목록 조회")
    void findEnabledByUserId() {
        // Given
        preferenceRepository.save(NotificationPreference.create(user, assignmentType));

        NotificationPreference disabledPref = NotificationPreference.create(user, commentType);
        disabledPref.disable();
        preferenceRepository.save(disabledPref);

        // When
        List<NotificationPreference> enabledPrefs = preferenceRepository.findEnabledByUserId(user.getId());

        // Then
        assertThat(enabledPrefs).hasSize(1);
        assertThat(enabledPrefs.get(0).getNotificationType().getTypeCode()).isEqualTo("ASSIGNMENT_CREATED");
    }

    @Test
    @DisplayName("사용자의 이메일 알림이 활성화된 설정 목록 조회")
    void findEmailEnabledByUserId() {
        // Given
        preferenceRepository.save(NotificationPreference.create(user, assignmentType));

        NotificationPreference emailEnabledPref =
                NotificationPreference.createWithOptions(user, commentType, true, true);
        preferenceRepository.save(emailEnabledPref);

        // When
        List<NotificationPreference> emailEnabledPrefs =
                preferenceRepository.findEmailEnabledByUserId(user.getId());

        // Then
        assertThat(emailEnabledPrefs).hasSize(1);
        assertThat(emailEnabledPrefs.get(0).getNotificationType().getTypeCode()).isEqualTo("COMMENT_ADDED");
    }

    @Test
    @DisplayName("특정 알림 타입에 대해 알림 수신이 활성화된 사용자 ID 목록 조회")
    void findUserIdsWithEnabledNotification() {
        // Given
        User user2 = userRepository.save(User.create(1002L, "user2@example.com", "password"));
        User user3 = userRepository.save(User.create(1003L, "user3@example.com", "password"));

        preferenceRepository.save(NotificationPreference.create(user, assignmentType));
        preferenceRepository.save(NotificationPreference.create(user2, assignmentType));

        NotificationPreference disabledPref = NotificationPreference.create(user3, assignmentType);
        disabledPref.disable();
        preferenceRepository.save(disabledPref);

        // When
        List<Long> userIds = preferenceRepository
                .findUserIdsWithEnabledNotification(assignmentType.getId());

        // Then
        assertThat(userIds).hasSize(2);
        assertThat(userIds).contains(user.getId(), user2.getId());
        assertThat(userIds).doesNotContain(user3.getId());
    }

    @Test
    @DisplayName("사용자 ID와 알림 타입 ID로 존재 여부 확인")
    void existsByUserIdAndNotificationTypeId() {
        // Given
        preferenceRepository.save(NotificationPreference.create(user, assignmentType));

        // When & Then
        assertThat(preferenceRepository.existsByUserIdAndNotificationTypeId(
                user.getId(), assignmentType.getId())).isTrue();
        assertThat(preferenceRepository.existsByUserIdAndNotificationTypeId(
                user.getId(), commentType.getId())).isFalse();
    }
}
