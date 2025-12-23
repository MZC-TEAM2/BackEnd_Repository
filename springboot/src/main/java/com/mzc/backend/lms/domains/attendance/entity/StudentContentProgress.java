package com.mzc.backend.lms.domains.attendance.entity;

import com.mzc.backend.lms.domains.course.course.entity.WeekContent;
import com.mzc.backend.lms.domains.user.student.entity.Student;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 학생 콘텐츠 진행 상황 엔티티
 * student_content_progress 테이블과 매핑 (Video Streaming Server에서 관리)
 */
@Entity
@Table(name = "student_content_progress",
		uniqueConstraints = @UniqueConstraint(columnNames = {"content_id", "student_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class StudentContentProgress {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "content_id", nullable = false)
	private WeekContent content;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "student_id", nullable = false)
	private Student student;
	
	@Column(name = "is_completed", nullable = false)
	private Boolean isCompleted;
	
	@Column(name = "progress_percentage", nullable = false)
	private Integer progressPercentage;
	
	@Column(name = "last_position_seconds")
	private Integer lastPositionSeconds;
	
	@Column(name = "completed_at")
	private LocalDateTime completedAt;
	
	@Column(name = "first_accessed_at")
	private LocalDateTime firstAccessedAt;
	
	@Column(name = "last_accessed_at")
	private LocalDateTime lastAccessedAt;
	
	@Column(name = "access_count", nullable = false)
	private Integer accessCount;
	
	/**
	 * 콘텐츠 완료 여부 확인
	 */
	public boolean isVideoCompleted() {
		return Boolean.TRUE.equals(this.isCompleted);
	}
	
	/**
	 * 콘텐츠 ID 반환 (편의 메서드)
	 */
	public Long getContentId() {
		return this.content != null ? this.content.getId() : null;
	}
	
	/**
	 * 학생 ID 반환 (편의 메서드)
	 */
	public Long getStudentId() {
		return this.student != null ? this.student.getStudentId() : null;
	}
}
