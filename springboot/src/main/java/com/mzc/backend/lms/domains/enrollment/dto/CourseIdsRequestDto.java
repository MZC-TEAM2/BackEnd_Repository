package com.mzc.backend.lms.domains.enrollment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * 강의 ID 목록 요청 DTO (장바구니 일괄 추가, 수강신청 일괄 등에서 공통 사용)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseIdsRequestDto {
    private List<Long> courseIds;
}
