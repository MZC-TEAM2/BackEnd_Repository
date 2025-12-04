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
    @Index(name = "idx_students_student_number", columnList = "student_number"),
    @Index(name = "idx_students_admission_year", columnList = "admission_year")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Student {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "student_number", length = 20, unique = true, nullable = false)
    private String studentNumber;  // 학번 (예: 2024123456)

    @Column(name = "admission_year", nullable = false)
    private Integer admissionYear;  // 입학년도

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Builder
    private Student(User user, String studentNumber, Integer admissionYear) {
        this.user = user;
        this.studentNumber = studentNumber;
        this.admissionYear = admissionYear;
        this.userId = user.getId();
    }

    /**
     * 학생 생성
     */
    public static Student create(User user, String studentNumber, Integer admissionYear) {
        return Student.builder()
                .user(user)
                .studentNumber(studentNumber)
                .admissionYear(admissionYear)
                .build();
    }
}
