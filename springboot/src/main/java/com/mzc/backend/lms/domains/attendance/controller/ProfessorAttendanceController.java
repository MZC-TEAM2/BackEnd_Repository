package com.mzc.backend.lms.domains.attendance.controller;

import com.mzc.backend.lms.domains.attendance.dto.CourseAttendanceOverviewDto;
import com.mzc.backend.lms.domains.attendance.dto.StudentAttendanceDto;
import com.mzc.backend.lms.domains.attendance.dto.WeekStudentAttendanceDto;
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
 * 교수용 출석 관리 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/professor/courses/{courseId}")
@RequiredArgsConstructor
public class ProfessorAttendanceController {

    private final AttendanceService attendanceService;

    /**
     * 강의 전체 출석 현황 조회
     * GET /api/v1/professor/courses/{courseId}/attendance
     */
    @GetMapping("/attendance")
    public ResponseEntity<?> getCourseAttendanceOverview(
            @PathVariable Long courseId,
            @AuthenticationPrincipal Long professorId) {
        try {
            if (professorId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Login required"));
            }

            CourseAttendanceOverviewDto response = attendanceService.getProfessorCourseAttendance(professorId, courseId);
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
     * 학생별 출석 목록 조회
     * GET /api/v1/professor/courses/{courseId}/attendance/students
     */
    @GetMapping("/attendance/students")
    public ResponseEntity<?> getStudentAttendances(
            @PathVariable Long courseId,
            @AuthenticationPrincipal Long professorId) {
        try {
            if (professorId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Login required"));
            }

            List<StudentAttendanceDto> response = attendanceService.getProfessorStudentAttendances(professorId, courseId);
            return ResponseEntity.ok(createSuccessResponse(response));

        } catch (IllegalArgumentException e) {
            log.warn("Failed to get student attendances: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to get student attendances: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 주차별 학생 출석 현황 조회
     * GET /api/v1/professor/courses/{courseId}/weeks/{weekId}/attendance
     */
    @GetMapping("/weeks/{weekId}/attendance")
    public ResponseEntity<?> getWeekStudentAttendances(
            @PathVariable Long courseId,
            @PathVariable Long weekId,
            @AuthenticationPrincipal Long professorId) {
        try {
            if (professorId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Login required"));
            }

            List<WeekStudentAttendanceDto> response = attendanceService.getProfessorWeekAttendances(professorId, courseId, weekId);
            return ResponseEntity.ok(createSuccessResponse(response));

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
