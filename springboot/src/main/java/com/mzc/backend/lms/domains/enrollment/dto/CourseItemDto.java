package com.mzc.backend.lms.domains.enrollment.dto;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

/**
 * 강의 항목 DTO
 */
@Getter
@Builder
public class CourseItemDto {
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
    private EnrollmentDto enrollment;
    private Boolean isInCart;
    private Boolean isEnrolled;
    private Boolean canEnroll;
    private List<String> warnings;
}
