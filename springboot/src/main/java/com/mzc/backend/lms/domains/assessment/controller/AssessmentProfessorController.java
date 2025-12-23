package com.mzc.backend.lms.domains.assessment.controller;

import com.mzc.backend.lms.domains.assessment.dto.request.AssessmentCreateRequestDto;
import com.mzc.backend.lms.domains.assessment.dto.request.AssessmentUpdateRequestDto;
import com.mzc.backend.lms.domains.assessment.dto.request.AttemptGradeRequestDto;
import com.mzc.backend.lms.domains.assessment.dto.response.AssessmentDetailResponseDto;
import com.mzc.backend.lms.domains.assessment.dto.response.AssessmentListItemResponseDto;
import com.mzc.backend.lms.domains.assessment.dto.response.AttemptGradeResponseDto;
import com.mzc.backend.lms.domains.assessment.dto.response.ProfessorAttemptDetailResponseDto;
import com.mzc.backend.lms.domains.assessment.dto.response.ProfessorAttemptListItemResponseDto;
import com.mzc.backend.lms.domains.assessment.enums.AssessmentType;
import com.mzc.backend.lms.domains.assessment.service.AssessmentService;
import com.mzc.backend.lms.domains.board.enums.BoardType;
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
 * 교수용 시험/퀴즈 관리 API
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class AssessmentProfessorController {

    private final AssessmentService assessmentService;

    /**
     * 시험/퀴즈 목록 조회 (교수)
     * - 예: GET /api/v1/professor/exams?courseId=101&examType=QUIZ
     * - 교수는 시작 전 포함 전체 조회 가능(미리보기)
     */
    @GetMapping("/api/v1/professor/exams")
    public ResponseEntity<?> list(
            @RequestParam Long courseId,
            @RequestParam AssessmentType examType,
            Authentication authentication
    ) {
        try {
            Long professorId = (Long) authentication.getPrincipal();
            List<AssessmentListItemResponseDto> data = assessmentService.listForProfessor(courseId, examType, professorId.longValue());
            return ResponseEntity.ok(success(data, null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        } catch (Exception e) {
            log.error("교수 시험/퀴즈 목록 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error("서버 오류"));
        }
    }

    /**
     * 시험/퀴즈 상세 조회 (교수)
     * - 예: GET /api/v1/professor/exams/{examId}
     * - 교수는 questionData(정답 포함) 원본 조회 가능
     */
    @GetMapping("/api/v1/professor/exams/{examId}")
    public ResponseEntity<?> detail(
            @PathVariable Long examId,
            Authentication authentication
    ) {
        try {
            Long professorId = (Long) authentication.getPrincipal();
            AssessmentDetailResponseDto data = assessmentService.getDetailForProfessor(examId, professorId.longValue());
            return ResponseEntity.ok(success(data, null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        } catch (Exception e) {
            log.error("교수 시험/퀴즈 상세 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error("서버 오류"));
        }
    }

    /**
     * 응시자/응시 결과 목록 조회 (교수)
     * - 예: GET /api/v1/professor/exams/{examId}/attempts?status=ALL
     * - status(optional): ALL | SUBMITTED | IN_PROGRESS (기본값 ALL)
     */
    @GetMapping("/api/v1/professor/exams/{examId}/attempts")
    public ResponseEntity<?> listAttempts(
            @PathVariable Long examId,
            @RequestParam(required = false, defaultValue = "ALL") String status,
            Authentication authentication
    ) {
        try {
            Long professorId = (Long) authentication.getPrincipal();
            List<ProfessorAttemptListItemResponseDto> data =
                    assessmentService.listAttemptsForProfessor(examId, status, professorId.longValue());
            return ResponseEntity.ok(success(data, null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        } catch (Exception e) {
            log.error("교수 응시 목록 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error("서버 오류"));
        }
    }

    /**
     * 응시 결과 상세 조회(답안 포함) (교수)
     * - 예: GET /api/v1/professor/exams/results/{attemptId}
     */
    @GetMapping("/api/v1/professor/exams/results/{attemptId}")
    public ResponseEntity<?> attemptDetail(
            @PathVariable Long attemptId,
            Authentication authentication
    ) {
        try {
            Long professorId = (Long) authentication.getPrincipal();
            ProfessorAttemptDetailResponseDto data =
                    assessmentService.getAttemptDetailForProfessor(attemptId, professorId.longValue());
            return ResponseEntity.ok(success(data, null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        } catch (Exception e) {
            log.error("교수 응시 상세 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error("서버 오류"));
        }
    }

    /**
     * 시험/퀴즈 등록 (교수)
     * - 예: POST /api/v1/boards/QUIZ/exams
     */
    @PostMapping("/api/v1/boards/{boardType}/exams")
    public ResponseEntity<?> create(
            @PathVariable String boardType,
            @Valid @RequestBody AssessmentCreateRequestDto request,
            Authentication authentication) {
        try {
            Long professorId = (Long) authentication.getPrincipal();
            BoardType bt = BoardType.valueOf(boardType);
            AssessmentDetailResponseDto data = assessmentService.create(bt, request, professorId.longValue());
            return ResponseEntity.status(HttpStatus.CREATED).body(success(data, "생성되었습니다"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        } catch (Exception e) {
            log.error("시험/퀴즈 생성 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error("서버 오류"));
        }
    }

    /**
     * 시험/퀴즈 수정 (교수)
     * - 예: PUT /api/v1/exams/{examId}/edit
     */
    @PutMapping("/api/v1/exams/{examId}/edit")
    public ResponseEntity<?> update(
            @PathVariable Long examId,
            @Valid @RequestBody AssessmentUpdateRequestDto request,
            Authentication authentication) {
        try {
            Long professorId = (Long) authentication.getPrincipal();
            AssessmentDetailResponseDto data = assessmentService.update(examId, request, professorId.longValue());
            return ResponseEntity.ok(success(data, "수정되었습니다"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        } catch (Exception e) {
            log.error("시험/퀴즈 수정 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error("서버 오류"));
        }
    }

    /**
     * 시험/퀴즈 삭제 (교수)
     * - 예: DELETE /api/v1/exams/{examId}/delete
     */
    @DeleteMapping("/api/v1/exams/{examId}/delete")
    public ResponseEntity<?> delete(
            @PathVariable Long examId,
            Authentication authentication) {
        try {
            Long professorId = (Long) authentication.getPrincipal();
            assessmentService.delete(examId, professorId.longValue());
            return ResponseEntity.ok(success(null, "삭제되었습니다"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        } catch (Exception e) {
            log.error("시험/퀴즈 삭제 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error("서버 오류"));
        }
    }

    /**
     * 시험 채점 (교수)
     * - 퀴즈는 자동채점이므로 본 API 대상이 아님
     */
    @PutMapping("/api/v1/exams/results/{attemptId}/grade")
    public ResponseEntity<?> gradeAttempt(
            @PathVariable Long attemptId,
            @Valid @RequestBody AttemptGradeRequestDto request,
            Authentication authentication
    ) {
        try {
            Long professorId = (Long) authentication.getPrincipal();
            AttemptGradeResponseDto data = assessmentService.gradeAttempt(attemptId, request, professorId.longValue());
            return ResponseEntity.ok(success(data, "채점되었습니다"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        } catch (Exception e) {
            log.error("시험 채점 실패", e);
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


