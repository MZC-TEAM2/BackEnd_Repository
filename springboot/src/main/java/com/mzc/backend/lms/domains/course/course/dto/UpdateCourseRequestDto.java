package com.mzc.backend.lms.domains.course.course.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 강의 수정 요청 DTO
 */
@Getter
@Builder
public class UpdateCourseRequestDto {
    private String sectionNumber;
    private Integer maxStudents;
    private List<ScheduleRequestDto> schedules;
}

