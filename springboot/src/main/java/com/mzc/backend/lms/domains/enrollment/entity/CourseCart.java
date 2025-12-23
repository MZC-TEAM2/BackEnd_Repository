package com.mzc.backend.lms.domains.enrollment.entity;

import com.mzc.backend.lms.domains.course.course.entity.Course;
import com.mzc.backend.lms.domains.user.student.entity.Student;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


/*
    CourseCart 엔티티
    course_carts 테이블과 매핑
*/

@Entity
@Table(name = "course_carts")
@Getter
@NoArgsConstructor
@AllArgsConstructor
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
