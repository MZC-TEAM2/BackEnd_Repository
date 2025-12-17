package com.mzc.backend.lms.domains.board.assignment.service;

import com.mzc.backend.lms.domains.board.assignment.dto.request.AssignmentCreateRequestDto;
import com.mzc.backend.lms.domains.board.assignment.dto.request.AssignmentGradeRequestDto;
import com.mzc.backend.lms.domains.board.assignment.dto.request.AssignmentSubmissionRequestDto;
import com.mzc.backend.lms.domains.board.assignment.dto.request.AssignmentUpdateRequestDto;
import com.mzc.backend.lms.domains.board.assignment.dto.response.AssignmentResponseDto;
import com.mzc.backend.lms.domains.board.assignment.dto.response.AssignmentSubmissionResponseDto;
import com.mzc.backend.lms.domains.board.assignment.entity.Assignment;
import com.mzc.backend.lms.domains.board.assignment.entity.AssignmentSubmission;
import com.mzc.backend.lms.domains.board.assignment.repository.AssignmentRepository;
import com.mzc.backend.lms.domains.board.assignment.repository.AssignmentSubmissionRepository;
import com.mzc.backend.lms.domains.board.entity.BoardCategory;
import com.mzc.backend.lms.domains.board.entity.Post;
import com.mzc.backend.lms.domains.board.enums.BoardType;
import com.mzc.backend.lms.domains.board.exception.BoardErrorCode;
import com.mzc.backend.lms.domains.board.exception.BoardException;
import com.mzc.backend.lms.domains.board.repository.BoardCategoryRepository;
import com.mzc.backend.lms.domains.board.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 과제 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionRepository submissionRepository;
    private final PostRepository postRepository;
    private final BoardCategoryRepository boardCategoryRepository;

    /**
     * 과제 등록 (교수)
     * Post와 Assignment를 함께 생성합니다.
     */
    @Transactional
    public AssignmentResponseDto createAssignment(AssignmentCreateRequestDto request, Long createdBy) {
        log.info("과제 등록 시작: title={}, courseId={}, createdBy={}", request.getTitle(), request.getCourseId(), createdBy);

        // 0. 과제 게시판 카테고리 조회
        BoardCategory assignmentCategory = boardCategoryRepository.findByBoardType(BoardType.ASSIGNMENT)
                .orElseThrow(() -> new BoardException(BoardErrorCode.BOARD_CATEGORY_NOT_FOUND));

        // 1. Post 생성
        Post post = Post.builder()
                .category(assignmentCategory)
                .title(request.getTitle())
                .content(request.getContent())
                .authorId(createdBy)
                .postType(com.mzc.backend.lms.domains.board.enums.PostType.ASSIGNMENT)
                .isAnonymous(false)
                .build();

        Post savedPost = postRepository.save(post);
        log.info("게시글 생성 완료: postId={}", savedPost.getId());

        // 2. Assignment 생성
        Assignment assignment = Assignment.builder()
                .post(savedPost)
                .courseId(request.getCourseId())
                .dueDate(request.getDueDate())
                .maxScore(request.getMaxScore())
                .submissionMethod(request.getSubmissionMethod())
                .lateSubmissionAllowed(request.getLateSubmissionAllowed())
                .latePenaltyPercent(request.getLatePenaltyPercent())
                .maxFileSizeMb(request.getMaxFileSizeMb())
                .allowedFileTypes(request.getAllowedFileTypes())
                .instructions(request.getInstructions())
                .createdBy(createdBy)
                .build();

        Assignment savedAssignment = assignmentRepository.save(assignment);
        log.info("과제 등록 완료: assignmentId={}", savedAssignment.getId());

        return AssignmentResponseDto.from(savedAssignment);
    }

    /**
     * 과제 조회
     */
    public AssignmentResponseDto getAssignment(Long assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new BoardException(BoardErrorCode.POST_NOT_FOUND));

        if (assignment.getIsDeleted()) {
            throw new BoardException(BoardErrorCode.POST_ALREADY_DELETED);
        }

        return AssignmentResponseDto.from(assignment);
    }

    /**
     * 게시글 ID로 과제 조회
     */
    public AssignmentResponseDto getAssignmentByPostId(Long postId) {
        Assignment assignment = assignmentRepository.findByPostId(postId)
                .orElseThrow(() -> new BoardException(BoardErrorCode.POST_NOT_FOUND));

        if (assignment.getIsDeleted()) {
            throw new BoardException(BoardErrorCode.POST_ALREADY_DELETED);
        }

        return AssignmentResponseDto.from(assignment);
    }

    /**
     * 강의별 과제 목록 조회
     */
    public List<AssignmentResponseDto> getAssignmentsByCourse(Long courseId) {
        List<Assignment> assignments = assignmentRepository.findByCourseId(courseId);
        
        return assignments.stream()
                .map(AssignmentResponseDto::fromWithoutPost)
                .collect(Collectors.toList());
    }

    /**
     * 과제 수정 (교수)
     * Post와 Assignment를 함께 수정합니다.
     */
    @Transactional
    public AssignmentResponseDto updateAssignment(Long assignmentId, AssignmentUpdateRequestDto request, Long updatedBy) {
        log.info("과제 수정 시작: assignmentId={}, updatedBy={}", assignmentId, updatedBy);

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new BoardException(BoardErrorCode.POST_NOT_FOUND));

        if (assignment.getIsDeleted()) {
            throw new BoardException(BoardErrorCode.POST_ALREADY_DELETED);
        }

        // 1. Post 수정 (title, content가 있는 경우)
        Post post = assignment.getPost();
        if (request.getTitle() != null || request.getContent() != null) {
            post.update(
                    request.getTitle() != null ? request.getTitle() : post.getTitle(),
                    request.getContent() != null ? request.getContent() : post.getContent(),
                    post.isAnonymous()
            );
            log.info("게시글 수정 완료: postId={}", post.getId());
        }

        // 2. Assignment 수정
        assignment.update(
                request.getDueDate() != null ? request.getDueDate() : assignment.getDueDate(),
                request.getMaxScore() != null ? request.getMaxScore() : assignment.getMaxScore(),
                request.getSubmissionMethod() != null ? request.getSubmissionMethod() : assignment.getSubmissionMethod(),
                request.getLateSubmissionAllowed() != null ? request.getLateSubmissionAllowed() : assignment.getLateSubmissionAllowed(),
                request.getLatePenaltyPercent(),
                request.getMaxFileSizeMb(),
                request.getAllowedFileTypes(),
                request.getInstructions(),
                updatedBy
        );

        log.info("과제 수정 완료: assignmentId={}", assignmentId);
        return AssignmentResponseDto.from(assignment);
    }

    /**
     * 과제 삭제 (교수)
     */
    @Transactional
    public void deleteAssignment(Long assignmentId) {
        log.info("과제 삭제 시작: assignmentId={}", assignmentId);

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new BoardException(BoardErrorCode.POST_NOT_FOUND));

        if (assignment.getIsDeleted()) {
            throw new BoardException(BoardErrorCode.POST_ALREADY_DELETED);
        }

        assignment.delete();
        log.info("과제 삭제 완료: assignmentId={}", assignmentId);
    }

    /**
     * 과제 제출 (학생)
     */
    @Transactional
    public AssignmentSubmissionResponseDto submitAssignment(Long assignmentId, AssignmentSubmissionRequestDto request, Long studentId) {
        log.info("과제 제출 시작: assignmentId={}, studentId={}", assignmentId, studentId);

        // 과제 조회
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new BoardException(BoardErrorCode.POST_NOT_FOUND));

        // 이미 제출했는지 확인
        submissionRepository.findByAssignmentIdAndUserId(assignment.getId(), studentId)
                .ifPresent(existing -> {
                    throw new BoardException(BoardErrorCode.ALREADY_SUBMITTED);
                });

        // 제출 시간
        LocalDateTime submittedAt = LocalDateTime.now();

        // 지각 여부 판단
        String status = assignment.getDueDate().isBefore(submittedAt) ? "LATE" : "SUBMITTED";

        // 제출 생성
        AssignmentSubmission submission = AssignmentSubmission.builder()
                .assignment(assignment)
                .userId(studentId)
                .content(request.getContent())
                .submittedAt(submittedAt)
                .status(status)
                .createdBy(studentId)
                .build();

        AssignmentSubmission savedSubmission = submissionRepository.save(submission);
        log.info("과제 제출 완료: submissionId={}, status={}", savedSubmission.getId(), status);

        return AssignmentSubmissionResponseDto.from(savedSubmission);
    }

    /**
     * 과제 재제출 (학생)
     */
    @Transactional
    public AssignmentSubmissionResponseDto resubmitAssignment(Long submissionId, String content) {
        log.info("과제 재제출 시작: submissionId={}", submissionId);

        AssignmentSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new BoardException(BoardErrorCode.POST_NOT_FOUND));

        LocalDateTime submittedAt = LocalDateTime.now();
        submission.resubmit(content, submittedAt);

        log.info("과제 재제출 완료: submissionId={}", submissionId);
        return AssignmentSubmissionResponseDto.from(submission);
    }

    /**
     * 과제 채점 (교수)
     */
    @Transactional
    public AssignmentSubmissionResponseDto gradeSubmission(Long submissionId, AssignmentGradeRequestDto request, Long gradedBy) {
        log.info("과제 채점 시작: submissionId={}, score={}, gradedBy={}", submissionId, request.getScore(), gradedBy);

        AssignmentSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new BoardException(BoardErrorCode.POST_NOT_FOUND));

        submission.grade(request.getScore(), request.getFeedback(), gradedBy);

        log.info("과제 채점 완료: submissionId={}", submissionId);
        return AssignmentSubmissionResponseDto.from(submission);
    }

    /**
     * 과제별 제출 목록 조회 (교수)
     */
    public List<AssignmentSubmissionResponseDto> getSubmissions(Long assignmentId) {
        List<AssignmentSubmission> submissions = submissionRepository.findByAssignmentId(assignmentId);
        
        return submissions.stream()
                .map(AssignmentSubmissionResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 내 제출 조회 (학생)
     */
    public AssignmentSubmissionResponseDto getMySubmission(Long assignmentId, Long userId) {
        AssignmentSubmission submission = submissionRepository.findByAssignmentIdAndUserId(assignmentId, userId)
                .orElseThrow(() -> new BoardException(BoardErrorCode.POST_NOT_FOUND));

        return AssignmentSubmissionResponseDto.from(submission);
    }

    /**
     * 채점 대기 목록 조회 (교수)
     */
    public List<AssignmentSubmissionResponseDto> getPendingGrading(Long assignmentId) {
        List<AssignmentSubmission> pending = submissionRepository.findPendingGradingByAssignment(assignmentId);
        
        return pending.stream()
                .map(AssignmentSubmissionResponseDto::from)
                .collect(Collectors.toList());
    }
}
