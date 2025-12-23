package com.mzc.backend.lms.domains.board.assignment.entity;

import com.mzc.backend.lms.domains.board.entity.Attachment;
import com.mzc.backend.lms.domains.board.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 과제 제출 엔티티
 * 학생이 제출한 과제 정보
 */
@Entity
@Table(name = "assignment_submissions",
		uniqueConstraints = @UniqueConstraint(columnNames = {"assignment_id", "user_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AssignmentSubmission extends AuditableEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;
	
	/**
	 * 과제 ID
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "assignment_id", nullable = false)
	private Assignment assignment;
	
	/**
	 * 제출자 ID (학생)
	 */
	@Column(name = "user_id", nullable = false)
	private Long userId;
	
	/**
	 * 텍스트 제출 내용
	 */
	@Lob
	@Column(name = "content", columnDefinition = "TEXT")
	private String content;
	
	/**
	 * 제출 일시
	 */
	@Column(name = "submitted_at", nullable = false)
	private LocalDateTime submittedAt;
	
	/**
	 * 제출 상태
	 * SUBMITTED: 제출됨
	 * LATE: 지각 제출
	 * GRADED: 채점 완료
	 * NOT_SUBMITTED: 미제출
	 */
	@Column(name = "status", nullable = false, length = 20)
	private String status = "SUBMITTED";
	
	/**
	 * 획득 점수
	 */
	@Column(name = "score", precision = 5, scale = 2)
	private BigDecimal score;
	
	/**
	 * 피드백
	 */
	@Lob
	@Column(name = "feedback", columnDefinition = "TEXT")
	private String feedback;
	
	/**
	 * 채점 일시
	 */
	@Column(name = "graded_at")
	private LocalDateTime gradedAt;
	
	/**
	 * 채점자 ID (교수/조교)
	 */
	@Column(name = "graded_by")
	private Long gradedBy;
	
	/**
	 * 재제출 허용 여부 (교수가 설정)
	 */
	@Column(name = "allow_resubmission", nullable = false)
	private Boolean allowResubmission = false;
	
	/**
	 * 재제출 마감일 (교수가 설정)
	 */
	@Column(name = "resubmission_deadline")
	private LocalDateTime resubmissionDeadline;
	
	/**
	 * 첨부파일 목록
	 */
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(
			name = "submission_attachments",
			joinColumns = @JoinColumn(name = "submission_id"),
			inverseJoinColumns = @JoinColumn(name = "attachment_id")
	)
	private List<Attachment> attachments = new ArrayList<>();
	
	@Builder
	public AssignmentSubmission(Assignment assignment, Long userId, String content,
	                            LocalDateTime submittedAt, String status, Long createdBy) {
		super(createdBy);
		this.assignment = assignment;
		this.userId = userId;
		this.content = content;
		this.submittedAt = submittedAt;
		this.status = status;
	}
	
	// --- 비즈니스 로직 ---
	
	/**
	 * 과제 재제출 / 수정
	 * - 채점 완료된 경우: 교수가 허용했을 때만 재제출 가능 (제출 시간 갱신, 점수 초기화)
	 * - 채점 전인 경우: 수정 (제출 시간 유지, 내용만 변경)
	 */
	public void resubmit(String content, List<Attachment> newAttachments) {
		// 채점 완료 후 재제출인 경우 허용 여부 확인
		if (isGraded() && !canResubmit()) {
			throw new IllegalStateException("재제출이 허용되지 않았습니다.");
		}
		
		this.content = content;
		
		if (isGraded()) {
			// 채점 완료된 경우 → 재제출 (전체 초기화)
			LocalDateTime now = LocalDateTime.now();
			this.submittedAt = now;
			this.status = assignment.getDueDate().isBefore(now) ? "LATE" : "SUBMITTED";
			this.score = null;
			this.feedback = null;
			this.gradedAt = null;
			this.gradedBy = null;
			this.allowResubmission = false; // 재제출 후 다시 비활성화
			this.resubmissionDeadline = null;
		}
		// 채점 전인 경우 → 수정 (submitted_at과 status 유지)
		
		// 첨부파일은 항상 교체
		this.attachments.clear();
		if (newAttachments != null) {
			this.attachments.addAll(newAttachments);
		}
	}
	
	/**
	 * 첨부파일 추가
	 */
	public void addAttachments(List<Attachment> attachments) {
		if (attachments != null) {
			this.attachments.addAll(attachments);
		}
	}
	
	/**
	 * 과제 채점
	 */
	public void grade(BigDecimal score, String feedback, Long graderId) {
		this.score = score;
		this.feedback = feedback;
		this.gradedAt = LocalDateTime.now();
		this.gradedBy = graderId;
		this.status = "GRADED";
		this.updateModifier(graderId);
	}
	
	/**
	 * 지각 제출 여부 확인
	 */
	public boolean isLateSubmission() {
		return "LATE".equals(this.status);
	}
	
	/**
	 * 채점 완료 여부 확인
	 */
	public boolean isGraded() {
		return "GRADED".equals(this.status);
	}
	
	/**
	 * 재제출 가능 여부 확인
	 * - 채점 전: 항상 가능 (수정)
	 * - 채점 후: 교수가 허용하고 마감일 이전인 경우만 가능
	 */
	public boolean canResubmit() {
		// 채점 전이면 언제든지 수정 가능
		if (!isGraded()) {
			return true;
		}
		
		// 채점 후에는 교수 허용 필요
		if (!allowResubmission) {
			return false;
		}
		
		// 재제출 마감일이 설정되어 있으면 체크
		if (resubmissionDeadline != null) {
			return LocalDateTime.now().isBefore(resubmissionDeadline);
		}
		
		return true;
	}
	
	/**
	 * 재제출 허용 (교수)
	 */
	public void allowResubmission(LocalDateTime deadline) {
		if (!isGraded()) {
			throw new IllegalStateException("채점되지 않은 제출은 재제출 허용을 설정할 수 없습니다.");
		}
		this.allowResubmission = true;
		this.resubmissionDeadline = deadline;
	}
	
	/**
	 * 제출 삭제 (Soft Delete)
	 */
	public void delete() {
		super.delete();
	}
}
