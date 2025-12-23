package com.mzc.backend.lms.domains.enrollment.controller;

import com.mzc.backend.lms.domains.enrollment.dto.*;
import com.mzc.backend.lms.domains.enrollment.service.EnrollmentCourseService;
import com.mzc.backend.lms.domains.enrollment.service.EnrollmentPeriodService;
import com.mzc.backend.lms.domains.enrollment.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 수강신청 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {
	
	private final EnrollmentCourseService enrollmentCourseService;
	private final EnrollmentPeriodService enrollmentPeriodService;
	private final EnrollmentService enrollmentService;
	
	/**
	 * 현재 활성화된 기간 조회
	 *
	 * @param type 기간 타입 코드 (ENROLLMENT, COURSE_REGISTRATION, ADJUSTMENT, CANCELLATION)
	 *             기본값: ENROLLMENT
	 */
	@GetMapping("/periods/current")
	public ResponseEntity<?> getCurrentPeriod(
			@RequestParam(required = false) String type) {
		try {
			EnrollmentPeriodResponseDto response = enrollmentPeriodService.getCurrentPeriod(type);
			return ResponseEntity.ok(createSuccessResponse(response));
		} catch (IllegalArgumentException e) {
			log.warn("기간 조회 실패: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(createErrorResponse(e.getMessage()));
		} catch (Exception e) {
			log.error("기간 조회 실패: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(createErrorResponse(e.getMessage()));
		}
	}
	
	/**
	 * 강의 목록 조회 (검색 및 필터링)
	 */
	@GetMapping("/courses")
	public ResponseEntity<?> searchCourses(
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer size,
			@RequestParam(required = false) String keyword,
			@RequestParam(required = false) Long departmentId,
			@RequestParam(required = false) Integer courseType,
			@RequestParam(required = false) Integer credits,
			@RequestParam(required = true) Long enrollmentPeriodId,
			@RequestParam(required = false) String sort,
			@AuthenticationPrincipal Long studentId) {
		try {
			if (studentId == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(createErrorResponse("로그인이 필요합니다."));
			}
			
			// 디버깅: 파라미터 확인
			log.debug("검색 파라미터: keyword={}, departmentId={}, courseType={}, credits={}, enrollmentPeriodId={}",
					keyword, departmentId, courseType, credits, enrollmentPeriodId);
			
			CourseSearchRequestDto request = CourseSearchRequestDto.builder()
					.page(page)
					.size(size)
					.keyword(keyword)
					.departmentId(departmentId)
					.courseType(courseType)
					.credits(credits)
					.enrollmentPeriodId(enrollmentPeriodId)
					.sort(sort)
					.build();
			
			CourseListResponseDto response = enrollmentCourseService.searchCourses(request, String.valueOf(studentId));
			return ResponseEntity.ok(createSuccessResponse(response));
		} catch (Exception e) {
			log.error("강의 목록 조회 실패: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(createErrorResponse(e.getMessage()));
		}
	}
	
	/**
	 * 일괄 수강신청
	 */
	@PostMapping("/bulk")
	public ResponseEntity<?> enrollBulk(
			@RequestBody CourseIdsRequestDto request,
			@AuthenticationPrincipal Long studentId) {
		try {
			// 인증 확인
			if (studentId == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(createErrorResponse("로그인이 필요합니다."));
			}
			
			log.debug("일괄 수강신청: studentId={}, courseIds={}", studentId, request.getCourseIds());
			
			EnrollmentBulkResponseDto response = enrollmentService.enrollBulk(request, String.valueOf(studentId));
			
			// 메시지 생성 - totalAttempted 포함
			String message = String.format("%d개 과목 수강신청 완료", response.getSummary().getSuccessCount());
			if (response.getSummary().getFailedCount() > 0) {
				message += String.format(", %d개 과목 실패", response.getSummary().getFailedCount());
			}
			
			Map<String, Object> successResponse = createSuccessResponse(response);
			successResponse.put("message", message);
			return ResponseEntity.ok(successResponse);
		} catch (IllegalArgumentException e) {
			log.warn("일괄 수강신청 실패: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(createErrorResponse(e.getMessage()));
		} catch (Exception e) {
			log.error("일괄 수강신청 실패: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(createErrorResponse(e.getMessage()));
		}
	}
	
	/**
	 * 일괄 수강신청 취소
	 */
	@DeleteMapping("/bulk")
	public ResponseEntity<?> cancelBulk(
			@RequestBody EnrollmentBulkCancelRequestDto request,
			@AuthenticationPrincipal Long studentId) {
		try {
			// 인증 확인
			if (studentId == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(createErrorResponse("로그인이 필요합니다."));
			}
			
			log.debug("일괄 수강신청 취소: studentId={}, enrollmentIds={}", studentId, request.getEnrollmentIds());
			
			EnrollmentBulkCancelResponseDto response = enrollmentService.cancelBulk(request, String.valueOf(studentId));
			
			// 메시지 생성
			String message = String.format("%d개 과목 취소 완료", response.getSummary().getSuccessCount());
			if (response.getSummary().getFailedCount() > 0) {
				message += String.format(", %d개 과목 실패", response.getSummary().getFailedCount());
			}
			
			Map<String, Object> successResponse = createSuccessResponse(response);
			successResponse.put("message", message);
			return ResponseEntity.ok(successResponse);
		} catch (IllegalArgumentException e) {
			log.warn("일괄 수강신청 취소 실패: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(createErrorResponse(e.getMessage()));
		} catch (Exception e) {
			log.error("일괄 수강신청 취소 실패: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(createErrorResponse(e.getMessage()));
		}
	}
	
	/**
	 * 내 수강신청 목록 조회
	 */
	@GetMapping("/my")
	public ResponseEntity<?> getMyEnrollments(
			@RequestParam(required = false) Long enrollmentPeriodId,
			@AuthenticationPrincipal Long studentId) {
		try {
			// 인증 확인
			if (studentId == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(createErrorResponse("로그인이 필요합니다."));
			}
			
			log.debug("내 수강신청 목록 조회: studentId={}, enrollmentPeriodId={}", studentId, enrollmentPeriodId);
			
			MyEnrollmentsResponseDto response = enrollmentService.getMyEnrollments(String.valueOf(studentId), enrollmentPeriodId);
			return ResponseEntity.ok(createSuccessResponse(response));
		} catch (IllegalArgumentException e) {
			log.warn("내 수강신청 목록 조회 실패: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(createErrorResponse(e.getMessage()));
		} catch (Exception e) {
			log.error("내 수강신청 목록 조회 실패: {}", e.getMessage(), e);
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
