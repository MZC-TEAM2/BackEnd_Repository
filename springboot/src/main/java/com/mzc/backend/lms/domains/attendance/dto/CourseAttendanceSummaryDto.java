package com.mzc.backend.lms.domains.attendance.dto;

import lombok.*;

/**
 * 강의별 출석 현황 요약 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseAttendanceSummaryDto {

    private Long courseId;
    private String courseName;
    private String sectionNumber;
    private Integer completedWeeks;
    private Integer totalWeeks;
    private Double attendanceRate;
}
