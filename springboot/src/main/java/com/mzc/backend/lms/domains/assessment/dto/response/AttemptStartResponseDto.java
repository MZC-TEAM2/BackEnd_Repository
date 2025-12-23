package com.mzc.backend.lms.domains.assessment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class AttemptStartResponseDto {
	
	private Long attemptId;
	private LocalDateTime startedAt;
	private LocalDateTime endAt;
	private Long remainingSeconds;
}


