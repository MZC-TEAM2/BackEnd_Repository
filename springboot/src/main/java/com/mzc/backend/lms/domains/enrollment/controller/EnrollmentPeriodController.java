package com.mzc.backend.lms.domains.enrollment.controller;

import com.mzc.backend.lms.domains.enrollment.dto.EnrollmentPeriodResponseDto;
import com.mzc.backend.lms.domains.enrollment.service.EnrollmentPeriodService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 수강신청 기간 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/enrollment/periods")
@RequiredArgsConstructor
public class EnrollmentPeriodController {

    private final EnrollmentPeriodService enrollmentPeriodService;

    /**
     * 현재 수강신청 기간 조회
     */
    @GetMapping("/current")
    public ResponseEntity<?> getCurrentEnrollmentPeriod() {
        try {
            EnrollmentPeriodResponseDto response = enrollmentPeriodService.getCurrentEnrollmentPeriod();
            return ResponseEntity.ok(createSuccessResponse(response));
        } catch (Exception e) {
            log.error("수강신청 기간 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse());
        }
    }

    /**
     * 성공 응답 생성
     */
    private Map<String, Object> createSuccessResponse(Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);
        return response;
    }

    /**
     * 에러 응답 생성
     */
    private Map<String, Object> createErrorResponse() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        return response;
    }
}
