package com.mzc.backend.lms.domains.course.grade.controller;

import com.mzc.backend.lms.domains.course.grade.service.GradePublishService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 교수용 성적 산출/확정/공개 수동 실행 API
 * <p>
 * 정책:
 * - 관리자 없이, PROFESSOR 권한이면 "아무 교수나" 실행 가능
 * - 단, 성적 공개 기간(GRADE_PUBLISH) 중에만 실행 가능
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/professor/grades")
@RequiredArgsConstructor
public class ProfessorGradePublishController {
	
	private final GradePublishService gradePublishService;
	
	/**
	 * (배치) 성적 공개 기간(GRADE_PUBLISH) 중인 학기의 강의들을 대상으로 공개 로직을 즉시 실행
	 * - 교수 버튼(수동)용
	 */
	@PostMapping("/publish-ended-terms")
	public ResponseEntity<?> publishEndedTerms(Authentication authentication) {
		try {
			// 권한은 SecurityConfig에서 /api/v1/professor/** 로 제한됨
			gradePublishService.publishEndedTerms(LocalDateTime.now());
			return ResponseEntity.ok(success(null, "성적 공개 기간 대상 성적 공개 처리를 실행했습니다."));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(error(e.getMessage()));
		} catch (Exception e) {
			log.error("성적 수동 공개 실행 실패", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error("서버 오류"));
		}
	}
	
	/**
	 * 특정 학기의 성적 공개(수동)
	 */
	@PostMapping("/publish/terms/{academicTermId}")
	public ResponseEntity<?> publishTerm(
			@PathVariable Long academicTermId,
			Authentication authentication
	) {
		try {
			gradePublishService.publishTermIfAllowed(academicTermId, LocalDateTime.now());
			return ResponseEntity.ok(success(null, "성적 공개 처리를 실행했습니다."));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(error(e.getMessage()));
		} catch (Exception e) {
			log.error("성적 수동 공개 실행 실패 academicTermId={}", academicTermId, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error("서버 오류"));
		}
	}
	
	private Map<String, Object> success(Object data, String message) {
		Map<String, Object> response = new HashMap<>();
		response.put("success", true);
		response.put("data", data);
		if (message != null) {
			response.put("message", message);
		}
		return response;
	}
	
	private Map<String, Object> error(String message) {
		Map<String, Object> response = new HashMap<>();
		response.put("success", false);
		response.put("message", message);
		return response;
	}
}


