package com.mzc.backend.lms.domains.course.course.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 강의별 평가 비율(가중치) 정책
 * - 시험(exam)은 중간(midterm) + 기말(finalExam)로 구성
 * - 퀴즈(quiz)는 별도 항목
 */
@Entity
@Table(name = "course_grading_policies")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseGradingPolicy {
	
	@Id
	@Column(name = "course_id")
	private Long courseId;
	
	@OneToOne(fetch = FetchType.LAZY)
	@MapsId
	@JoinColumn(name = "course_id")
	private Course course;
	
	@Column(name = "midterm", nullable = false)
	private Integer midterm;
	
	@Column(name = "final_exam", nullable = false)
	private Integer finalExam;
	
	@Column(name = "quiz", nullable = false)
	private Integer quiz;
	
	@Column(name = "assignment", nullable = false)
	private Integer assignment;
	
	@Column(name = "attendance", nullable = false)
	private Integer attendance;
	
	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;
	
	@UpdateTimestamp
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;
	
	@Builder
	private CourseGradingPolicy(Course course,
	                            Integer midterm,
	                            Integer finalExam,
	                            Integer quiz,
	                            Integer assignment,
	                            Integer attendance) {
		this.course = course;
		// 주의:
		// course_grading_policies는 course_id(=courses.id)를 PK로 사용하는 @MapsId 엔티티.
		// 여기서 courseId를 미리 세팅하면 Spring Data JPA save()가 "기존 엔티티"로 오인하여 merge(update)를 수행할 수 있고,
		// 아직 row가 없는 생성 시점에는 0 row update → "Row was updated or deleted..." 예외가 발생할 수 있다.
		// 따라서 courseId는 @MapsId가 persist 시점에 채우도록 두고(null 유지), course만 설정한다.
		this.midterm = midterm;
		this.finalExam = finalExam;
		this.quiz = quiz;
		this.assignment = assignment;
		this.attendance = attendance;
	}
}


