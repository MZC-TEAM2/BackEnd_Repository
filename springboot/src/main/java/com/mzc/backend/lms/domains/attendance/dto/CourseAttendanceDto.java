package com.mzc.backend.lms.domains.attendance.dto;

import lombok.*;

import java.util.List;

/**
 * 강의별 출석 현황 상세 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseAttendanceDto {
	
	private Long courseId;
	private String courseName;
	private String sectionNumber;
	private Integer completedWeeks;
	private Integer totalWeeks;
	private Double attendanceRate;
	private List<WeekAttendanceDto> weekAttendances;
}
