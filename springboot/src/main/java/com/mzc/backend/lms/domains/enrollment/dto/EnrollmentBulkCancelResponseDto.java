package com.mzc.backend.lms.domains.enrollment.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 수강신청 일괄 취소 응답 DTO
 */
@Getter
@Builder
public class EnrollmentBulkCancelResponseDto {
	private SummaryDto summary;
	private List<CancelledEnrollmentDto> cancelled;
	private List<FailedCancelDto> failed;
	private EnrollmentSummaryDto enrollmentSummary;
	
	@Getter
	@Builder
	public static class SummaryDto {
		private Integer totalAttempted;
		private Integer successCount;
		private Integer failedCount;
	}
	
	@Getter
	@Builder
	public static class CancelledEnrollmentDto {
		private Long enrollmentId;
		private Long courseId;
		private String courseCode;
		private String courseName;
		private Integer credits;
		private LocalDateTime cancelledAt;
	}
	
	@Getter
	@Builder
	public static class FailedCancelDto {
		private Long enrollmentId;
		private Long courseId;
		private String errorCode;
		private String message;
	}
	
	@Getter
	@Builder
	public static class EnrollmentSummaryDto {
		private Integer totalCourses;
		private Integer totalCredits;
	}
}
