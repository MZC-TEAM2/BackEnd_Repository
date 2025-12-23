package com.mzc.backend.lms.domains.enrollment.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 강의 검색 요청 DTO
 */
@Getter
@Builder
public class CourseSearchRequestDto {
	private Integer page;
	private Integer size;
	private String keyword;
	private Long departmentId;
	private Integer courseType; // MAJOR_REQ, MAJOR_ELEC, GEN_REQ, GEN_ELEC
	private Integer credits;
	private Long enrollmentPeriodId; // 필수 (enrollment_periods.id)
	private String sort; // 기본값: courseCode,asc
}
