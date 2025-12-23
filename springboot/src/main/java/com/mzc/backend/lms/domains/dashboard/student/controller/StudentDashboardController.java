package com.mzc.backend.lms.domains.dashboard.student.controller;

import com.mzc.backend.lms.domains.dashboard.student.dto.EnrollmentSummaryDto;
import com.mzc.backend.lms.domains.dashboard.student.dto.NoticeDto;
import com.mzc.backend.lms.domains.dashboard.student.dto.PendingAssignmentDto;
import com.mzc.backend.lms.domains.dashboard.student.dto.TodayCourseDto;
import com.mzc.backend.lms.domains.dashboard.student.dto.UpcomingAssessmentDto;
import com.mzc.backend.lms.domains.dashboard.student.service.StudentDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 학생 대시보드 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/dashboard/student")
@RequiredArgsConstructor
public class StudentDashboardController {
	
	private static final int MAX_DAYS = 30;
	
	private final StudentDashboardService studentDashboardService;
	
	/**
	 * 미제출 과제 목록 조회
	 *
	 * @param studentId 학생 ID (JWT에서 추출)
	 * @param days      마감일 기준 일수 (기본값: 7, 최대: 30)
	 * @return 미제출 과제 목록
	 */
	@GetMapping("/pending-assignments")
	public ResponseEntity<?> getPendingAssignments(
			@AuthenticationPrincipal Long studentId,
			@RequestParam(defaultValue = "7") int days) {
		try {
			validateStudentId(studentId);
			int validDays = validateDays(days);
			
			List<PendingAssignmentDto> assignments =
					studentDashboardService.getPendingAssignments(studentId, validDays);
			
			Map<String, Object> response = new HashMap<>();
			response.put("success", true);
			response.put("data", assignments);
			response.put("count", assignments.size());
			
			return ResponseEntity.ok(response);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
		} catch (Exception e) {
			log.error("미제출 과제 목록 조회 실패: {}", e.getMessage(), e);
			return ResponseEntity.internalServerError()
					.body(createErrorResponse("미제출 과제 목록 조회에 실패했습니다."));
		}
	}
	
	/**
	 * 예정된 시험/퀴즈 목록 조회
	 *
	 * @param studentId 학생 ID (JWT에서 추출)
	 * @param days      시험일 기준 일수 (기본값: 7, 최대: 30)
	 * @return 예정된 시험 목록
	 */
	@GetMapping("/upcoming-assessments")
	public ResponseEntity<?> getUpcomingAssessments(
			@AuthenticationPrincipal Long studentId,
			@RequestParam(defaultValue = "7") int days) {
		try {
			validateStudentId(studentId);
			int validDays = validateDays(days);

			List<UpcomingAssessmentDto> assessments =
					studentDashboardService.getUpcomingAssessments(studentId, validDays);

			Map<String, Object> response = new HashMap<>();
			response.put("success", true);
			response.put("data", assessments);
			response.put("count", assessments.size());

			return ResponseEntity.ok(response);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
		} catch (Exception e) {
			log.error("예정된 시험 목록 조회 실패: {}", e.getMessage(), e);
			return ResponseEntity.internalServerError()
					.body(createErrorResponse("예정된 시험 목록 조회에 실패했습니다."));
		}
	}

	/**
	 * 오늘의 강의 목록 조회
	 *
	 * @param studentId 학생 ID (JWT에서 추출)
	 * @return 오늘의 강의 목록
	 */
	@GetMapping("/today-courses")
	public ResponseEntity<?> getTodayCourses(@AuthenticationPrincipal Long studentId) {
		try {
			validateStudentId(studentId);
			
			List<TodayCourseDto> courses = studentDashboardService.getTodayCourses(studentId);
			
			Map<String, Object> response = new HashMap<>();
			response.put("success", true);
			response.put("data", courses);
			response.put("count", courses.size());
			
			return ResponseEntity.ok(response);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
		} catch (Exception e) {
			log.error("오늘의 강의 목록 조회 실패: {}", e.getMessage(), e);
			return ResponseEntity.internalServerError()
					.body(createErrorResponse("오늘의 강의 목록 조회에 실패했습니다."));
		}
	}
	
	/**
	 * 최신 공지사항 목록 조회
	 *
	 * @param limit 조회할 개수 (기본값: 5, 최대: 10)
	 * @return 최신 공지사항 목록
	 */
	@GetMapping("/notices")
	public ResponseEntity<?> getLatestNotices(
			@RequestParam(defaultValue = "5") int limit) {
		try {
			int validLimit = Math.min(Math.max(limit, 1), 10);
			
			List<NoticeDto> notices = studentDashboardService.getLatestNotices(validLimit);
			
			Map<String, Object> response = new HashMap<>();
			response.put("success", true);
			response.put("data", notices);
			response.put("count", notices.size());
			
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			log.error("공지사항 목록 조회 실패: {}", e.getMessage(), e);
			return ResponseEntity.internalServerError()
					.body(createErrorResponse("공지사항 목록 조회에 실패했습니다."));
		}
	}
	
	/**
	 * 수강 현황 요약 조회
	 *
	 * @param studentId 학생 ID (JWT에서 추출)
	 * @return 수강 중인 과목 수와 총 학점
	 */
	@GetMapping("/enrollment-summary")
	public ResponseEntity<?> getEnrollmentSummary(@AuthenticationPrincipal Long studentId) {
		try {
			validateStudentId(studentId);
			
			EnrollmentSummaryDto summary = studentDashboardService.getEnrollmentSummary(studentId);
			
			Map<String, Object> response = new HashMap<>();
			response.put("success", true);
			response.put("data", summary);
			
			return ResponseEntity.ok(response);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
		} catch (Exception e) {
			log.error("수강 현황 요약 조회 실패: {}", e.getMessage(), e);
			return ResponseEntity.internalServerError()
					.body(createErrorResponse("수강 현황 요약 조회에 실패했습니다."));
		}
	}
	
	private void validateStudentId(Long studentId) {
		if (studentId == null) {
			throw new IllegalArgumentException("인증이 필요합니다.");
		}
	}
	
	private int validateDays(int days) {
		return Math.min(Math.max(days, 1), MAX_DAYS);
	}
	
	private Map<String, Object> createErrorResponse(String message) {
		Map<String, Object> response = new HashMap<>();
		response.put("success", false);
		response.put("message", message);
		return response;
	}
}
