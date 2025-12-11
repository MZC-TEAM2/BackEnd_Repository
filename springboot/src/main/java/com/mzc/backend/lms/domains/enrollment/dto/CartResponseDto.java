package com.mzc.backend.lms.domains.enrollment.dto;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

/**
 * 장바구니 조회 응답 DTO
 */
@Getter
@Builder
public class CartResponseDto {
    private Integer totalCourses;
    private Integer totalCredits;
    private List<CartItemDto> courses;
}
