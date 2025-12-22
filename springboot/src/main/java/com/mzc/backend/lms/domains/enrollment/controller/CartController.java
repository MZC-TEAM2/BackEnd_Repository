package com.mzc.backend.lms.domains.enrollment.controller;

import com.mzc.backend.lms.domains.enrollment.dto.CourseIdsRequestDto;
import com.mzc.backend.lms.domains.enrollment.dto.CartBulkAddResponseDto;
import com.mzc.backend.lms.domains.enrollment.dto.CartBulkDeleteRequestDto;
import com.mzc.backend.lms.domains.enrollment.dto.CartBulkDeleteResponseDto;
import com.mzc.backend.lms.domains.enrollment.dto.CartResponseDto;
import com.mzc.backend.lms.domains.enrollment.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 장바구니 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /**
     * 장바구니 조회
     */
    @GetMapping
    public ResponseEntity<?> getCart(@AuthenticationPrincipal Long studentId) {
        try {
            // 인증 확인
            if (studentId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("로그인이 필요합니다."));
            }

            log.debug("장바구니 조회: studentId={}", studentId);

            CartResponseDto response = cartService.getCart(String.valueOf(studentId));
            return ResponseEntity.ok(createSuccessResponse(response));
        } catch (Exception e) {
            log.error("장바구니 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 장바구니에 강의 요청 항목 일괄 추가
     */
    @PostMapping("/bulk")
    public ResponseEntity<?> addToCartBulk(
            @RequestBody CourseIdsRequestDto request,
            @AuthenticationPrincipal Long studentId) {
        try {
            // 인증 확인
            if (studentId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("로그인이 필요합니다."));
            }

            log.debug("장바구니 요청 항목 일괄 추가: studentId={}, courseIds={}", studentId, request.getCourseIds());

            CartBulkAddResponseDto response = cartService.addToCartBulk(request, String.valueOf(studentId));
            return ResponseEntity.ok(createSuccessResponse(response));
        } catch (IllegalArgumentException e) {
            log.warn("장바구니 요청 항목 일괄 추가 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("장바구니 요청 항목 일괄 추가 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 장바구니에서 강의 요청 항목 일괄 삭제
     */
    @DeleteMapping("/bulk")
    public ResponseEntity<?> deleteFromCartBulk(
            @RequestBody CartBulkDeleteRequestDto request,
            @AuthenticationPrincipal Long studentId) {
        try {
            // 인증 확인
            if (studentId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("로그인이 필요합니다."));
            }

            log.debug("장바구니 요청 항목 일괄 삭제: studentId={}, cartIds={}", studentId, request.getCartIds());

            CartBulkDeleteResponseDto response = cartService.deleteFromCartBulk(request, String.valueOf(studentId));
            return ResponseEntity.ok(createSuccessResponse(response));
        } catch (IllegalArgumentException e) {
            log.warn("장바구니 요청 항목 일괄 삭제 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("장바구니 요청 항목 일괄 삭제 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 장바구니 전체 비우기
     */

    @DeleteMapping
    public ResponseEntity<?> deleteAllCart(
            @AuthenticationPrincipal Long studentId) {
        try {
            // 인증 확인
            if (studentId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("로그인이 필요합니다."));
            }

            log.debug("장바구니 전체 비우기: studentId={}", studentId);

            CartBulkDeleteResponseDto response = cartService.deleteAllCart(String.valueOf(studentId));
            return ResponseEntity.ok(createSuccessResponse(response));
        }
        catch (IllegalArgumentException e) {
            log.warn("장바구니 전체 비우기 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        }
        catch (Exception e) {
            log.error("장바구니 전체 비우기 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse(e.getMessage()));
        }
    }


    private Map<String, Object> createSuccessResponse(Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);
        return response;
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return response;
    }
}
