package com.mzc.backend.lms.domains.course.course.controller;

import com.mzc.backend.lms.domains.course.course.dto.CourseResponseDto;
import com.mzc.backend.lms.domains.course.course.dto.CourseSearchRequestDto;
import com.mzc.backend.lms.domains.course.course.dto.CourseDetailDto;
import com.mzc.backend.lms.domains.course.course.service.CourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
/**
 * 강의 목록 컨트롤러 (개설된 강의 조회)
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    /**
     * 강의 목록 조회 (검색 및 필터링)
     * 인증된 사용자만 접근 가능
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
            // 인증 확인
            if (authentication == null || authentication.getName() == null) {
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

            CourseResponseDto response = courseService.searchCourses(request);
            return ResponseEntity.ok(createSuccessResponse(response));
        } catch (Exception e) {
            log.error("강의 목록 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /*
    * 강의 하나에 대한 정보 상세조회
    */

    @GetMapping("/{courseId}")
    public ResponseEntity<?> getSpecificCourseInfo(@PathVariable Long courseId, Authentication authentication) {
        try {
            // 인증 확인
            if (authentication == null || authentication.getName() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("로그인이 필요합니다."));
            }
            
            log.debug("강의 상세 조회: courseId={}", courseId);
            
            CourseDetailDto courseDetail = courseService.getCourseDetailById(courseId);
            return ResponseEntity.ok(createSuccessResponse(courseDetail));
        } catch (IllegalArgumentException e) {
            log.warn("강의 상세 조회 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("강의 상세 조회 실패: {}", e.getMessage(), e);
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
