package com.mzc.backend.lms.domains.course.grade.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class ProfessorCourseGradesResponseDto {
	private Long courseId;
	private Long academicTermId;
	private String courseName;
	
	private StudentDto student;
	
	private BigDecimal midtermScore;
	private BigDecimal finalExamScore;
	private BigDecimal quizScore;
	private BigDecimal assignmentScore;
	private BigDecimal attendanceScore;
	private BigDecimal finalScore;
	private String finalGrade;
	private String status; // PENDING/GRADED/PUBLISHED
	private LocalDateTime gradedAt;
	private LocalDateTime publishedAt;
	
	@Getter
	@Builder
	public static class StudentDto {
		private Long id;
		private Long studentNumber;
		private String name;
	}
}


