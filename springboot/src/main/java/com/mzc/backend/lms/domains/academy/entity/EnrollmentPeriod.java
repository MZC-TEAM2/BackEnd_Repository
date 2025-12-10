package com.mzc.backend.lms.domains.academy.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import jakarta.persistence.Column;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;

@Entity
@Table(name="enrollment_periods")
@Getter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class EnrollmentPeriod {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 수강신청 기간 식별자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "term_id", nullable = false)  // academic_term_id → term_id로 변경
    private AcademicTerm academicTerm;

    @Column(name = "period_name", length = 50, nullable = false)
    private String periodName; // 기간명 (예: 1차 수강신청, 정정기간)

    @Column(name = "start_datetime", nullable = false)  // start_date → start_datetime으로 변경
    private LocalDateTime startDatetime; // 수강신청 시작일시

    @Column(name = "end_datetime", nullable = false)  // end_date → end_datetime으로 변경
    private LocalDateTime endDatetime; // 수강신청 종료일시

    @Column(name = "target_year", nullable = false)
    private Integer targetYear; // 대상 학년 (0이면 전체)

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
