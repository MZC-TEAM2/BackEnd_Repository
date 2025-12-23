package com.mzc.backend.lms.domains.notification.queue.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * NotificationMessage DTO 테스트
 */
@DisplayName("NotificationMessage DTO 테스트")
class NotificationMessageTest {
	
	@Test
	@DisplayName("단순 알림 메시지 생성")
	void createSimpleMessage() {
		// when
		NotificationMessage message = NotificationMessage.of(1, 100L, 200L, "테스트 메시지");
		
		// then
		assertThat(message.getTypeId()).isEqualTo(1);
		assertThat(message.getSenderId()).isEqualTo(100L);
		assertThat(message.getRecipientId()).isEqualTo(200L);
		assertThat(message.getMessage()).isEqualTo("테스트 메시지");
		assertThat(message.getCreatedAt()).isNotNull();
	}
	
	@Test
	@DisplayName("강의 관련 알림 메시지 생성")
	void createCourseMessage() {
		// when
		NotificationMessage message = NotificationMessage.forCourse(
				1, 100L, 200L, 10L, "과제 알림", "새 과제가 등록되었습니다."
		);
		
		// then
		assertThat(message.getTypeId()).isEqualTo(1);
		assertThat(message.getSenderId()).isEqualTo(100L);
		assertThat(message.getRecipientId()).isEqualTo(200L);
		assertThat(message.getCourseId()).isEqualTo(10L);
		assertThat(message.getTitle()).isEqualTo("과제 알림");
		assertThat(message.getMessage()).isEqualTo("새 과제가 등록되었습니다.");
	}
	
	@Test
	@DisplayName("관련 엔티티 정보가 포함된 알림 메시지 생성")
	void createMessageWithRelatedEntity() {
		// when
		NotificationMessage message = NotificationMessage.withRelatedEntity(
				1, 100L, 200L,
				"ASSIGNMENT", 50L,
				10L, "과제 알림", "새 과제가 등록되었습니다.",
				"/courses/10/assignments/50"
		);
		
		// then
		assertThat(message.getTypeId()).isEqualTo(1);
		assertThat(message.getSenderId()).isEqualTo(100L);
		assertThat(message.getRecipientId()).isEqualTo(200L);
		assertThat(message.getRelatedEntityType()).isEqualTo("ASSIGNMENT");
		assertThat(message.getRelatedEntityId()).isEqualTo(50L);
		assertThat(message.getCourseId()).isEqualTo(10L);
		assertThat(message.getTitle()).isEqualTo("과제 알림");
		assertThat(message.getMessage()).isEqualTo("새 과제가 등록되었습니다.");
		assertThat(message.getActionUrl()).isEqualTo("/courses/10/assignments/50");
	}
	
	@Test
	@DisplayName("Builder를 사용한 메시지 생성")
	void createMessageWithBuilder() {
		// when
		NotificationMessage message = NotificationMessage.builder()
				.typeId(1)
				.senderId(100L)
				.recipientId(200L)
				.courseId(10L)
				.relatedEntityType("NOTICE")
				.relatedEntityId(30L)
				.title("공지사항")
				.message("새 공지사항이 등록되었습니다.")
				.actionUrl("/notices/30")
				.build();
		
		// then
		assertThat(message.getTypeId()).isEqualTo(1);
		assertThat(message.getSenderId()).isEqualTo(100L);
		assertThat(message.getRecipientId()).isEqualTo(200L);
		assertThat(message.getCourseId()).isEqualTo(10L);
		assertThat(message.getRelatedEntityType()).isEqualTo("NOTICE");
		assertThat(message.getRelatedEntityId()).isEqualTo(30L);
		assertThat(message.getTitle()).isEqualTo("공지사항");
		assertThat(message.getMessage()).isEqualTo("새 공지사항이 등록되었습니다.");
		assertThat(message.getActionUrl()).isEqualTo("/notices/30");
		assertThat(message.getCreatedAt()).isNotNull();
	}
}
