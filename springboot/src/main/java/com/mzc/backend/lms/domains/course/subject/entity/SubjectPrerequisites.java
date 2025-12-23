package com.mzc.backend.lms.domains.course.subject.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/*
    선수과목 관계 엔티티
    subject_prerequisites 테이블과 매핑
*/

@Entity
@Table(name = "subject_prerequisites")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubjectPrerequisites {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "subject_id", nullable = false)
	private Subject subject;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "prerequisite_id", nullable = false)
	private Subject prerequisite; // 선수과목
	
	@Column(name = "is_mandatory", nullable = false)
	private Boolean isMandatory; // 필수 여부
	
	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;
	
	// 편의 메서드
	public void setSubject(Subject subject) {
		this.subject = subject;
	}
	
	public void setPrerequisite(Subject prerequisite) {
		this.prerequisite = prerequisite;
	}
}
