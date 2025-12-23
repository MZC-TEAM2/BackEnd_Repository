package com.mzc.backend.lms.domains.assessment.dto.request;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentUpdateRequestDto {
	
	private String title;
	private String content;
	
	private LocalDateTime startAt;
	private Integer durationMinutes;
	private BigDecimal totalScore;
	private Boolean isOnline;
	private String location;
	private String instructions;
	private Integer questionCount;
	private BigDecimal passingScore;
	
	/**
	 * 문제 JSON (정답 포함)
	 * - null이면 유지
	 */
	private JsonNode questionData;
}


