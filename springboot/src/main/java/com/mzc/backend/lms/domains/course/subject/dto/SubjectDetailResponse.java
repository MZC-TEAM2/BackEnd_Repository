package com.mzc.backend.lms.domains.course.subject.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 과목 상세 응답 DTO (개설 강좌 정보 포함)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectDetailResponse {
    private Long id;
    private String subjectCode;
    private String subjectName;
    private String englishName;
    private Integer credits;
    private SubjectResponse.CourseTypeDto courseType;
    private SubjectResponse.DepartmentDto department;
    private String description;
    private List<String> objectives;
    private List<SubjectResponse.PrerequisiteDto> prerequisites;
    private List<CourseInfoDto> courses;  // 개설된 강좌들
    private Boolean isActive;
    private LocalDateTime createdAt;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseInfoDto {
        private Long id;
        private String section;
        private ProfessorDto professor;
        private TermDto term;
        private Integer currentStudents;
        private Integer maxStudents;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfessorDto {
        private Long id;
        private String name;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TermDto {
        private Integer year;
        private String termType;
    }
}

