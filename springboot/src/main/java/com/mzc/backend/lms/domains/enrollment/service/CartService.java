package com.mzc.backend.lms.domains.enrollment.service;

import com.mzc.backend.lms.domains.enrollment.dto.CartBulkAddRequestDto;
import com.mzc.backend.lms.domains.enrollment.dto.CartBulkAddResponseDto;
import com.mzc.backend.lms.domains.enrollment.dto.CartBulkDeleteRequestDto;
import com.mzc.backend.lms.domains.enrollment.dto.CartBulkDeleteResponseDto;
import com.mzc.backend.lms.domains.enrollment.dto.CartResponseDto;

/**
 * 장바구니 서비스 인터페이스
 */
public interface CartService {
    /**
     * 학생의 장바구니 조회
     * @param studentId 학생 ID
     * @return 장바구니 응답 DTO
     */
    CartResponseDto getCart(String studentId);

    /**
     * 장바구니에 강의 일괄 추가
     * @param request 요청 DTO
     * @param studentId 학생 ID
     * @return 일괄 추가 응답 DTO
     */
    CartBulkAddResponseDto addToCartBulk(CartBulkAddRequestDto request, String studentId);

    /**
     * 장바구니에서 강의 일괄 삭제
     * @param request 요청 DTO
     * @param studentId 학생 ID
     * @return 일괄 삭제 응답 DTO
     */
    CartBulkDeleteResponseDto deleteFromCartBulk(CartBulkDeleteRequestDto request, String studentId);

    /**
     * 장바구니 전체 비우기
     * @param studentId 학생 ID
     * @return 전체 비우기 응답 DTO
     */
    CartBulkDeleteResponseDto deleteAllCart(String studentId);
}
