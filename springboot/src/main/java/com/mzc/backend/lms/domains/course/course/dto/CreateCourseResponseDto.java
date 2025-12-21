package com.mzc.backend.lms.domains.course.course.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 강의 개설 응답 DTO (API 명세서 11.2 기준)
 */
@Getter
@Builder
public class CreateCourseResponseDto {
    private Long id;  // 강의 ID
    private String courseCode;
    private String courseName;
    private String section;
    private Integer credits;
    private Integer maxStudents;
    private String description;  // 강의 설명
    private String status;  // DRAFT, PUBLISHED, CLOSED
    private Long subjectId;  // 과목 ID
    private Boolean isNewlyCreated;  // 과목이 새로 생성되었는지 여부
    private Long academicTermId;  // 학기 ID
    private List<ScheduleDto> schedules;
    private LocalDateTime createdAt;
}

