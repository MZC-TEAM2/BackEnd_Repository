package com.mzc.backend.lms.domains.user.student.entity;

import com.mzc.backend.lms.domains.user.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 학생 엔티티
 * students 테이블과 매핑
 */
@Entity
@Table(name = "students", indexes = {
    @Index(name = "idx_students_student_id", columnList = "student_id"),
    @Index(name = "idx_students_admission_year", columnList = "admission_year")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Student {

    @Id
    @Column(name = "student_id", length = 20)
    private String studentId;  // 학번 (예: 20240101001) - PK이자 User.id와 동일

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "student_id")
    private User user;

    @Column(name = "admission_year", nullable = false)
    private Integer admissionYear;  // 입학년도

    @Column(name = "grade", nullable = false)
    private Integer grade;  // 학년 (1~4)

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Builder
    private Student(String studentId, User user, Integer admissionYear, Integer grade) {
        this.studentId = studentId;
        this.user = user;
        this.admissionYear = admissionYear;
        this.grade = grade != null ? grade : 1;  // 기본값 1학년
    }

    /**
     * 학생 생성
     */
    public static Student create(String studentId, User user, Integer admissionYear, Integer grade) {
        return Student.builder()
                .studentId(studentId)
                .user(user)
                .admissionYear(admissionYear)
                .grade(grade)
                .build();
    }

    /**
     * 학년 업데이트
     */
    public void updateGrade(Integer newGrade) {
        if (newGrade < 1 || newGrade > 4) {
            throw new IllegalArgumentException("학년은 1~4 사이여야 합니다.");
        }
        this.grade = newGrade;
    }

    /**
     * 학번 getter (기존 코드 호환용)
     */
    public String getStudentNumber() {
        return this.studentId;
    }
}
