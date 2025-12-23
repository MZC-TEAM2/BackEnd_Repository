package com.mzc.backend.lms.domains.course.grade.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class StudentGradeResponseDto {
    private Long academicTermId;
    private Long courseId;
    private String courseName;
    private Integer courseCredits;
    private String status; // PUBLISHED

    private BigDecimal midtermScore;
    private BigDecimal finalExamScore;
    private BigDecimal quizScore;
    private BigDecimal assignmentScore;
    private BigDecimal attendanceScore;

    private BigDecimal finalScore;
    private String finalGrade;
    private LocalDateTime publishedAt;
}


