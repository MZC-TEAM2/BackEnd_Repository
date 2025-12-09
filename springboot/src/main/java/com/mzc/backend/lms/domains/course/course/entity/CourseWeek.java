package com.mzc.backend.lms.domains.course.course.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

/*
    CourseWeek 엔티티
    course_weeks 테이블과 매핑
*/

@Entity
@Table(name = "course_weeks")
@Getter
@NoArgsConstructor @AllArgsConstructor
@Builder

public class CourseWeek {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "week_number", nullable = false)
    private Integer weekNumber; // 주차 번호

    @Column(name = "week_title", length = 200, nullable = false)
    private String weekTitle; // 주차 제목

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
