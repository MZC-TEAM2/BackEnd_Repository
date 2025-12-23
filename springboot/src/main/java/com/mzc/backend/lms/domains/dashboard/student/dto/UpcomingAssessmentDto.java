package com.mzc.backend.lms.domains.dashboard.student.dto;

import lombok.Getter;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 예정된 시험/퀴즈 응답 DTO
 */
@Getter
public class UpcomingAssessmentDto {

	private final Long assessmentId;
	private final Long postId;
	private final String title;
	private final String assessmentType; // MIDTERM, FINAL, QUIZ
	private final Long courseId;
	private final String courseName;
	private final String subjectName;
	private final LocalDateTime startAt;
	private final Integer durationMinutes;
	private final Boolean isOnline;
	private final String location;

	/**
	 * JPQL 생성자
	 */
	public UpcomingAssessmentDto(
			Long assessmentId,
			Long postId,
			String title,
			String assessmentType,
			Long courseId,
			String courseName,
			String subjectName,
			LocalDateTime startAt,
			Integer durationMinutes,
			Boolean isOnline,
			String location
	) {
		this.assessmentId = assessmentId;
		this.postId = postId;
		this.title = title;
		this.assessmentType = assessmentType;
		this.courseId = courseId;
		this.courseName = courseName;
		this.subjectName = subjectName;
		this.startAt = startAt;
		this.durationMinutes = durationMinutes;
		this.isOnline = isOnline;
		this.location = location;
	}

	/**
	 * 남은 일수 계산
	 */
	public Long getDaysRemaining() {
		return ChronoUnit.DAYS.between(LocalDateTime.now(), startAt);
	}

	/**
	 * 시험 종료 시간 계산
	 */
	public LocalDateTime getEndAt() {
		return startAt.plusMinutes(durationMinutes);
	}
}
