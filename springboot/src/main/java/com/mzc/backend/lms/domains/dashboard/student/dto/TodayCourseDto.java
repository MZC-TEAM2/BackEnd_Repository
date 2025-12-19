package com.mzc.backend.lms.domains.dashboard.student.dto;

import com.mzc.backend.lms.domains.user.auth.encryption.annotation.Encrypted;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 오늘의 강의 응답 DTO
 * 기존 MyEnrollmentsResponseDto.EnrollmentItemDto와 동일한 구조
 */
@Getter
@Builder
public class TodayCourseDto {

    private Long enrollmentId;
    private CourseInfoDto course;
    private ProfessorDto professor;
    private List<ScheduleDto> schedule;

    @Getter
    @Builder
    public static class CourseInfoDto {
        private Long id;
        private String courseCode;
        private String courseName;
        private String section;
        private Integer credits;
        private CourseTypeDto courseType;
        private Integer currentStudents;
        private Integer maxStudents;
    }

    @Getter
    @Builder
    public static class ProfessorDto {
        private Long id;

        @Encrypted
        private String name;
    }

    @Getter
    @Builder
    public static class ScheduleDto {
        private Integer dayOfWeek;
        private String dayName;
        private String startTime;
        private String endTime;
        private String classroom;
    }

    @Getter
    @Builder
    public static class CourseTypeDto {
        private String code;
        private String name;
        private String color;
    }
}
