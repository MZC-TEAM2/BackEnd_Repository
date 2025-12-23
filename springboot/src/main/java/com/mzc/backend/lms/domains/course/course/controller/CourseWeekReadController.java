package com.mzc.backend.lms.domains.course.course.controller;

import com.mzc.backend.lms.domains.course.course.dto.WeekContentsResponseDto;
import com.mzc.backend.lms.domains.course.course.dto.WeekListResponseDto;
import com.mzc.backend.lms.domains.course.course.service.CourseWeekContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 주차 목록 조회 컨트롤러 (학생/교수 공용)
 * - 수강 중 학생 또는 담당 교수만 조회 가능
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
public class CourseWeekReadController {
	
	private final CourseWeekContentService courseWeekContentService;
	
	/**
	 * n주차 목록 조회 (수강중 학생 / 담당 교수)
	 */
	@GetMapping("/{courseId}/weeks")
	public ResponseEntity<?> getWeeks(
			@PathVariable Long courseId,
			Authentication authentication
	) {
		try {
			if (authentication == null || authentication.getPrincipal() == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(createErrorResponse("로그인이 필요합니다."));
			}
			
			Long requesterId = (Long) authentication.getPrincipal();
			List<WeekListResponseDto> response = courseWeekContentService.getWeeks(courseId, requesterId);
			return ResponseEntity.ok(createSuccessResponse(response));
		} catch (IllegalArgumentException e) {
			log.warn("주차 목록 조회 실패: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(createErrorResponse(e.getMessage()));
		} catch (Exception e) {
			log.error("주차 목록 조회 실패: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(createErrorResponse("서버 오류"));
		}
	}
	
	/**
	 * 주차별 콘텐츠 목록 조회 (수강중 학생 / 담당 교수)
	 */
	@GetMapping("/{courseId}/weeks/{weekId}/contents")
	public ResponseEntity<?> getWeekContents(
			@PathVariable Long courseId,
			@PathVariable Long weekId,
			Authentication authentication
	) {
		try {
			if (authentication == null || authentication.getPrincipal() == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(createErrorResponse("로그인이 필요합니다."));
			}
			
			Long requesterId = (Long) authentication.getPrincipal();
			WeekContentsResponseDto response = courseWeekContentService.getWeekContents(courseId, weekId, requesterId);
			return ResponseEntity.ok(createSuccessResponse(response));
		} catch (IllegalArgumentException e) {
			log.warn("주차별 콘텐츠 목록 조회 실패: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(createErrorResponse(e.getMessage()));
		} catch (Exception e) {
			log.error("주차별 콘텐츠 목록 조회 실패: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(createErrorResponse("서버 오류"));
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


