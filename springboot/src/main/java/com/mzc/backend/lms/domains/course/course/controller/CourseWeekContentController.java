package com.mzc.backend.lms.domains.course.course.controller;

import com.mzc.backend.lms.domains.course.course.dto.*;
import com.mzc.backend.lms.domains.course.course.service.CourseWeekContentService;
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
 * 주차별 콘텐츠 관리 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/professor/courses/{courseId}/weeks")
@RequiredArgsConstructor
public class CourseWeekContentController {
	
	private final CourseWeekContentService courseWeekContentService;
	
	/**
	 * 주차 생성
	 */
	@PostMapping
	public ResponseEntity<?> createWeek(
			@PathVariable Long courseId,
			@RequestBody CreateWeekRequestDto request,
			@AuthenticationPrincipal Long professorId) {
		try {
			// 인증 확인
			if (professorId == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(createErrorResponse("로그인이 필요합니다."));
			}
			
			log.debug("주차 생성 요청: courseId={}, weekNumber={}, professorId={}",
					courseId, request.getWeekNumber(), professorId);
			
			WeekDto response = courseWeekContentService.createWeek(courseId, request, professorId);
			return ResponseEntity.status(HttpStatus.CREATED)
					.body(createSuccessResponse(response, "주차가 생성되었습니다"));
		} catch (IllegalArgumentException e) {
			log.warn("주차 생성 실패: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(createErrorResponse(e.getMessage()));
		} catch (Exception e) {
			log.error("주차 생성 실패: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(createErrorResponse(e.getMessage()));
		}
	}
	
	/**
	 * 주차 수정
	 */
	@PutMapping("/{weekId}")
	public ResponseEntity<?> updateWeek(
			@PathVariable Long courseId,
			@PathVariable Long weekId,
			@RequestBody UpdateWeekRequestDto request,
			@AuthenticationPrincipal Long professorId) {
		try {
			// 인증 확인
			if (professorId == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(createErrorResponse("로그인이 필요합니다."));
			}
			
			log.debug("주차 수정 요청: courseId={}, weekId={}, professorId={}", courseId, weekId, professorId);
			
			WeekDto response = courseWeekContentService.updateWeek(courseId, weekId, request, professorId);
			return ResponseEntity.ok(createSuccessResponse(response));
		} catch (IllegalArgumentException e) {
			log.warn("주차 수정 실패: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(createErrorResponse(e.getMessage()));
		} catch (Exception e) {
			log.error("주차 수정 실패: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(createErrorResponse(e.getMessage()));
		}
	}
	
	/**
	 * 주차 삭제
	 */
	@DeleteMapping("/{weekId}")
	public ResponseEntity<?> deleteWeek(
			@PathVariable Long courseId,
			@PathVariable Long weekId,
			@AuthenticationPrincipal Long professorId) {
		try {
			// 인증 확인
			if (professorId == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(createErrorResponse("로그인이 필요합니다."));
			}
			
			log.debug("주차 삭제 요청: courseId={}, weekId={}, professorId={}", courseId, weekId, professorId);
			
			courseWeekContentService.deleteWeek(courseId, weekId, professorId);
			return ResponseEntity.ok(createSuccessResponse(null, "주차가 삭제되었습니다."));
		} catch (IllegalArgumentException e) {
			log.warn("주차 삭제 실패: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(createErrorResponse(e.getMessage()));
		} catch (Exception e) {
			log.error("주차 삭제 실패: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(createErrorResponse(e.getMessage()));
		}
	}
	
	/**
	 * 콘텐츠 등록
	 */
	@PostMapping("/{weekId}/contents")
	public ResponseEntity<?> createContent(
			@PathVariable Long courseId,
			@PathVariable Long weekId,
			@RequestBody CreateWeekContentRequestDto request,
			@AuthenticationPrincipal Long professorId) {
		try {
			// 인증 확인
			if (professorId == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(createErrorResponse("로그인이 필요합니다."));
			}
			
			log.debug("콘텐츠 등록 요청: courseId={}, weekId={}, contentType={}, professorId={}",
					courseId, weekId, request.getContentType(), professorId);
			
			WeekContentDto response = courseWeekContentService.createContent(courseId, weekId, request, professorId);
			return ResponseEntity.status(HttpStatus.CREATED)
					.body(createSuccessResponse(response, "콘텐츠가 추가되었습니다"));
		} catch (IllegalArgumentException e) {
			log.warn("콘텐츠 등록 실패: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(createErrorResponse(e.getMessage()));
		} catch (Exception e) {
			log.error("콘텐츠 등록 실패: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(createErrorResponse(e.getMessage()));
		}
	}
	
	/**
	 * 콘텐츠 수정
	 */
	@PutMapping("/{weekId}/contents/{contentId}")
	public ResponseEntity<?> updateContent(
			@PathVariable Long courseId,
			@PathVariable Long weekId,
			@PathVariable Long contentId,
			@RequestBody UpdateWeekContentRequestDto request,
			@AuthenticationPrincipal Long professorId) {
		try {
			// 인증 확인
			if (professorId == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(createErrorResponse("로그인이 필요합니다."));
			}
			
			log.debug("콘텐츠 수정 요청: courseId={}, weekId={}, contentId={}, professorId={}",
					courseId, weekId, contentId, professorId);
			
			WeekContentDto response = courseWeekContentService.updateContent(
					courseId, weekId, contentId, request, professorId);
			return ResponseEntity.ok(createSuccessResponse(response, "콘텐츠가 수정되었습니다"));
		} catch (IllegalArgumentException e) {
			log.warn("콘텐츠 수정 실패: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(createErrorResponse(e.getMessage()));
		} catch (Exception e) {
			log.error("콘텐츠 수정 실패: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(createErrorResponse(e.getMessage()));
		}
	}
	
	/**
	 * 콘텐츠 삭제
	 */
	@DeleteMapping("/{weekId}/contents/{contentId}")
	public ResponseEntity<?> deleteContent(
			@PathVariable Long courseId,
			@PathVariable Long weekId,
			@PathVariable Long contentId,
			@AuthenticationPrincipal Long professorId) {
		try {
			// 인증 확인
			if (professorId == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(createErrorResponse("로그인이 필요합니다."));
			}
			
			log.debug("콘텐츠 삭제 요청: courseId={}, weekId={}, contentId={}, professorId={}",
					courseId, weekId, contentId, professorId);
			
			courseWeekContentService.deleteContent(courseId, weekId, contentId, professorId);
			return ResponseEntity.ok(createSuccessResponse(null, "콘텐츠가 삭제되었습니다."));
		} catch (IllegalArgumentException e) {
			log.warn("콘텐츠 삭제 실패: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(createErrorResponse(e.getMessage()));
		} catch (Exception e) {
			log.error("콘텐츠 삭제 실패: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(createErrorResponse(e.getMessage()));
		}
	}
	
	/**
	 * 강의 주차 목록 조회 (교수/수강중 학생)
	 */
	@GetMapping
	public ResponseEntity<?> getWeeks(
			@PathVariable Long courseId,
			@AuthenticationPrincipal Long requesterId) {
		try {
			// 인증 확인
			if (requesterId == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(createErrorResponse("로그인이 필요합니다."));
			}
			
			log.debug("강의 주차 목록 조회: courseId={}, requesterId={}", courseId, requesterId);
			
			List<WeekListResponseDto> response = courseWeekContentService.getWeeks(courseId, requesterId);
			return ResponseEntity.ok(createSuccessResponse(response));
		} catch (IllegalArgumentException e) {
			log.warn("강의 주차 목록 조회 실패: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(createErrorResponse(e.getMessage()));
		} catch (Exception e) {
			log.error("강의 주차 목록 조회 실패: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(createErrorResponse(e.getMessage()));
		}
	}
	
	/**
	 * 주차별 콘텐츠 목록 조회
	 */
	@GetMapping("/{weekId}/contents")
	public ResponseEntity<?> getWeekContents(
			@PathVariable Long courseId,
			@PathVariable Long weekId,
			@AuthenticationPrincipal Long requesterId) {
		try {
			// 인증 확인
			if (requesterId == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(createErrorResponse("로그인이 필요합니다."));
			}
			
			log.debug("주차별 콘텐츠 목록 조회: courseId={}, weekId={}, requesterId={}",
					courseId, weekId, requesterId);
			
			WeekContentsResponseDto response = courseWeekContentService.getWeekContents(
					courseId, weekId, requesterId);
			return ResponseEntity.ok(createSuccessResponse(response));
		} catch (IllegalArgumentException e) {
			log.warn("주차별 콘텐츠 목록 조회 실패: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(createErrorResponse(e.getMessage()));
		} catch (Exception e) {
			log.error("주차별 콘텐츠 목록 조회 실패: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(createErrorResponse(e.getMessage()));
		}
	}
	
	/**
	 * 공통 성공 응답
	 */
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

