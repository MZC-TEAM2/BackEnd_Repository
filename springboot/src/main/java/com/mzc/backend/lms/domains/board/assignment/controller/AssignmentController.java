package com.mzc.backend.lms.domains.board.assignment.controller;

import com.mzc.backend.lms.domains.board.assignment.dto.request.AssignmentCreateRequestDto;
import com.mzc.backend.lms.domains.board.assignment.dto.request.AssignmentGradeRequestDto;
import com.mzc.backend.lms.domains.board.assignment.dto.request.AssignmentSubmissionRequestDto;
import com.mzc.backend.lms.domains.board.assignment.dto.request.AssignmentUpdateRequestDto;
import com.mzc.backend.lms.domains.board.assignment.dto.response.AssignmentResponseDto;
import com.mzc.backend.lms.domains.board.assignment.dto.response.AssignmentSubmissionResponseDto;
import com.mzc.backend.lms.domains.board.assignment.service.AssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 과제 컨트롤러
 * 
 * 교수: 과제 등록, 수정, 삭제, 제출 목록 조회, 채점
 * 학생: 과제 조회, 제출, 내 제출 조회
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;

    /**
     * 과제 등록 (교수)
     */
    @PostMapping
    public ResponseEntity<AssignmentResponseDto> createAssignment(
            @Valid @RequestBody AssignmentCreateRequestDto request,
            Authentication authentication) {
        Long createdBy = (Long) authentication.getPrincipal();
        log.info("과제 등록 API 호출: courseId={}, title={}, dueDate={}, createdBy={}", 
                request.getCourseId(), request.getTitle(), request.getDueDate(), createdBy);
        AssignmentResponseDto response = assignmentService.createAssignment(request, createdBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 강의별 과제 목록 조회
     */
    @GetMapping
    public ResponseEntity<List<AssignmentResponseDto>> getAssignmentsByCourse(
            @RequestParam Long courseId) {
        log.info("강의별 과제 목록 조회 API 호출: courseId={}", courseId);
        List<AssignmentResponseDto> response = assignmentService.getAssignmentsByCourse(courseId);
        return ResponseEntity.ok(response);
    }

    /**
     * 과제 상세 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<AssignmentResponseDto> getAssignment(@PathVariable Long id) {
        log.info("과제 상세 조회 API 호출: assignmentId={}", id);
        AssignmentResponseDto response = assignmentService.getAssignment(id);
        return ResponseEntity.ok(response);
    }

    /**
     * 과제 수정 (교수)
     */
    @PutMapping("/{id}")
    public ResponseEntity<AssignmentResponseDto> updateAssignment(
            @PathVariable Long id,
            @Valid @RequestBody AssignmentUpdateRequestDto request,
            Authentication authentication) {
        Long updatedBy = (Long) authentication.getPrincipal();
        log.info("과제 수정 API 호출: assignmentId={}, title={}, dueDate={}, updatedBy={}", 
                id, request.getTitle(), request.getDueDate(), updatedBy);
        AssignmentResponseDto response = assignmentService.updateAssignment(id, request, updatedBy);
        return ResponseEntity.ok(response);
    }

    /**
     * 과제 삭제 (교수)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssignment(@PathVariable Long id) {
        log.info("과제 삭제 API 호출: assignmentId={}", id);
        assignmentService.deleteAssignment(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 과제 제출 (학생)
     */
    @PostMapping("/{id}/submit")
    public ResponseEntity<AssignmentSubmissionResponseDto> submitAssignment(
            @PathVariable Long id,
            @Valid @RequestBody AssignmentSubmissionRequestDto request,
            Authentication authentication) {
        Long studentId = (Long) authentication.getPrincipal();
        log.info("과제 제출 API 호출: assignmentId={}, studentId={}", id, studentId);
        AssignmentSubmissionResponseDto response = assignmentService.submitAssignment(id, request, studentId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 과제 제출 목록 조회 (교수)
     */
    @GetMapping("/{id}/submissions")
    public ResponseEntity<List<AssignmentSubmissionResponseDto>> getSubmissions(@PathVariable Long id) {
        log.info("과제 제출 목록 조회 API 호출: assignmentId={}", id);
        List<AssignmentSubmissionResponseDto> response = assignmentService.getSubmissions(id);
        return ResponseEntity.ok(response);
    }

    /**
     * 과제 채점 (교수)
     */
    @PutMapping("/submissions/{submissionId}/grade")
    public ResponseEntity<AssignmentSubmissionResponseDto> gradeSubmission(
            @PathVariable Long submissionId,
            @Valid @RequestBody AssignmentGradeRequestDto request,
            Authentication authentication) {
        Long gradedBy = (Long) authentication.getPrincipal();
        log.info("과제 채점 API 호출: submissionId={}, score={}, gradedBy={}", 
                submissionId, request.getScore(), gradedBy);
        AssignmentSubmissionResponseDto response = assignmentService.gradeSubmission(submissionId, request, gradedBy);
        return ResponseEntity.ok(response);
    }

    /**
     * 내 제출 조회 (학생)
     */
    @GetMapping("/{id}/my-submission")
    public ResponseEntity<AssignmentSubmissionResponseDto> getMySubmission(
            @PathVariable Long id,
            Authentication authentication) {
        Long studentId = (Long) authentication.getPrincipal();
        log.info("내 제출 조회 API 호출: assignmentId={}, studentId={}", id, studentId);
        AssignmentSubmissionResponseDto response = assignmentService.getMySubmission(id, studentId);
        return ResponseEntity.ok(response);
    }
}
