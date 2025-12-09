package com.mzc.backend.lms.domains.enrollment.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

import com.mzc.backend.lms.domains.user.student.entity.Student;
import com.mzc.backend.lms.domains.course.course.entity.Course;


/*
    CourseCart 엔티티
    course_carts 테이블과 매핑
*/

@Entity
@Table(name = "course_carts")
@Getter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class CourseCart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student; // 학생 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course; // 강의 ID

    @Column(name = "added_at", nullable = false)
    private LocalDateTime addedAt; // 담은 일시
}
