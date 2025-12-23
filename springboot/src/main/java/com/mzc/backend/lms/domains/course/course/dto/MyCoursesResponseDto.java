package com.mzc.backend.lms.domains.course.course.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 교수가 개설한 강의 목록 응답 DTO
 */
@Getter
@Builder
public class MyCoursesResponseDto {
	private AcademicTermDto term;
	private Integer totalCourses;
	private List<CourseDto> courses;
}

