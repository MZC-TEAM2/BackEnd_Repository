package com.mzc.backend.lms.domains.notification.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * NotificationType 엔티티 테스트
 */
@DisplayName("NotificationType 엔티티 테스트")
class NotificationTypeTest {
	
	@Test
	@DisplayName("알림 타입 생성 - 팩토리 메소드")
	void createNotificationType() {
		// Given
		String typeCode = "ASSIGNMENT_CREATED";
		String typeName = "과제 생성";
		String category = "ASSIGNMENT";
		String template = "{courseName} 강의에 새 과제 '{title}'가 등록되었습니다.";
		
		// When
		NotificationType notificationType = NotificationType.create(
				typeCode, typeName, category, template);
		
		// Then
		assertThat(notificationType.getTypeCode()).isEqualTo(typeCode);
		assertThat(notificationType.getTypeName()).isEqualTo(typeName);
		assertThat(notificationType.getCategory()).isEqualTo(category);
		assertThat(notificationType.getDefaultMessageTemplate()).isEqualTo(template);
		assertThat(notificationType.getIsActive()).isTrue();
	}
	
	@Test
	@DisplayName("알림 타입 비활성화")
	void deactivateNotificationType() {
		// Given
		NotificationType notificationType = NotificationType.create(
				"TEST_TYPE", "테스트", "TEST", "테스트 메시지");
		
		// When
		notificationType.deactivate();
		
		// Then
		assertThat(notificationType.getIsActive()).isFalse();
	}
	
	@Test
	@DisplayName("알림 타입 활성화")
	void activateNotificationType() {
		// Given
		NotificationType notificationType = NotificationType.create(
				"TEST_TYPE", "테스트", "TEST", "테스트 메시지");
		notificationType.deactivate();
		
		// When
		notificationType.activate();
		
		// Then
		assertThat(notificationType.getIsActive()).isTrue();
	}
	
	@Test
	@DisplayName("메시지 템플릿 변경")
	void changeMessageTemplate() {
		// Given
		NotificationType notificationType = NotificationType.create(
				"TEST_TYPE", "테스트", "TEST", "기존 템플릿");
		String newTemplate = "새로운 템플릿: {title}";
		
		// When
		notificationType.changeMessageTemplate(newTemplate);
		
		// Then
		assertThat(notificationType.getDefaultMessageTemplate()).isEqualTo(newTemplate);
	}
}
