package com.mzc.backend.lms.domains.attendance.controller;

import com.mzc.backend.lms.domains.attendance.dto.CourseAttendanceDto;
import com.mzc.backend.lms.domains.attendance.dto.CourseAttendanceSummaryDto;
import com.mzc.backend.lms.domains.attendance.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 학생용 출석 조회 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
public class StudentAttendanceController {

    private final AttendanceService attendanceService;

    /**
     * 내 전체 출석 현황 조회
     * GET /api/v1/attendance/my
     */
    @GetMapping("/my")
    public ResponseEntity<?> getMyAttendance(@AuthenticationPrincipal Long studentId) {
        try {
            if (studentId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Login required"));
            }

            List<CourseAttendanceSummaryDto> response = attendanceService.getStudentAllAttendance(studentId);
            return ResponseEntity.ok(createSuccessResponse(response));

        } catch (IllegalArgumentException e) {
            log.warn("Failed to get attendance: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to get attendance: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 특정 강의 출석 현황 조회
     * GET /api/v1/attendance/courses/{courseId}
     */
    @GetMapping("/courses/{courseId}")
    public ResponseEntity<?> getCourseAttendance(
            @PathVariable Long courseId,
            @AuthenticationPrincipal Long studentId) {
        try {
            if (studentId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Login required"));
            }

            CourseAttendanceDto response = attendanceService.getStudentCourseAttendance(studentId, courseId);
            return ResponseEntity.ok(createSuccessResponse(response));

        } catch (IllegalArgumentException e) {
            log.warn("Failed to get course attendance: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to get course attendance: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 주차별 출석 상세 조회
     * GET /api/v1/attendance/courses/{courseId}/weeks/{weekId}
     */
    @GetMapping("/courses/{courseId}/weeks/{weekId}")
    public ResponseEntity<?> getWeekAttendanceDetail(
            @PathVariable Long courseId,
            @PathVariable Long weekId,
            @AuthenticationPrincipal Long studentId) {
        try {
            if (studentId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Login required"));
            }

            // 강의 출석 조회 후 해당 주차 정보만 필터링
            CourseAttendanceDto courseAttendance = attendanceService.getStudentCourseAttendance(studentId, courseId);
            var weekAttendance = courseAttendance.getWeekAttendances().stream()
                    .filter(w -> w.getWeekId().equals(weekId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Week not found: " + weekId));

            return ResponseEntity.ok(createSuccessResponse(weekAttendance));

        } catch (IllegalArgumentException e) {
            log.warn("Failed to get week attendance: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to get week attendance: {}", e.getMessage(), e);
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
