package com.mzc.backend.lms.domains.course.course.controller;

import com.mzc.backend.lms.domains.course.course.dto.*;
import com.mzc.backend.lms.domains.course.course.service.ProfessorCourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 교수 강의 관리 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/professor/courses")
@RequiredArgsConstructor
public class ProfessorCourseController {

    private final ProfessorCourseService professorCourseService;

    /**
     * 강의 개설
     */
    @PostMapping
    public ResponseEntity<?> createCourse(
            @RequestBody CreateCourseRequestDto request,
            Authentication authentication) {
        try {
            // 인증 확인
            if (authentication == null || authentication.getName() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("로그인이 필요합니다."));
            }

            Long professorId = Long.parseLong(authentication.getName());
            log.debug("강의 개설 요청: professorId={}", professorId);

            CreateCourseResponseDto response = professorCourseService.createCourse(request, professorId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(createSuccessResponse(response));
        } catch (IllegalArgumentException e) {
            log.warn("강의 개설 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("강의 개설 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 강의 수정
     */
    @PutMapping("/{courseId}")
    public ResponseEntity<?> updateCourse(
            @PathVariable Long courseId,
            @RequestBody UpdateCourseRequestDto request,
            Authentication authentication) {
        try {
            // 인증 확인
            if (authentication == null || authentication.getName() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("로그인이 필요합니다."));
            }

            Long professorId = Long.parseLong(authentication.getName());
            log.debug("강의 수정 요청: courseId={}, professorId={}", courseId, professorId);

            CreateCourseResponseDto response = professorCourseService.updateCourse(courseId, request, professorId);
            return ResponseEntity.ok(createSuccessResponse(response));
        } catch (IllegalArgumentException e) {
            log.warn("강의 수정 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("강의 수정 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 강의 취소
     */
    @DeleteMapping("/{courseId}")
    public ResponseEntity<?> cancelCourse(
            @PathVariable Long courseId,
            Authentication authentication) {
        try {
            // 인증 확인
            if (authentication == null || authentication.getName() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("로그인이 필요합니다."));
            }

            Long professorId = Long.parseLong(authentication.getName());
            log.debug("강의 취소 요청: courseId={}, professorId={}", courseId, professorId);

            professorCourseService.cancelCourse(courseId, professorId);
            return ResponseEntity.ok(createSuccessResponse(null, "강의가 취소되었습니다."));
        } catch (IllegalArgumentException e) {
            log.warn("강의 취소 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("강의 취소 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 내가 개설한 강의 목록 조회
     */
    @GetMapping
    public ResponseEntity<?> getMyCourses(
            @RequestParam(required = false) Long academicTermId,
            Authentication authentication) {
        try {
            // 인증 확인
            if (authentication == null || authentication.getName() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("로그인이 필요합니다."));
            }

            Long professorId = Long.parseLong(authentication.getName());
            log.debug("내 강의 목록 조회: professorId={}, academicTermId={}", professorId, academicTermId);

            MyCoursesResponseDto response = professorCourseService.getMyCourses(professorId, academicTermId);
            return ResponseEntity.ok(createSuccessResponse(response));
        } catch (IllegalArgumentException e) {
            log.warn("내 강의 목록 조회 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("내 강의 목록 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 교수 강의 상세 조회
     */
    @GetMapping("/{courseId}")
    public ResponseEntity<?> getCourseDetail(
            @PathVariable Long courseId,
            Authentication authentication) {
        try {
            // 인증 확인
            if (authentication == null || authentication.getName() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("로그인이 필요합니다."));
            }

            Long professorId = Long.parseLong(authentication.getName());
            log.debug("교수 강의 상세 조회: courseId={}, professorId={}", courseId, professorId);

            ProfessorCourseDetailDto response = professorCourseService.getCourseDetail(courseId, professorId);
            return ResponseEntity.ok(createSuccessResponse(response));
        } catch (IllegalArgumentException e) {
            log.warn("교수 강의 상세 조회 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("교수 강의 상세 조회 실패: {}", e.getMessage(), e);
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

    private Map<String, Object> createSuccessResponse(Object data, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);
        response.put("message", message);
        return response;
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return response;
    }
}

