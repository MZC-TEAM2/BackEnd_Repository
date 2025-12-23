package com.mzc.backend.lms.domains.notification.entity;

import com.mzc.backend.lms.domains.user.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * NotificationPreference 엔티티 테스트
 */
@DisplayName("NotificationPreference 엔티티 테스트")
class NotificationPreferenceTest {
	
	private User user;
	private NotificationType notificationType;
	
	@BeforeEach
	void setUp() {
		user = User.create(1001L, "user@example.com", "password");
		notificationType = NotificationType.create(
				"ASSIGNMENT_CREATED", "과제 생성", "ASSIGNMENT",
				"새 과제가 등록되었습니다.");
	}
	
	@Test
	@DisplayName("알림 수신 설정 생성 - 기본값")
	void createNotificationPreference() {
		// When
		NotificationPreference preference = NotificationPreference.create(user, notificationType);
		
		// Then
		assertThat(preference.getUser()).isEqualTo(user);
		assertThat(preference.getNotificationType()).isEqualTo(notificationType);
		assertThat(preference.getIsEnabled()).isTrue();
		assertThat(preference.getEmailEnabled()).isFalse();
	}
	
	@Test
	@DisplayName("알림 수신 설정 생성 - 옵션 포함")
	void createNotificationPreferenceWithOptions() {
		// When
		NotificationPreference preference = NotificationPreference.createWithOptions(
				user, notificationType, true, true);
		
		// Then
		assertThat(preference.getIsEnabled()).isTrue();
		assertThat(preference.getEmailEnabled()).isTrue();
	}
	
	@Test
	@DisplayName("알림 수신 비활성화")
	void disableNotification() {
		// Given
		NotificationPreference preference = NotificationPreference.create(user, notificationType);
		
		// When
		preference.disable();
		
		// Then
		assertThat(preference.getIsEnabled()).isFalse();
	}
	
	@Test
	@DisplayName("알림 수신 활성화")
	void enableNotification() {
		// Given
		NotificationPreference preference = NotificationPreference.create(user, notificationType);
		preference.disable();
		
		// When
		preference.enable();
		
		// Then
		assertThat(preference.getIsEnabled()).isTrue();
	}
	
	@Test
	@DisplayName("이메일 알림 활성화")
	void enableEmail() {
		// Given
		NotificationPreference preference = NotificationPreference.create(user, notificationType);
		
		// When
		preference.enableEmail();
		
		// Then
		assertThat(preference.getEmailEnabled()).isTrue();
	}
	
	@Test
	@DisplayName("이메일 알림 비활성화")
	void disableEmail() {
		// Given
		NotificationPreference preference = NotificationPreference.createWithOptions(
				user, notificationType, true, true);
		
		// When
		preference.disableEmail();
		
		// Then
		assertThat(preference.getEmailEnabled()).isFalse();
	}
	
	@Test
	@DisplayName("알림 설정 업데이트")
	void updatePreference() {
		// Given
		NotificationPreference preference = NotificationPreference.create(user, notificationType);
		
		// When
		preference.updatePreference(false, true);
		
		// Then
		assertThat(preference.getIsEnabled()).isFalse();
		assertThat(preference.getEmailEnabled()).isTrue();
	}
}
