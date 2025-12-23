package com.mzc.backend.lms.domains.enrollment.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 수강신청 정보 DTO
 */
@Getter
@Builder
public class EnrollmentDto {
	private Integer current;
	private Integer max;
	private Boolean isFull;
}
