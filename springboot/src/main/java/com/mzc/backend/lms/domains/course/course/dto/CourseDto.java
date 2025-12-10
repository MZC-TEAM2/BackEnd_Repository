package com.mzc.backend.lms.domains.course.course.dto;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

/**
 * 강의 정보 DTO (수강신청 정보 제외)
 */
@Getter
@Builder
public class CourseDto {
    private Long id;
    private String courseCode;
    private String courseName;
    private String section;
    private ProfessorDto professor;
    private DepartmentDto department;
    private Integer credits;
    private CourseTypeDto courseType;
    private List<ScheduleDto> schedule;
    private String scheduleText;
    private EnrollmentInfoDto enrollment;
}
