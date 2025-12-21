package com.mzc.backend.lms.domains.enrollment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * 수강신청 일괄 취소 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentBulkCancelRequestDto {
    private List<Long> enrollmentIds;
}
