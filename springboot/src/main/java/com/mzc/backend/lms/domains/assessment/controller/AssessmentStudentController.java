package com.mzc.backend.lms.domains.assessment.controller;

import com.mzc.backend.lms.domains.assessment.dto.request.AttemptSubmitRequestDto;
import com.mzc.backend.lms.domains.assessment.dto.response.AssessmentDetailResponseDto;
import com.mzc.backend.lms.domains.assessment.dto.response.AssessmentListItemResponseDto;
import com.mzc.backend.lms.domains.assessment.dto.response.AttemptStartResponseDto;
import com.mzc.backend.lms.domains.assessment.dto.response.AttemptSubmitResponseDto;
import com.mzc.backend.lms.domains.assessment.enums.AssessmentType;
import com.mzc.backend.lms.domains.assessment.service.AssessmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 학생용 시험/퀴즈 조회/응시 API
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class AssessmentStudentController {
	
	private final AssessmentService assessmentService;
	
	/**
	 * 시험/퀴즈 목록 조회 (학생)
	 * - 명세서 호환: GET /api/v1/exams?courseId=...&examType=QUIZ
	 * - 학생에게는 시작시간 전 항목 숨김
	 */
	@GetMapping("/api/v1/exams")
	public ResponseEntity<?> list(
			@RequestParam Long courseId,
			@RequestParam AssessmentType examType,
			Authentication authentication) {
		try {
			Long studentId = (Long) authentication.getPrincipal();
			List<AssessmentListItemResponseDto> data = assessmentService.listForStudent(courseId, examType, studentId.longValue());
			return ResponseEntity.ok(success(data));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(error(e.getMessage()));
		} catch (Exception e) {
			log.error("시험/퀴즈 목록 조회 실패", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error("서버 오류"));
		}
	}
	
	/**
	 * 시험/퀴즈 상세 조회 (학생)
	 * - 학생에게는 정답 마스킹된 questionData 제공
	 */
	@GetMapping("/api/v1/exams/{examId}")
	public ResponseEntity<?> detail(
			@PathVariable Long examId,
			Authentication authentication) {
		try {
			Long studentId = (Long) authentication.getPrincipal();
			AssessmentDetailResponseDto data = assessmentService.getDetailForStudent(examId, studentId.longValue());
			return ResponseEntity.ok(success(data));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(error(e.getMessage()));
		} catch (Exception e) {
			log.error("시험/퀴즈 상세 조회 실패", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error("서버 오류"));
		}
	}
	
	/**
	 * 응시 시작 (학생)
	 * - 예: POST /api/v1/exams/{examId}/start
	 * - QuizAttempt = exam_results 생성/조회
	 */
	@PostMapping("/api/v1/exams/{examId}/start")
	public ResponseEntity<?> start(
			@PathVariable Long examId,
			Authentication authentication) {
		try {
			Long studentId = (Long) authentication.getPrincipal();
			AttemptStartResponseDto data = assessmentService.startAttempt(examId, studentId.longValue());
			return ResponseEntity.ok(success(data));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(error(e.getMessage()));
		} catch (Exception e) {
			log.error("응시 시작 실패", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error("서버 오류"));
		}
	}
	
	/**
	 * 최종 제출 (학생)
	 * - late면 0점 처리
	 */
	@PostMapping("/api/v1/exams/results/{attemptId}/submit")
	public ResponseEntity<?> submit(
			@PathVariable Long attemptId,
			@Valid @RequestBody AttemptSubmitRequestDto request,
			Authentication authentication) {
		try {
			Long studentId = (Long) authentication.getPrincipal();
			AttemptSubmitResponseDto data = assessmentService.submitAttempt(attemptId, request, studentId.longValue());
			return ResponseEntity.ok(success(data));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(error(e.getMessage()));
		} catch (Exception e) {
			log.error("제출 실패", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error("서버 오류"));
		}
	}
	
	private Map<String, Object> success(Object data) {
		Map<String, Object> response = new HashMap<>();
		response.put("success", true);
		response.put("data", data);
		return response;
	}
	
	private Map<String, Object> error(String message) {
		Map<String, Object> response = new HashMap<>();
		response.put("success", false);
		response.put("message", message);
		return response;
	}
}


