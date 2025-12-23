package com.mzc.backend.lms.domains.attendance.dto;

import lombok.*;

import java.util.List;

/**
 * 강의 전체 출석 현황 개요 DTO (교수용)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseAttendanceOverviewDto {
	
	private Long courseId;
	private String courseName;
	private String sectionNumber;
	private Integer totalStudents;
	private Integer totalWeeks;
	private Double averageAttendanceRate;
	private List<WeekAttendanceSummaryDto> weekSummaries;
	
	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class WeekAttendanceSummaryDto {
		private Long weekId;
		private Integer weekNumber;
		private String weekTitle;
		private Integer completedStudents;
		private Integer totalStudents;
		private Double completionRate;
	}
}
