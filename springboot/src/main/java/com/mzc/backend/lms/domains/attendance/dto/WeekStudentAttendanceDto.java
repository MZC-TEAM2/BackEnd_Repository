package com.mzc.backend.lms.domains.attendance.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * 주차별 학생 출석 현황 DTO (교수용)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeekStudentAttendanceDto {
	
	private Long studentId;
	private String studentName;
	private Boolean isCompleted;
	private Integer completedVideoCount;
	private Integer totalVideoCount;
	private LocalDateTime completedAt;
}
