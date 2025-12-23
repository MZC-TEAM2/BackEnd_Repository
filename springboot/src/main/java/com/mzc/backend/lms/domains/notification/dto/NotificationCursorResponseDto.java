package com.mzc.backend.lms.domains.notification.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 커서 기반 알림 목록 응답 DTO
 * COUNT 쿼리 없이 효율적인 페이징 지원
 */
@Getter
@Builder
public class NotificationCursorResponseDto {
	
	/**
	 * 알림 목록
	 */
	private List<NotificationResponseDto> notifications;
	
	/**
	 * 다음 페이지 커서 (다음 요청 시 사용)
	 * null이면 더 이상 데이터 없음
	 */
	private Long nextCursor;
	
	/**
	 * 다음 페이지 존재 여부
	 */
	private boolean hasNext;
	
	/**
	 * 요청한 페이지 크기
	 */
	private int size;
	
	/**
	 * 읽지 않은 알림 개수 (선택적)
	 */
	private Long unreadCount;
	
	/**
	 * 커서 기반 응답 생성
	 *
	 * @param notifications 알림 목록
	 * @param requestedSize 요청한 크기
	 * @param unreadCount   읽지 않은 개수
	 * @return 커서 응답 DTO
	 */
	public static NotificationCursorResponseDto of(
			List<NotificationResponseDto> notifications,
			int requestedSize,
			Long unreadCount) {
		
		boolean hasNext = notifications.size() > requestedSize;
		
		// 요청한 크기보다 많으면 마지막 하나 제거 (hasNext 확인용으로 1개 더 조회했으므로)
		List<NotificationResponseDto> result = hasNext
				? notifications.subList(0, requestedSize)
				: notifications;
		
		// 다음 커서는 마지막 알림의 ID
		Long nextCursor = hasNext && !result.isEmpty()
				? result.get(result.size() - 1).getId()
				: null;
		
		return NotificationCursorResponseDto.builder()
				.notifications(result)
				.nextCursor(nextCursor)
				.hasNext(hasNext)
				.size(requestedSize)
				.unreadCount(unreadCount)
				.build();
	}
}
