package com.mzc.backend.lms.domains.user.student.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 학번 시퀀스 관리 엔티티
 * 년도/단과대학/학과별로 마지막 순번을 관리
 */
@Entity
@Table(name = "student_number_sequences",
		uniqueConstraints = {
				@UniqueConstraint(columnNames = {"\"year\"", "college_id", "department_id"})
		},
		indexes = {
				@Index(name = "idx_student_seq_year_college_dept",
						columnList = "\"year\", college_id, department_id")
		})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudentNumberSequence {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "\"year\"", nullable = false)
	private Integer year;
	
	@Column(name = "college_id", nullable = false)
	private Long collegeId;
	
	@Column(name = "department_id", nullable = false)
	private Long departmentId;
	
	@Column(name = "last_sequence", nullable = false)
	private Integer lastSequence;
	
	@Version
	private Long version;  // 동시성 제어를 위한 버전
	
	@Builder
	private StudentNumberSequence(Integer year, Long collegeId, Long departmentId, Integer lastSequence) {
		this.year = year;
		this.collegeId = collegeId;
		this.departmentId = departmentId;
		this.lastSequence = lastSequence;
	}
	
	/**
	 * 시퀀스 생성
	 */
	public static StudentNumberSequence create(Integer year, Long collegeId, Long departmentId) {
		return StudentNumberSequence.builder()
				.year(year)
				.collegeId(collegeId)
				.departmentId(departmentId)
				.lastSequence(0)
				.build();
	}
	
	/**
	 * 초기 시퀀스 값을 지정하여 생성 (기존 데이터 고려)
	 */
	public static StudentNumberSequence createWithInitialSequence(
			Integer year, Long collegeId, Long departmentId, Integer initialSequence) {
		return StudentNumberSequence.builder()
				.year(year)
				.collegeId(collegeId)
				.departmentId(departmentId)
				.lastSequence(initialSequence)
				.build();
	}
	
	/**
	 * 다음 시퀀스 번호 반환 및 증가
	 */
	public Integer getNextSequence() {
		return ++this.lastSequence;
	}
}
