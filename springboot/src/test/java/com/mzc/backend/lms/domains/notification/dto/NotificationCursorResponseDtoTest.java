package com.mzc.backend.lms.domains.notification.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * NotificationCursorResponseDto 테스트
 */
@DisplayName("NotificationCursorResponseDto 테스트")
class NotificationCursorResponseDtoTest {
	
	@Test
	@DisplayName("다음 페이지가 있는 경우 hasNext true 반환")
	void hasNextTrue() {
		// given - 요청 크기보다 1개 더 많은 결과 (size + 1 조회)
		List<NotificationResponseDto> notifications = createNotifications(21);
		int requestedSize = 20;
		
		// when
		NotificationCursorResponseDto response = NotificationCursorResponseDto.of(
				notifications, requestedSize, 5L);
		
		// then
		assertThat(response.isHasNext()).isTrue();
		assertThat(response.getNotifications()).hasSize(20);
		assertThat(response.getNextCursor()).isEqualTo(2L); // 마지막 항목의 ID
	}
	
	@Test
	@DisplayName("다음 페이지가 없는 경우 hasNext false 반환")
	void hasNextFalse() {
		// given - 요청 크기보다 적은 결과
		List<NotificationResponseDto> notifications = createNotifications(15);
		int requestedSize = 20;
		
		// when
		NotificationCursorResponseDto response = NotificationCursorResponseDto.of(
				notifications, requestedSize, 3L);
		
		// then
		assertThat(response.isHasNext()).isFalse();
		assertThat(response.getNotifications()).hasSize(15);
		assertThat(response.getNextCursor()).isNull();
	}
	
	@Test
	@DisplayName("빈 결과 처리")
	void emptyResult() {
		// given
		List<NotificationResponseDto> notifications = new ArrayList<>();
		int requestedSize = 20;
		
		// when
		NotificationCursorResponseDto response = NotificationCursorResponseDto.of(
				notifications, requestedSize, 0L);
		
		// then
		assertThat(response.isHasNext()).isFalse();
		assertThat(response.getNotifications()).isEmpty();
		assertThat(response.getNextCursor()).isNull();
		assertThat(response.getUnreadCount()).isEqualTo(0L);
	}
	
	@Test
	@DisplayName("정확히 요청 크기만큼 있는 경우")
	void exactSize() {
		// given
		List<NotificationResponseDto> notifications = createNotifications(20);
		int requestedSize = 20;
		
		// when
		NotificationCursorResponseDto response = NotificationCursorResponseDto.of(
				notifications, requestedSize, 2L);
		
		// then
		assertThat(response.isHasNext()).isFalse();
		assertThat(response.getNotifications()).hasSize(20);
		assertThat(response.getNextCursor()).isNull();
	}
	
	@Test
	@DisplayName("unreadCount null 허용")
	void unreadCountNull() {
		// given
		List<NotificationResponseDto> notifications = createNotifications(5);
		
		// when
		NotificationCursorResponseDto response = NotificationCursorResponseDto.of(
				notifications, 20, null);
		
		// then
		assertThat(response.getUnreadCount()).isNull();
	}
	
	@Test
	@DisplayName("size 필드 정상 반환")
	void sizeField() {
		// given
		List<NotificationResponseDto> notifications = createNotifications(10);
		int requestedSize = 20;
		
		// when
		NotificationCursorResponseDto response = NotificationCursorResponseDto.of(
				notifications, requestedSize, 5L);
		
		// then
		assertThat(response.getSize()).isEqualTo(20);
	}
	
	/**
	 * 테스트용 알림 DTO 목록 생성
	 */
	private List<NotificationResponseDto> createNotifications(int count) {
		List<NotificationResponseDto> list = new ArrayList<>();
		for (int i = count; i >= 1; i--) {
			list.add(NotificationResponseDto.builder()
					.id((long) i)
					.typeCode("TEST_TYPE")
					.typeName("테스트 타입")
					.category("테스트")
					.title("테스트 알림 " + i)
					.message("테스트 메시지 " + i)
					.isRead(false)
					.createdAt(LocalDateTime.now())
					.build());
		}
		return list;
	}
}
