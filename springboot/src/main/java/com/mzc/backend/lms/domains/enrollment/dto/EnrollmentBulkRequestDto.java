package com.mzc.backend.lms.domains.enrollment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * 수강신청 일괄 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentBulkRequestDto {
    private List<Long> courseIds;
}
