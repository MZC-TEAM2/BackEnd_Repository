package com.mzc.backend.lms.domains.attendance.entity;

import com.mzc.backend.lms.domains.course.course.entity.Course;
import com.mzc.backend.lms.domains.course.course.entity.CourseWeek;
import com.mzc.backend.lms.domains.user.student.entity.Student;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 주차별 출석 엔티티
 * 학생이 해당 주차의 모든 VIDEO를 완료하면 출석으로 인정
 * 완료 시점 잠금: 출석 완료된 주차는 이후 콘텐츠 변경에 영향받지 않음
 */
@Entity
@Table(name = "week_attendance",
		uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "week_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class WeekAttendance {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "student_id", nullable = false)
	private Student student;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "week_id", nullable = false)
	private CourseWeek week;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "course_id", nullable = false)
	private Course course;
	
	@Column(name = "is_completed", nullable = false)
	@Builder.Default
	private Boolean isCompleted = false;
	
	@Column(name = "completed_video_count", nullable = false)
	@Builder.Default
	private Integer completedVideoCount = 0;
	
	@Column(name = "total_video_count", nullable = false)
	private Integer totalVideoCount;
	
	@Column(name = "first_accessed_at")
	private LocalDateTime firstAccessedAt;
	
	@Column(name = "completed_at")
	private LocalDateTime completedAt;
	
	/**
	 * 출석 레코드 생성 팩토리 메서드
	 */
	public static WeekAttendance create(Student student, CourseWeek week, Course course, int totalVideoCount) {
		return WeekAttendance.builder()
				.student(student)
				.week(week)
				.course(course)
				.isCompleted(false)
				.completedVideoCount(0)
				.totalVideoCount(totalVideoCount)
				.firstAccessedAt(LocalDateTime.now())
				.build();
	}
	
	/**
	 * VIDEO 완료 시 진행 상황 업데이트
	 * 완료 시점 잠금: 이미 완료된 출석은 변경하지 않음
	 */
	public void updateProgress(int completedVideoCount) {
		if (Boolean.TRUE.equals(this.isCompleted)) {
			return;
		}
		
		this.completedVideoCount = completedVideoCount;
		
		if (completedVideoCount >= this.totalVideoCount) {
			markAsCompleted();
		}
	}
	
	/**
	 * 출석 완료 처리
	 */
	private void markAsCompleted() {
		this.isCompleted = true;
		this.completedAt = LocalDateTime.now();
	}
	
	/**
	 * 출석 완료 여부 확인
	 */
	public boolean isAttendanceCompleted() {
		return Boolean.TRUE.equals(this.isCompleted);
	}
	
	/**
	 * 진행률 계산 (0 ~ 100)
	 */
	public int getProgressPercentage() {
		if (this.totalVideoCount == 0) {
			return 100;
		}
		return (int) ((this.completedVideoCount * 100.0) / this.totalVideoCount);
	}
	
	/**
	 * 학생 ID 반환 (편의 메서드)
	 */
	public Long getStudentId() {
		return this.student != null ? this.student.getStudentId() : null;
	}
	
	/**
	 * 주차 ID 반환 (편의 메서드)
	 */
	public Long getWeekId() {
		return this.week != null ? this.week.getId() : null;
	}
	
	/**
	 * 강의 ID 반환 (편의 메서드)
	 */
	public Long getCourseId() {
		return this.course != null ? this.course.getId() : null;
	}
}
