package com.mzc.backend.lms.domains.assessment.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 응시(Attempt)/결과 엔티티 (exam_results)
 *
 * 주의: exam_results 테이블은 BaseEntity/AuditableEntity(is_deleted/updated_by) 컬럼이 없어 상속하지 않습니다.
 */
@Entity
@Table(name = "exam_results",
        uniqueConstraints = @UniqueConstraint(columnNames = {"exam_id", "user_id"}))
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AssessmentAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Assessment assessment;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "is_late", nullable = false)
    private Boolean isLate = false;

    @Column(name = "late_penalty_points", nullable = false, precision = 5, scale = 2)
    private BigDecimal latePenaltyPoints = BigDecimal.ZERO;

    @Column(name = "late_penalty_rate", nullable = false, precision = 5, scale = 4)
    private BigDecimal latePenaltyRate = BigDecimal.ZERO;

    @Column(name = "score", precision = 5, scale = 2)
    private BigDecimal score;

    @Column(name = "grade", length = 2)
    private String grade;

    @Lob
    @Column(name = "answer_data", columnDefinition = "TEXT")
    private String answerData;

    @Lob
    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;

    @Column(name = "graded_at")
    private LocalDateTime gradedAt;

    @Column(name = "graded_by")
    private Long gradedBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public AssessmentAttempt(Assessment assessment, Long userId, LocalDateTime startedAt) {
        this.assessment = assessment;
        this.userId = userId;
        this.startedAt = startedAt;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public boolean isSubmitted() {
        return submittedAt != null;
    }

    public void start(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public void submit(String answerData, LocalDateTime submittedAt, BigDecimal score) {
        this.answerData = answerData;
        this.submittedAt = submittedAt;
        this.score = score;
    }

    public void grade(BigDecimal finalScore, String feedback, Long graderId) {
        this.score = finalScore;
        this.feedback = feedback;
        this.gradedAt = LocalDateTime.now();
        this.gradedBy = graderId;
    }

    public void markLate(BigDecimal penaltyPoints, BigDecimal penaltyRate) {
        this.isLate = true;
        this.latePenaltyPoints = penaltyPoints != null ? penaltyPoints : BigDecimal.ZERO;
        this.latePenaltyRate = penaltyRate != null ? penaltyRate : BigDecimal.ZERO;
    }

    public void markOnTime() {
        this.isLate = false;
        this.latePenaltyPoints = BigDecimal.ZERO;
        this.latePenaltyRate = BigDecimal.ZERO;
    }
}


