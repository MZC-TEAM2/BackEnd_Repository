package com.mzc.backend.lms.domains.course.notice.controller;

import com.mzc.backend.lms.domains.course.notice.dto.request.CourseNoticeCommentRequest;
import com.mzc.backend.lms.domains.course.notice.dto.request.CourseNoticeCreateRequest;
import com.mzc.backend.lms.domains.course.notice.dto.request.CourseNoticeUpdateRequest;
import com.mzc.backend.lms.domains.course.notice.dto.response.CourseNoticeCommentResponse;
import com.mzc.backend.lms.domains.course.notice.dto.response.CourseNoticeDetailResponse;
import com.mzc.backend.lms.domains.course.notice.dto.response.CourseNoticeResponse;
import com.mzc.backend.lms.domains.course.notice.service.CourseNoticeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 강의 공지사항 컨트롤러
 * API: /api/v1/courses/{courseId}/notices
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/courses/{courseId}/notices")
@RequiredArgsConstructor
public class CourseNoticeController {
	
	private final CourseNoticeService courseNoticeService;
	
	// === 공지 API ===
	
	/**
	 * 공지사항 생성 (담당 교수만)
	 */
	@PostMapping
	public ResponseEntity<?> createNotice(
			@PathVariable Long courseId,
			@Valid @RequestBody CourseNoticeCreateRequest request,
			@AuthenticationPrincipal Long professorId) {
		try {
			if (professorId == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(createErrorResponse("로그인이 필요합니다."));
			}
			
			log.info("공지사항 생성 요청: courseId={}, professorId={}", courseId, professorId);
			
			CourseNoticeResponse response = courseNoticeService.createNotice(courseId, request, professorId);
			return ResponseEntity.status(HttpStatus.CREATED)
					.body(createSuccessResponse(response, "공지사항이 생성되었습니다."));
		} catch (IllegalArgumentException e) {
			log.warn("공지사항 생성 실패: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(createErrorResponse(e.getMessage()));
		} catch (Exception e) {
			log.error("공지사항 생성 실패: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(createErrorResponse("서버 오류가 발생했습니다."));
		}
	}
	
	/**
	 * 공지사항 목록 조회 (수강생 + 담당 교수)
	 */
	@GetMapping
	public ResponseEntity<?> getNotices(
			@PathVariable Long courseId,
			@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
			@AuthenticationPrincipal Long userId) {
		try {
			if (userId == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(createErrorResponse("로그인이 필요합니다."));
			}
			
			log.debug("공지사항 목록 조회: courseId={}, userId={}", courseId, userId);
			
			Page<CourseNoticeResponse> response = courseNoticeService.getNotices(courseId, userId, pageable);
			return ResponseEntity.ok(createSuccessResponse(response));
		} catch (IllegalArgumentException e) {
			log.warn("공지사항 목록 조회 실패: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(createErrorResponse(e.getMessage()));
		} catch (Exception e) {
			log.error("공지사항 목록 조회 실패: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(createErrorResponse("서버 오류가 발생했습니다."));
		}
	}
	
	/**
	 * 공지사항 상세 조회 (수강생 + 담당 교수)
	 */
	@GetMapping("/{noticeId}")
	public ResponseEntity<?> getNotice(
			@PathVariable Long courseId,
			@PathVariable Long noticeId,
			@AuthenticationPrincipal Long userId) {
		try {
			if (userId == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(createErrorResponse("로그인이 필요합니다."));
			}
			
			log.debug("공지사항 상세 조회: courseId={}, noticeId={}, userId={}", courseId, noticeId, userId);
			
			CourseNoticeDetailResponse response = courseNoticeService.getNotice(courseId, noticeId, userId);
			return ResponseEntity.ok(createSuccessResponse(response));
		} catch (IllegalArgumentException e) {
			log.warn("공지사항 상세 조회 실패: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(createErrorResponse(e.getMessage()));
		} catch (Exception e) {
			log.error("공지사항 상세 조회 실패: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(createErrorResponse("서버 오류가 발생했습니다."));
		}
	}
	
	/**
	 * 공지사항 수정 (담당 교수만)
	 */
	@PutMapping("/{noticeId}")
	public ResponseEntity<?> updateNotice(
			@PathVariable Long courseId,
			@PathVariable Long noticeId,
			@Valid @RequestBody CourseNoticeUpdateRequest request,
			@AuthenticationPrincipal Long professorId) {
		try {
			if (professorId == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(createErrorResponse("로그인이 필요합니다."));
			}
			
			log.info("공지사항 수정 요청: courseId={}, noticeId={}, professorId={}", courseId, noticeId, professorId);
			
			CourseNoticeResponse response = courseNoticeService.updateNotice(courseId, noticeId, request, professorId);
			return ResponseEntity.ok(createSuccessResponse(response, "공지사항이 수정되었습니다."));
		} catch (IllegalArgumentException e) {
			log.warn("공지사항 수정 실패: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(createErrorResponse(e.getMessage()));
		} catch (Exception e) {
			log.error("공지사항 수정 실패: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(createErrorResponse("서버 오류가 발생했습니다."));
		}
	}
	
	/**
	 * 공지사항 삭제 (담당 교수만)
	 */
	@DeleteMapping("/{noticeId}")
	public ResponseEntity<?> deleteNotice(
			@PathVariable Long courseId,
			@PathVariable Long noticeId,
			@AuthenticationPrincipal Long professorId) {
		try {
			if (professorId == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(createErrorResponse("로그인이 필요합니다."));
			}
			
			log.info("공지사항 삭제 요청: courseId={}, noticeId={}, professorId={}", courseId, noticeId, professorId);
			
			courseNoticeService.deleteNotice(courseId, noticeId, professorId);
			return ResponseEntity.ok(createSuccessResponse(null, "공지사항이 삭제되었습니다."));
		} catch (IllegalArgumentException e) {
			log.warn("공지사항 삭제 실패: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(createErrorResponse(e.getMessage()));
		} catch (Exception e) {
			log.error("공지사항 삭제 실패: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(createErrorResponse("서버 오류가 발생했습니다."));
		}
	}
	
	// === 댓글 API ===
	
	/**
	 * 댓글 작성 (수강생 + 담당 교수)
	 */
	@PostMapping("/{noticeId}/comments")
	public ResponseEntity<?> createComment(
			@PathVariable Long courseId,
			@PathVariable Long noticeId,
			@Valid @RequestBody CourseNoticeCommentRequest request,
			@AuthenticationPrincipal Long userId) {
		try {
			if (userId == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(createErrorResponse("로그인이 필요합니다."));
			}
			
			log.info("댓글 작성 요청: courseId={}, noticeId={}, userId={}", courseId, noticeId, userId);
			
			CourseNoticeCommentResponse response = courseNoticeService.createComment(courseId, noticeId, request, userId);
			return ResponseEntity.status(HttpStatus.CREATED)
					.body(createSuccessResponse(response, "댓글이 작성되었습니다."));
		} catch (IllegalArgumentException e) {
			log.warn("댓글 작성 실패: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(createErrorResponse(e.getMessage()));
		} catch (Exception e) {
			log.error("댓글 작성 실패: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(createErrorResponse("서버 오류가 발생했습니다."));
		}
	}
	
	/**
	 * 대댓글 작성 (수강생 + 담당 교수)
	 */
	@PostMapping("/{noticeId}/comments/{parentId}/replies")
	public ResponseEntity<?> createReply(
			@PathVariable Long courseId,
			@PathVariable Long noticeId,
			@PathVariable Long parentId,
			@Valid @RequestBody CourseNoticeCommentRequest request,
			@AuthenticationPrincipal Long userId) {
		try {
			if (userId == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(createErrorResponse("로그인이 필요합니다."));
			}
			
			log.info("대댓글 작성 요청: courseId={}, noticeId={}, parentId={}, userId={}",
					courseId, noticeId, parentId, userId);
			
			CourseNoticeCommentResponse response = courseNoticeService.createReply(courseId, noticeId, parentId, request, userId);
			return ResponseEntity.status(HttpStatus.CREATED)
					.body(createSuccessResponse(response, "답글이 작성되었습니다."));
		} catch (IllegalArgumentException e) {
			log.warn("대댓글 작성 실패: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(createErrorResponse(e.getMessage()));
		} catch (Exception e) {
			log.error("대댓글 작성 실패: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(createErrorResponse("서버 오류가 발생했습니다."));
		}
	}
	
	/**
	 * 댓글 수정 (작성자만)
	 */
	@PutMapping("/{noticeId}/comments/{commentId}")
	public ResponseEntity<?> updateComment(
			@PathVariable Long courseId,
			@PathVariable Long noticeId,
			@PathVariable Long commentId,
			@Valid @RequestBody CourseNoticeCommentRequest request,
			@AuthenticationPrincipal Long userId) {
		try {
			if (userId == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(createErrorResponse("로그인이 필요합니다."));
			}
			
			log.info("댓글 수정 요청: courseId={}, noticeId={}, commentId={}, userId={}",
					courseId, noticeId, commentId, userId);
			
			CourseNoticeCommentResponse response = courseNoticeService.updateComment(courseId, noticeId, commentId, request, userId);
			return ResponseEntity.ok(createSuccessResponse(response, "댓글이 수정되었습니다."));
		} catch (IllegalArgumentException e) {
			log.warn("댓글 수정 실패: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(createErrorResponse(e.getMessage()));
		} catch (Exception e) {
			log.error("댓글 수정 실패: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(createErrorResponse("서버 오류가 발생했습니다."));
		}
	}
	
	/**
	 * 댓글 삭제 (작성자만)
	 */
	@DeleteMapping("/{noticeId}/comments/{commentId}")
	public ResponseEntity<?> deleteComment(
			@PathVariable Long courseId,
			@PathVariable Long noticeId,
			@PathVariable Long commentId,
			@AuthenticationPrincipal Long userId) {
		try {
			if (userId == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(createErrorResponse("로그인이 필요합니다."));
			}
			
			log.info("댓글 삭제 요청: courseId={}, noticeId={}, commentId={}, userId={}",
					courseId, noticeId, commentId, userId);
			
			courseNoticeService.deleteComment(courseId, noticeId, commentId, userId);
			return ResponseEntity.ok(createSuccessResponse(null, "댓글이 삭제되었습니다."));
		} catch (IllegalArgumentException e) {
			log.warn("댓글 삭제 실패: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(createErrorResponse(e.getMessage()));
		} catch (Exception e) {
			log.error("댓글 삭제 실패: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(createErrorResponse("서버 오류가 발생했습니다."));
		}
	}
	
	// === Response Helper Methods ===
	
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
