package com.mzc.backend.lms.domains.board.assignment.entity;

import com.mzc.backend.lms.domains.board.entity.AuditableEntity;
import com.mzc.backend.lms.domains.board.entity.Post;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 과제 엔티티
 * posts 테이블과 1:1 관계
 */
@Entity
@Table(name = "assignments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Assignment extends AuditableEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;
	
	/**
	 * 게시글 ID (1:1 관계)
	 * 제목/내용은 Post 테이블에 저장
	 */
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "post_id", nullable = false, unique = true)
	private Post post;
	
	/**
	 * 강의 ID
	 */
	@Column(name = "course_id", nullable = false)
	private Long courseId;
	
	/**
	 * 제출 마감일
	 */
	@Column(name = "due_date", nullable = false)
	private LocalDateTime dueDate;
	
	/**
	 * 만점
	 */
	@Column(name = "max_score", nullable = false, precision = 5, scale = 2)
	private BigDecimal maxScore;
	
	/**
	 * 제출 방법
	 * UPLOAD: 파일 업로드
	 * TEXT: 텍스트 입력
	 * LINK: URL 링크
	 */
	@Column(name = "submission_method", nullable = false, length = 20)
	private String submissionMethod;
	
	/**
	 * 지각 제출 허용 여부
	 */
	@Column(name = "late_submission_allowed", nullable = false)
	private Boolean lateSubmissionAllowed = false;
	
	/**
	 * 지각 제출 감점 비율 (%)
	 */
	@Column(name = "late_penalty_percent", precision = 3, scale = 2)
	private BigDecimal latePenaltyPercent;
	
	/**
	 * 최대 파일 크기 (MB)
	 */
	@Column(name = "max_file_size_mb")
	private Integer maxFileSizeMb = 10;
	
	/**
	 * 허용 파일 확장자 (쉼표 구분)
	 * 예: "pdf,docx,hwp,zip"
	 */
	@Column(name = "allowed_file_types")
	private String allowedFileTypes;
	
	/**
	 * 제출 지침
	 */
	@Lob
	@Column(name = "instructions", columnDefinition = "TEXT")
	private String instructions;
	
	@Builder
	public Assignment(Post post, Long courseId, LocalDateTime dueDate, BigDecimal maxScore,
	                  String submissionMethod, Boolean lateSubmissionAllowed,
	                  BigDecimal latePenaltyPercent, Integer maxFileSizeMb,
	                  String allowedFileTypes, String instructions, Long createdBy) {
		super(createdBy);
		this.post = post;
		this.courseId = courseId;
		this.dueDate = dueDate;
		this.maxScore = maxScore;
		this.submissionMethod = submissionMethod;
		this.lateSubmissionAllowed = lateSubmissionAllowed;
		this.latePenaltyPercent = latePenaltyPercent;
		this.maxFileSizeMb = maxFileSizeMb;
		this.allowedFileTypes = allowedFileTypes;
		this.instructions = instructions;
	}
	
	// --- 비즈니스 로직 ---
	
	/**
	 * 과제 정보 수정
	 */
	public void update(LocalDateTime dueDate, BigDecimal maxScore, String submissionMethod,
	                   Boolean lateSubmissionAllowed, BigDecimal latePenaltyPercent,
	                   Integer maxFileSizeMb, String allowedFileTypes, String instructions,
	                   Long modifierId) {
		this.dueDate = dueDate;
		this.maxScore = maxScore;
		this.submissionMethod = submissionMethod;
		this.lateSubmissionAllowed = lateSubmissionAllowed;
		this.latePenaltyPercent = latePenaltyPercent;
		this.maxFileSizeMb = maxFileSizeMb;
		this.allowedFileTypes = allowedFileTypes;
		this.instructions = instructions;
		this.updateModifier(modifierId);
	}
	
	/**
	 * 마감일이 지났는지 확인
	 */
	public boolean isOverdue() {
		return LocalDateTime.now().isAfter(dueDate);
	}
	
	/**
	 * 지각 제출 가능 여부 확인
	 */
	public boolean canSubmitLate() {
		return lateSubmissionAllowed && isOverdue();
	}
	
	/**
	 * 과제 삭제 (Soft Delete)
	 */
	public void delete() {
		super.delete();
	}
}
