package com.mzc.backend.lms.domains.enrollment.dto;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 수강신청 일괄 응답 DTO
 */
@Getter
@Builder
public class EnrollmentBulkResponseDto {
    private SummaryDto summary;
    private List<SucceededEnrollmentDto> succeeded;
    private List<FailedEnrollmentDto> failed;

    @Getter
    @Builder
    public static class SummaryDto {
        private Integer totalAttempted;
        private Integer successCount;
        private Integer failedCount;
        private Integer enrolledCredits;
        private Integer totalCredits;
    }

    @Getter
    @Builder
    public static class SucceededEnrollmentDto {
        private Long enrollmentId;
        private Long courseId;
        private String courseCode;
        private String courseName;
        private String section;
        private Integer credits;
        private LocalDateTime enrolledAt;
    }

    @Getter
    @Builder
    public static class FailedEnrollmentDto {
        private Long courseId;
        private String courseCode;
        private String courseName;
        private String section;
        private String errorCode;
        private String message;
        // enrollment 필드 제거
    }
}
