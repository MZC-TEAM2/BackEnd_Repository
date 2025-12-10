package com.mzc.backend.lms.domains.enrollment.controller;

import com.mzc.backend.lms.domains.enrollment.dto.CourseListResponseDto;
import com.mzc.backend.lms.domains.enrollment.dto.CourseSearchRequestDto;
import com.mzc.backend.lms.domains.enrollment.service.EnrollmentCourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 수강신청 강의 목록 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/enrollment/courses")
@RequiredArgsConstructor
public class EnrollmentCourseController {

    private final EnrollmentCourseService enrollmentCourseService;

    /**
     * 강의 목록 조회 (검색 및 필터링)
     */
    @GetMapping
    public ResponseEntity<?> searchCourses(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Integer courseType,
            @RequestParam(required = false) Integer credits,
            @RequestParam(required = true) Long termId,
            @RequestParam(required = false) String sort,
            Authentication authentication) {
        try {
            String studentId = authentication != null ? authentication.getName() : null;

            if (studentId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("로그인이 필요합니다."));
            }
            
            // 디버깅: 파라미터 확인
            log.debug("검색 파라미터: keyword={}, departmentId={}, courseType={}, credits={}, termId={}", 
                    keyword, departmentId, courseType, credits, termId);
            
            CourseSearchRequestDto request = CourseSearchRequestDto.builder()
                    .page(page)
                    .size(size)
                    .keyword(keyword)
                    .departmentId(departmentId)
                    .courseType(courseType)
                    .credits(credits)
                    .termId(termId)
                    .sort(sort)
                    .build();

            CourseListResponseDto response = enrollmentCourseService.searchCourses(request, studentId);
            return ResponseEntity.ok(createSuccessResponse(response));
        } catch (Exception e) {
            log.error("강의 목록 조회 실패: {}", e.getMessage(), e);
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
