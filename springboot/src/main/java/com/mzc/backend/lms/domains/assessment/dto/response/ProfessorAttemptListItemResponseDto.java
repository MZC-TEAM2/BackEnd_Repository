package com.mzc.backend.lms.domains.assessment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ProfessorAttemptListItemResponseDto {
	
	private Long attemptId;
	private Long examId;
	private Long courseId;
	
	private StudentInfo student;
	
	private LocalDateTime startedAt;
	private LocalDateTime submittedAt;
	
	private Boolean isLate;
	private BigDecimal latePenaltyRate;
	
	private BigDecimal score;
	private String feedback;
	
	@Getter
	@Builder
	@AllArgsConstructor
	public static class StudentInfo {
		private Long id;
		private String studentNumber;
		private String name;
	}
}


