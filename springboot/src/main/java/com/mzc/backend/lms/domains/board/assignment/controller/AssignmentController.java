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
     * 과제 목록 조회 (강의별 또는 전체)
     */
    @GetMapping
    public ResponseEntity<List<AssignmentResponseDto>> getAssignments(
            @RequestParam(required = false) Long courseId) {
        if (courseId != null) {
            log.info("강의별 과제 목록 조회 API 호출: courseId={}", courseId);
            List<AssignmentResponseDto> response = assignmentService.getAssignmentsByCourse(courseId);
            return ResponseEntity.ok(response);
        } else {
            log.info("전체 과제 목록 조회 API 호출");
            List<AssignmentResponseDto> response = assignmentService.getAllAssignments();
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 과제 상세 조회 (Post ID로 조회)
     */
    @GetMapping("/{id}")
    public ResponseEntity<AssignmentResponseDto> getAssignment(@PathVariable Long id) {
        log.info("과제 상세 조회 API 호출: postId={}", id);
        AssignmentResponseDto response = assignmentService.getAssignmentByPostId(id);
        return ResponseEntity.ok(response);
    }

    /**
     * 과제 수정 (교수) - Post ID로 조회
     */
    @PutMapping("/{id}")
    public ResponseEntity<AssignmentResponseDto> updateAssignment(
            @PathVariable Long id,
            @Valid @RequestBody AssignmentUpdateRequestDto request,
            Authentication authentication) {
        Long updatedBy = (Long) authentication.getPrincipal();
        log.info("과제 수정 API 호출: postId={}, title={}, dueDate={}, updatedBy={}", 
                id, request.getTitle(), request.getDueDate(), updatedBy);
        // postId로 assignment 조회 후 수정
        AssignmentResponseDto assignment = assignmentService.getAssignmentByPostId(id);
        AssignmentResponseDto response = assignmentService.updateAssignment(assignment.getId(), request, updatedBy);
        return ResponseEntity.ok(response);
    }

    /**
     * 과제 삭제 (교수) - Post ID로 조회
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssignment(@PathVariable Long id) {
        log.info("과제 삭제 API 호출: postId={}", id);
        // postId로 assignment 조회 후 삭제
        AssignmentResponseDto assignment = assignmentService.getAssignmentByPostId(id);
        assignmentService.deleteAssignment(assignment.getId());
        return ResponseEntity.noContent().build();
    }

    /**
     * 과제 제출 (학생) - Post ID로 조회
     */
    @PostMapping("/{id}/submit")
    public ResponseEntity<AssignmentSubmissionResponseDto> submitAssignment(
            @PathVariable Long id,
            @Valid @RequestBody AssignmentSubmissionRequestDto request,
            Authentication authentication) {
        Long studentId = (Long) authentication.getPrincipal();
        log.info("과제 제출 API 호출: postId={}, studentId={}", id, studentId);
        // postId로 assignment 조회 후 제출
        AssignmentResponseDto assignment = assignmentService.getAssignmentByPostId(id);
        AssignmentSubmissionResponseDto response = assignmentService.submitAssignment(assignment.getId(), request, studentId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 과제 제출 목록 조회 (교수) - Post ID로 조회
     */
    @GetMapping("/{id}/submissions")
    public ResponseEntity<List<AssignmentSubmissionResponseDto>> getSubmissions(@PathVariable Long id) {
        log.info("과제 제출 목록 조회 API 호출: postId={}", id);
        try {
            // postId로 assignment 조회 후 제출 목록 조회
            AssignmentResponseDto assignment = assignmentService.getAssignmentByPostId(id);
            log.info("Assignment 조회 성공: assignmentId={}", assignment.getId());
            List<AssignmentSubmissionResponseDto> response = assignmentService.getSubmissions(assignment.getId());
            log.info("제출 목록 조회 완료: count={}", response.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("과제 제출 목록 조회 실패: postId={}, error={}", id, e.getMessage(), e);
            throw e;
        }
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
     * 내 제출 조회 (학생) - Post ID로 조회
     */
    @GetMapping("/{id}/my-submission")
    public ResponseEntity<AssignmentSubmissionResponseDto> getMySubmission(
            @PathVariable Long id,
            Authentication authentication) {
        Long studentId = (Long) authentication.getPrincipal();
        log.info("내 제출 조회 API 호출: postId={}, studentId={}", id, studentId);
        // postId로 assignment 조회 후 내 제출 조회
        AssignmentResponseDto assignment = assignmentService.getAssignmentByPostId(id);
        AssignmentSubmissionResponseDto response = assignmentService.getMySubmission(assignment.getId(), studentId);
        return ResponseEntity.ok(response);
    }

    /**
     * 재제출 허용 (교수)
     */
    @PostMapping("/submissions/{submissionId}/allow-resubmission")
    public ResponseEntity<AssignmentSubmissionResponseDto> allowResubmission(
            @PathVariable Long submissionId,
            @RequestParam(required = false) String deadline,
            Authentication authentication) {
        Long professorId = (Long) authentication.getPrincipal();
        log.info("재제출 허용 API 호출: submissionId={}, deadline={}, professorId={}", submissionId, deadline, professorId);
        
        java.time.LocalDateTime resubmissionDeadline = null;
        if (deadline != null && !deadline.isEmpty()) {
            resubmissionDeadline = java.time.LocalDateTime.parse(deadline);
        }
        
        AssignmentSubmissionResponseDto response = assignmentService.allowResubmission(submissionId, resubmissionDeadline, professorId);
        return ResponseEntity.ok(response);
    }
}
