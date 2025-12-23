package com.mzc.backend.lms.domains.enrollment.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 장바구니 일괄 삭제 응답 DTO
 */
@Getter
@Builder
public class CartBulkDeleteResponseDto {
	private Integer removedCount;
	private Integer removedCredits;
	private List<RemovedCourseDto> removedCourses;
	
	@Getter
	@Builder
	public static class RemovedCourseDto {
		private Long cartId;
		private String courseCode;
		private String courseName;
		private Integer credits;
	}
}
