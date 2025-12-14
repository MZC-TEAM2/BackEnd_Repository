package com.mzc.backend.lms.domains.enrollment.dto;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 내 수강신청 목록 응답 DTO
 */
@Getter
@Builder
public class MyEnrollmentsResponseDto {
    private TermDto term;
    private SummaryDto summary;
    private List<EnrollmentItemDto> enrollments;

    @Getter
    @Builder
    public static class TermDto {
        private Long id;
        private Integer year;
        private String termType;
        private String termName;
    }

    @Getter
    @Builder
    public static class SummaryDto {
        private Integer totalCourses;
        private Integer totalCredits;
        private Integer maxCredits;
        private Integer remainingCredits;
    }

    @Getter
    @Builder
    public static class EnrollmentItemDto {
        private Long enrollmentId;
        private CourseInfoDto course;
        private ProfessorDto professor;
        private List<ScheduleDto> schedule;
        private LocalDateTime enrolledAt;
        private Boolean canCancel;
    }

    @Getter
    @Builder
    public static class CourseInfoDto {
        private Long id;
        private String courseCode;
        private String courseName;
        private String section;
        private Integer credits;
        private CourseTypeDto courseType;
        private Integer currentStudents;  // 수강인원 추가
        private Integer maxStudents;       // 전체 인원 추가
    }
}
