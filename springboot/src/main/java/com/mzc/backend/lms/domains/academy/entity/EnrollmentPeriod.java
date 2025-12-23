package com.mzc.backend.lms.domains.academy.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "enrollment_periods")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentPeriod {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id; // 수강신청 기간 식별자
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "term_id", nullable = false)  // academic_term_id → term_id로 변경
	private AcademicTerm academicTerm;
	
	@Column(name = "period_name", length = 50, nullable = false)
	private String periodName; // 기간명 (예: 1차 수강신청, 정정기간, 강의등록기간)
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "period_type_id", nullable = false)
	private PeriodType periodType; // 기간 타입
	
	@Column(name = "start_datetime", nullable = false)  // start_date → start_datetime으로 변경
	private LocalDateTime startDatetime; // 수강신청 시작일시
	
	@Column(name = "end_datetime", nullable = false)  // end_date → end_datetime으로 변경
	private LocalDateTime endDatetime; // 수강신청 종료일시
	
	@Column(name = "target_year", nullable = false)
	private Integer targetYear; // 대상 학년 (0이면 전체)
	
	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;
	
	/**
	 * 현재 시간이 기간 내에 있는지 확인
	 */
	public boolean isActive(LocalDateTime now) {
		return !now.isBefore(startDatetime) && !now.isAfter(endDatetime);
	}
	
	/**
	 * 수강신청 기간인지 확인
	 */
	public boolean isEnrollmentPeriod() {
		return periodType != null && "ENROLLMENT".equals(periodType.getTypeCode());
	}
	
	/**
	 * 강의 등록 기간인지 확인
	 */
	public boolean isCourseRegistrationPeriod() {
		return periodType != null && "COURSE_REGISTRATION".equals(periodType.getTypeCode());
	}
	
	/**
	 * 정정 기간인지 확인
	 */
	public boolean isAdjustmentPeriod() {
		return periodType != null && "ADJUSTMENT".equals(periodType.getTypeCode());
	}
	
	/**
	 * 수강철회 기간인지 확인
	 */
	public boolean isCancellationPeriod() {
		return periodType != null && "CANCELLATION".equals(periodType.getTypeCode());
	}
}
