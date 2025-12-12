package com.mzc.backend.lms.domains.enrollment.dto;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 장바구니 일괄 추가 응답 DTO
 */
@Getter
@Builder
public class CartBulkAddResponseDto {
    private SummaryDto summary;
    private List<SucceededItemDto> succeeded;

    @Getter
    @Builder
    public static class SummaryDto {
        private Integer totalAttempted;
        private Integer successCount;
        private Integer failedCount;
    }

    @Getter
    @Builder
    public static class SucceededItemDto {
        private Long cartId;
        private Long courseId;
        private String courseCode;
        private String courseName;
        private Integer credits;
        private LocalDateTime addedAt;
    }
}
