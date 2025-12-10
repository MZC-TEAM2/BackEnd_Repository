package com.mzc.backend.lms.domains.enrollment.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 수강신청 기간 응답 DTO
 */
@Getter
@Builder
public class EnrollmentPeriodResponseDto {
    private Boolean isActive;
    private CurrentPeriodDto currentPeriod;

    @Getter
    @Builder
    public static class CurrentPeriodDto {
        private Long id;
        private TermDto term;
        private String periodName;
        private LocalDateTime startDatetime;
        private LocalDateTime endDatetime;
        private Integer targetYear;
        private RemainingTimeDto remainingTime;
    }

    @Getter
    @Builder
    public static class TermDto {
        private Integer year;
        private String termType;
        private String termName;
    }

    @Getter
    @Builder
    public static class RemainingTimeDto {
        private Long days;
        private Long hours;
        private Long minutes;
        private Long totalSeconds;
    }
}
