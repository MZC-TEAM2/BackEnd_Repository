package com.mzc.backend.lms.domains.notification.queue.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 대량 알림 발송을 위한 배치 메시지 DTO
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BatchNotificationMessage implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Long batchId;
	private Integer typeId;
	private Long senderId;
	private List<Long> recipientIds;
	private Long courseId;
	private String relatedEntityType;
	private Long relatedEntityId;
	private String title;
	private String message;
	private String actionUrl;
	private LocalDateTime createdAt;
	
	@Builder
	private BatchNotificationMessage(Long batchId, Integer typeId, Long senderId,
	                                 List<Long> recipientIds, Long courseId,
	                                 String relatedEntityType, Long relatedEntityId,
	                                 String title, String message, String actionUrl) {
		this.batchId = batchId;
		this.typeId = typeId;
		this.senderId = senderId;
		this.recipientIds = recipientIds;
		this.courseId = courseId;
		this.relatedEntityType = relatedEntityType;
		this.relatedEntityId = relatedEntityId;
		this.title = title;
		this.message = message;
		this.actionUrl = actionUrl;
		this.createdAt = LocalDateTime.now();
	}
	
	/**
	 * 강의 관련 배치 알림 생성
	 */
	public static BatchNotificationMessage forCourse(Long batchId, Integer typeId, Long senderId,
	                                                 List<Long> recipientIds, Long courseId,
	                                                 String title, String message) {
		return BatchNotificationMessage.builder()
				.batchId(batchId)
				.typeId(typeId)
				.senderId(senderId)
				.recipientIds(recipientIds)
				.courseId(courseId)
				.title(title)
				.message(message)
				.build();
	}
	
	/**
	 * 수신자 수 반환
	 */
	public int getRecipientCount() {
		return recipientIds != null ? recipientIds.size() : 0;
	}
}
