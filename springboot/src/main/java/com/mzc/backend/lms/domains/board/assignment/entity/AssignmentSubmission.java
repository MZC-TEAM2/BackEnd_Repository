package com.mzc.backend.lms.domains.board.assignment.entity;

import com.mzc.backend.lms.domains.board.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
     * 과제 재제출
     */
    public void resubmit(String content, LocalDateTime submittedAt) {
        this.content = content;
        this.submittedAt = submittedAt;
        this.status = assignment.getDueDate().isBefore(submittedAt) ? "LATE" : "SUBMITTED";
        this.score = null;
        this.feedback = null;
        this.gradedAt = null;
        this.gradedBy = null;
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
     * 제출 삭제 (Soft Delete)
     */
    public void delete() {
        super.delete();
    }
}
