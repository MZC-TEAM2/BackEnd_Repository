package com.mzc.backend.lms.domains.dashboard.student.dto;

import lombok.Getter;

/**
 * 수강 현황 요약 DTO
 * 수강 중인 과목 수와 총 학점
 */
@Getter
public class EnrollmentSummaryDto {

    private final Integer courseCount;
    private final Integer totalCredits;

    public EnrollmentSummaryDto(Number courseCount, Number totalCredits) {
        this.courseCount = courseCount != null ? courseCount.intValue() : 0;
        this.totalCredits = totalCredits != null ? totalCredits.intValue() : 0;
    }
}
