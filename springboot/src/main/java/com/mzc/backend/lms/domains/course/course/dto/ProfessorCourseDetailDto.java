package com.mzc.backend.lms.domains.course.course.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 교수 강의 상세 조회 응답 DTO
 */
@Getter
@Builder
public class ProfessorCourseDetailDto {
    private Long id;
    private String courseCode;
    private String courseName;
    private String section;
    private DepartmentDto department;
    private Integer credits;
    private CourseTypeDto courseType;
    private Integer maxStudents;
    private Integer currentStudents;
    private String description;
    private List<ScheduleDto> schedule;
    private LocalDateTime createdAt;
}

