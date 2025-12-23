package com.mzc.backend.lms.domains.notification.aop.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * NotificationEventType 테스트
 */
@DisplayName("NotificationEventType 테스트")
class NotificationEventTypeTest {
	
	@Test
	@DisplayName("과제 생성 이벤트 타입 확인")
	void assignmentCreatedType() {
		// given
		NotificationEventType type = NotificationEventType.ASSIGNMENT_CREATED;
		
		// then
		assertThat(type.getTypeCode()).isEqualTo("ASSIGNMENT_CREATED");
		assertThat(type.getCategory()).isEqualTo("과제");
		assertThat(type.getDefaultMessage()).isEqualTo("새 과제가 등록되었습니다.");
	}
	
	@Test
	@DisplayName("강의 공지 이벤트 타입 확인")
	void courseNoticeCreatedType() {
		// given
		NotificationEventType type = NotificationEventType.COURSE_NOTICE_CREATED;
		
		// then
		assertThat(type.getTypeCode()).isEqualTo("COURSE_NOTICE_CREATED");
		assertThat(type.getCategory()).isEqualTo("공지");
		assertThat(type.getDefaultMessage()).isEqualTo("새 공지사항이 등록되었습니다.");
	}
	
	@Test
	@DisplayName("성적 업데이트 이벤트 타입 확인")
	void gradeUpdatedType() {
		// given
		NotificationEventType type = NotificationEventType.GRADE_UPDATED;
		
		// then
		assertThat(type.getTypeCode()).isEqualTo("GRADE_UPDATED");
		assertThat(type.getCategory()).isEqualTo("성적");
		assertThat(type.getDefaultMessage()).isEqualTo("성적이 업데이트되었습니다.");
	}
	
	@Test
	@DisplayName("타입 코드로 이벤트 타입 조회 성공")
	void fromTypeCodeSuccess() {
		// when
		NotificationEventType type = NotificationEventType.fromTypeCode("ASSIGNMENT_CREATED");
		
		// then
		assertThat(type).isEqualTo(NotificationEventType.ASSIGNMENT_CREATED);
	}
	
	@Test
	@DisplayName("존재하지 않는 타입 코드로 조회 시 예외 발생")
	void fromTypeCodeNotFound() {
		// when & then
		assertThatThrownBy(() -> NotificationEventType.fromTypeCode("INVALID_CODE"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("알 수 없는 알림 이벤트 타입");
	}
	
	@Test
	@DisplayName("모든 이벤트 타입은 고유한 타입 코드를 가짐")
	void allTypeCodesAreUnique() {
		// given
		NotificationEventType[] types = NotificationEventType.values();
		
		// when
		long uniqueCount = java.util.Arrays.stream(types)
				.map(NotificationEventType::getTypeCode)
				.distinct()
				.count();
		
		// then
		assertThat(uniqueCount).isEqualTo(types.length);
	}
	
	@Test
	@DisplayName("모든 이벤트 타입은 카테고리를 가짐")
	void allTypesHaveCategory() {
		// given
		NotificationEventType[] types = NotificationEventType.values();
		
		// then
		for (NotificationEventType type : types) {
			assertThat(type.getCategory()).isNotNull();
			assertThat(type.getCategory()).isNotEmpty();
		}
	}
	
	@Test
	@DisplayName("모든 이벤트 타입은 기본 메시지를 가짐")
	void allTypesHaveDefaultMessage() {
		// given
		NotificationEventType[] types = NotificationEventType.values();
		
		// then
		for (NotificationEventType type : types) {
			assertThat(type.getDefaultMessage()).isNotNull();
			assertThat(type.getDefaultMessage()).isNotEmpty();
		}
	}
}
