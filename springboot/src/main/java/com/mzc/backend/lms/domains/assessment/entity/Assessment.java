package com.mzc.backend.lms.domains.assessment.entity;

import com.mzc.backend.lms.domains.assessment.enums.AssessmentType;
import com.mzc.backend.lms.domains.board.entity.Post;
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
 * 시험/퀴즈 엔티티 (exams)
 *
 * 주의: exams 테이블은 BaseEntity/AuditableEntity(is_deleted/updated_by) 컬럼이 없어 상속하지 않습니다.
 */
@Entity
@Table(name = "exams")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Assessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false, unique = true)
    private Post post;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Enumerated(EnumType.STRING)
    @Column(name = "exam_type", nullable = false, length = 20)
    private AssessmentType type;

    /** 시작시간 */
    @Column(name = "exam_date", nullable = false)
    private LocalDateTime startAt;

    /** 제한시간(분) */
    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    /** 총점 */
    @Column(name = "total_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal totalScore;

    @Column(name = "is_online", nullable = false)
    private Boolean isOnline = false;

    @Column(name = "location", length = 100)
    private String location;

    @Lob
    @Column(name = "instructions", columnDefinition = "TEXT")
    private String instructions;

    @Column(name = "question_count")
    private Integer questionCount;

    @Column(name = "passing_score", precision = 5, scale = 2)
    private BigDecimal passingScore;

    /** 문제 데이터(JSON, 정답 포함) */
    @Lob
    @Column(name = "question_data", columnDefinition = "TEXT")
    private String questionData;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public Assessment(Post post,
                      Long courseId,
                      AssessmentType type,
                      LocalDateTime startAt,
                      Integer durationMinutes,
                      BigDecimal totalScore,
                      Boolean isOnline,
                      String location,
                      String instructions,
                      Integer questionCount,
                      BigDecimal passingScore,
                      String questionData,
                      Long createdBy) {
        this.post = post;
        this.courseId = courseId;
        this.type = type;
        this.startAt = startAt;
        this.durationMinutes = durationMinutes;
        this.totalScore = totalScore;
        this.isOnline = isOnline != null ? isOnline : false;
        this.location = location;
        this.instructions = instructions;
        this.questionCount = questionCount;
        this.passingScore = passingScore;
        this.questionData = questionData;
        this.createdBy = createdBy;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }

    public LocalDateTime endAt() {
        return startAt.plusMinutes(durationMinutes);
    }

    public void update(LocalDateTime startAt,
                       Integer durationMinutes,
                       BigDecimal totalScore,
                       Boolean isOnline,
                       String location,
                       String instructions,
                       Integer questionCount,
                       BigDecimal passingScore,
                       String questionData) {
        if (startAt != null) this.startAt = startAt;
        if (durationMinutes != null) this.durationMinutes = durationMinutes;
        if (totalScore != null) this.totalScore = totalScore;
        if (isOnline != null) this.isOnline = isOnline;
        if (location != null) this.location = location;
        if (instructions != null) this.instructions = instructions;
        if (questionCount != null) this.questionCount = questionCount;
        if (passingScore != null) this.passingScore = passingScore;
        if (questionData != null) this.questionData = questionData;
    }
}


