package com.mzc.backend.lms.domains.course.grade.entity;

import com.mzc.backend.lms.domains.course.grade.enums.GradeStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 성적 엔티티 (단일 grades 테이블)
 * - academic_term_id로 학기 구분
 */
@Entity
@Table(name = "grades",
        uniqueConstraints = @UniqueConstraint(name = "uk_grades_course_student", columnNames = {"course_id", "student_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "academic_term_id", nullable = false)
    private Long academicTermId;

    @Column(name = "final_score", precision = 5, scale = 2)
    private BigDecimal finalScore;

    @Column(name = "final_grade", length = 2)
    private String finalGrade;

    @Column(name = "quiz_score", precision = 5, scale = 2)
    private BigDecimal quizScore;

    @Column(name = "assignment_score", precision = 5, scale = 2)
    private BigDecimal assignmentScore;

    @Column(name = "midterm_score", precision = 5, scale = 2)
    private BigDecimal midtermScore;

    @Column(name = "final_exam_score", precision = 5, scale = 2)
    private BigDecimal finalExamScore;

    @Column(name = "attendance_score", precision = 5, scale = 2)
    private BigDecimal attendanceScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private GradeStatus status = GradeStatus.PENDING;

    @Column(name = "graded_at")
    private LocalDateTime gradedAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void publish(BigDecimal quizScore,
                        BigDecimal assignmentScore,
                        BigDecimal midtermScore,
                        BigDecimal finalExamScore,
                        BigDecimal attendanceScore,
                        BigDecimal finalScore,
                        String finalGrade,
                        LocalDateTime now) {
        this.quizScore = quizScore;
        this.assignmentScore = assignmentScore;
        this.midtermScore = midtermScore;
        this.finalExamScore = finalExamScore;
        this.attendanceScore = attendanceScore;
        this.finalScore = finalScore;
        this.finalGrade = finalGrade;
        this.status = GradeStatus.PUBLISHED;
        this.gradedAt = now;
        this.publishedAt = now;
    }

    /**
     * 성적 산출(점수 계산)만 수행하고, 공개는 하지 않음
     * - status=GRADED
     * - final_grade는 publish 단계에서 상대평가로 부여
     */
    public void markGraded(BigDecimal quizScore,
                           BigDecimal assignmentScore,
                           BigDecimal midtermScore,
                           BigDecimal finalExamScore,
                           BigDecimal attendanceScore,
                           BigDecimal finalScore,
                           LocalDateTime now) {
        this.quizScore = quizScore;
        this.assignmentScore = assignmentScore;
        this.midtermScore = midtermScore;
        this.finalExamScore = finalExamScore;
        this.attendanceScore = attendanceScore;
        this.finalScore = finalScore;
        this.finalGrade = null;
        this.status = GradeStatus.GRADED;
        this.gradedAt = now;
        this.publishedAt = null;
    }

    /**
     * 공개 단계: 등급 확정 + 공개 처리
     */
    public void publishFinalGrade(String finalGrade, LocalDateTime now) {
        this.finalGrade = finalGrade;
        this.status = GradeStatus.PUBLISHED;
        if (this.gradedAt == null) {
            this.gradedAt = now;
        }
        this.publishedAt = now;
    }

    @Builder
    private Grade(Long courseId,
                  Long studentId,
                  Long academicTermId,
                  BigDecimal finalScore,
                  String finalGrade,
                  BigDecimal quizScore,
                  BigDecimal assignmentScore,
                  BigDecimal midtermScore,
                  BigDecimal finalExamScore,
                  BigDecimal attendanceScore,
                  GradeStatus status,
                  LocalDateTime gradedAt,
                  LocalDateTime publishedAt) {
        this.courseId = courseId;
        this.studentId = studentId;
        this.academicTermId = academicTermId;
        this.finalScore = finalScore;
        this.finalGrade = finalGrade;
        this.quizScore = quizScore;
        this.assignmentScore = assignmentScore;
        this.midtermScore = midtermScore;
        this.finalExamScore = finalExamScore;
        this.attendanceScore = attendanceScore;
        if (status != null) this.status = status;
        this.gradedAt = gradedAt;
        this.publishedAt = publishedAt;
    }
}


