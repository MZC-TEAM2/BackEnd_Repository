package com.mzc.backend.lms.domains.course.subject.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 과목 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectResponse {
	private Long id;
	private String subjectCode;
	private String subjectName;
	private String englishName;  // 영문명은 아직 DB에 없지만 향후 추가 가능
	private Integer credits;
	private CourseTypeDto courseType;
	private DepartmentDto department;
	private String description;
	private List<PrerequisiteDto> prerequisites;
	private Integer currentTermSections;  // 현재 학기 개설 분반 수
	private Boolean isActive;
	private LocalDateTime createdAt;
	
	@Getter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class CourseTypeDto {
		private Long id;
		private String code;
		private String name;
		private String color;
	}
	
	@Getter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class DepartmentDto {
		private Long id;
		private String name;
		private String college;
	}
	
	@Getter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class PrerequisiteDto {
		private Long id;
		private String subjectCode;
		private String subjectName;
		private Integer credits;
		private Boolean isMandatory;
	}
}

