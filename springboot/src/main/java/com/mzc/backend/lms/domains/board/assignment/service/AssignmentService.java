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
import com.mzc.backend.lms.domains.board.entity.Attachment;
import com.mzc.backend.lms.domains.board.entity.BoardCategory;
import com.mzc.backend.lms.domains.board.entity.Post;
import com.mzc.backend.lms.domains.board.enums.BoardType;
import com.mzc.backend.lms.domains.board.exception.BoardErrorCode;
import com.mzc.backend.lms.domains.board.exception.BoardException;
import com.mzc.backend.lms.domains.board.repository.AttachmentRepository;
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
	private final AttachmentRepository attachmentRepository;
	
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
	 * ID로 과제 조회 (assignment ID 또는 post ID)
	 * 먼저 assignment ID로 조회 시도, 없으면 post ID로 조회
	 */
	public AssignmentResponseDto getAssignmentByPostId(Long id) {
		Assignment assignment = assignmentRepository.findById(id)
				.or(() -> assignmentRepository.findByPostId(id))
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
				.map(AssignmentResponseDto::from)
				.collect(Collectors.toList());
	}
	
	/**
	 * 전체 과제 목록 조회
	 */
	public List<AssignmentResponseDto> getAllAssignments() {
		List<Assignment> assignments = assignmentRepository.findAll().stream()
				.filter(a -> !a.getIsDeleted())
				.sorted((a1, a2) -> a2.getCreatedAt().compareTo(a1.getCreatedAt()))
				.collect(Collectors.toList());
		
		return assignments.stream()
				.map(AssignmentResponseDto::from)
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
		
		// 1. Assignment 삭제
		assignment.delete();
		
		// 2. 연결된 Post도 삭제
		Post post = assignment.getPost();
		post.delete();
		
		log.info("과제 삭제 완료: assignmentId={}, postId={}", assignmentId, post.getId());
	}
	
	/**
	 * 과제 제출 (학생)
	 * - 신규 제출: 새로운 제출 생성
	 * - 재제출/수정: 기존 제출 업데이트 (정책에 따라 허용 여부 확인)
	 */
	@Transactional
	public AssignmentSubmissionResponseDto submitAssignment(Long assignmentId, AssignmentSubmissionRequestDto request, Long studentId) {
		log.info("과제 제출 시작: assignmentId={}, studentId={}", assignmentId, studentId);
		
		// 과제 조회
		Assignment assignment = assignmentRepository.findById(assignmentId)
				.orElseThrow(() -> new BoardException(BoardErrorCode.POST_NOT_FOUND));
		
		// 기존 제출 확인
		var existingSubmission = submissionRepository.findByAssignmentIdAndUserId(assignment.getId(), studentId);
		
		if (existingSubmission.isPresent()) {
			// 이미 제출한 경우 - 수정/재제출 로직
			AssignmentSubmission existing = existingSubmission.get();
			log.info("기존 제출 발견: submissionId={}, status={}, isGraded={}",
					existing.getId(), existing.getStatus(), existing.isGraded());
			
			// 채점 완료된 경우 재제출 허용 여부 확인
			if (existing.isGraded() && !existing.canResubmit()) {
				log.warn("재제출 불가: submissionId={}, 채점 완료 상태", existing.getId());
				throw new BoardException(BoardErrorCode.RESUBMISSION_NOT_ALLOWED);
			}
			
			// 재제출/수정 허용 - 내용 업데이트
			LocalDateTime now = LocalDateTime.now();
			
			// 첨부파일 처리
			List<Attachment> attachments = null;
			if (request.getAttachmentIds() != null && !request.getAttachmentIds().isEmpty()) {
				attachments = attachmentRepository.findAllById(request.getAttachmentIds());
				log.info("첨부파일 {} 개 업데이트", attachments.size());
			}
			
			existing.resubmit(request.getContent(), attachments);
			existing.updateModifier(studentId);
			
			log.info("과제 재제출/수정 완료: submissionId={}", existing.getId());
			return AssignmentSubmissionResponseDto.from(existing);
		} else {
			// 신규 제출
			log.info("신규 제출 생성");
			
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
			
			// 첨부파일 처리
			if (request.getAttachmentIds() != null && !request.getAttachmentIds().isEmpty()) {
				List<Attachment> attachments = attachmentRepository.findAllById(request.getAttachmentIds());
				savedSubmission.addAttachments(attachments);
				log.info("첨부파일 {} 개 추가 완료", attachments.size());
			}
			
			log.info("과제 제출 완료: submissionId={}, status={}", savedSubmission.getId(), status);
			
			return AssignmentSubmissionResponseDto.from(savedSubmission);
		}
	}
	
	/**
	 * 과제 재제출/수정 (학생)
	 * - 채점 완료된 경우: 재제출 (점수 초기화)
	 * - 채점 전인 경우: 수정 (내용만 변경)
	 */
	@Transactional
	public AssignmentSubmissionResponseDto resubmitAssignment(Long submissionId, String content) {
		log.info("과제 재제출/수정 시작: submissionId={}", submissionId);
		
		AssignmentSubmission submission = submissionRepository.findById(submissionId)
				.orElseThrow(() -> new BoardException(BoardErrorCode.POST_NOT_FOUND));
		
		submission.resubmit(content, null);
		
		log.info("과제 재제출/수정 완료: submissionId={}, isGraded={}", submissionId, submission.isGraded());
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
	
	/**
	 * 재제출 허용 (교수)
	 * 채점된 제출에 대해 재제출을 허용합니다.
	 *
	 * @param submissionId 제출 ID
	 * @param deadline     재제출 마감일 (null이면 무제한)
	 * @param professorId  교수 ID
	 */
	@Transactional
	public AssignmentSubmissionResponseDto allowResubmission(Long submissionId, LocalDateTime deadline, Long professorId) {
		log.info("재제출 허용 시작: submissionId={}, deadline={}, professorId={}", submissionId, deadline, professorId);
		
		AssignmentSubmission submission = submissionRepository.findById(submissionId)
				.orElseThrow(() -> new BoardException(BoardErrorCode.POST_NOT_FOUND));
		
		submission.allowResubmission(deadline);
		submission.updateModifier(professorId);
		
		log.info("재제출 허용 완료: submissionId={}", submissionId);
		return AssignmentSubmissionResponseDto.from(submission);
	}
}
