package com.mzc.backend.lms.domains.attendance.dto;

import lombok.*;

/**
 * 학생별 출석 현황 DTO (교수용)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentAttendanceDto {
	
	private Long studentId;
	private String studentName;
	private Integer completedWeeks;
	private Integer totalWeeks;
	private Double attendanceRate;
}
