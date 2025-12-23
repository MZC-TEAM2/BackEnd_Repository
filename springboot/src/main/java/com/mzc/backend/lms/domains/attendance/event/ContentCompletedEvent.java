package com.mzc.backend.lms.domains.attendance.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 콘텐츠 완료 이벤트 DTO
 * Video Streaming Server에서 Redis Pub/Sub으로 발행하는 이벤트
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContentCompletedEvent {
	
	@JsonProperty("studentId")
	private Long studentId;
	
	@JsonProperty("contentId")
	private Long contentId;
	
	@JsonProperty("weekId")
	private Long weekId;
	
	@JsonProperty("courseId")
	private Long courseId;
	
	@JsonProperty("completedAt")
	private LocalDateTime completedAt;
	
	@Override
	public String toString() {
		return "ContentCompletedEvent{" +
				"studentId=" + studentId +
				", contentId=" + contentId +
				", weekId=" + weekId +
				", courseId=" + courseId +
				", completedAt=" + completedAt +
				'}';
	}
}
