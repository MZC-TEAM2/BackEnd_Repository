package com.mzc.backend.lms.domains.course.course.entity;

import com.mzc.backend.lms.domains.academy.entity.AcademicTerm;
import com.mzc.backend.lms.domains.course.subject.entity.Subject;
import com.mzc.backend.lms.domains.user.professor.entity.Professor;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/*
  강의 엔티티
  courses 테이블과 매핑
*/

@Entity
@Setter
@Table(name = "courses")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Course {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "subject_id", nullable = false)
	private Subject subject;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "professor_id", nullable = false)
	private Professor professor;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "academic_term_id", nullable = false)
	private AcademicTerm academicTerm;
	
	@Column(name = "section_number", nullable = false)
	private String sectionNumber;
	
	@Column(name = "max_students", nullable = false)
	private Integer maxStudents;
	
	@Column(name = "current_students", nullable = false)
	private Integer currentStudents;
	
	@Column(name = "description", columnDefinition = "TEXT")
	private String description;  // 강의 설명 (분반별)
	
	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;
	
	// Course -> CourseSchedule (1:N)
	@OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<CourseSchedule> schedules = new ArrayList<>();
	
	// 편의 메서드
	public void addSchedule(CourseSchedule schedule) {
		schedules.add(schedule);
		schedule.setCourse(this);
	}
	
	public void removeSchedule(CourseSchedule schedule) {
		schedules.remove(schedule);
		schedule.setCourse(null);
	}
	
}
