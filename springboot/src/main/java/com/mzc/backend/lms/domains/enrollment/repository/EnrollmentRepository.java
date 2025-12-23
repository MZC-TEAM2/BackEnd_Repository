package com.mzc.backend.lms.domains.enrollment.repository;

import com.mzc.backend.lms.domains.academy.entity.AcademicTerm;
import com.mzc.backend.lms.domains.enrollment.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 수강신청 Repository
 */

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
	/**
	 * 학생 ID와 강의 ID로 수강신청 조회
	 */
	Optional<Enrollment> findByStudentIdAndCourseId(Long studentId, Long courseId);
	
	/**
	 * 학생 ID와 강의 ID로 수강신청 존재 여부 확인
	 */
	boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);
	
	/**
	 * 학생 ID로 수강신청 목록 조회
	 */
	List<Enrollment> findByStudentId(Long studentId);
	
	/**
	 * 강의 ID로 수강신청 목록 조회
	 */
	List<Enrollment> findByCourseId(Long courseId);
	
	/**
	 * N+1 방지: 강의별 수강신청 목록 + 학생 함께 로딩
	 */
	@Query("SELECT e FROM Enrollment e JOIN FETCH e.student s WHERE e.course.id = :courseId")
	List<Enrollment> findByCourseIdWithStudent(@Param("courseId") Long courseId);
	
	/**
	 * 학생이 수강한 학기 목록(중복 제거)
	 */
	@Query("SELECT DISTINCT e.course.academicTerm FROM Enrollment e WHERE e.student.studentId = :studentId")
	List<AcademicTerm> findDistinctAcademicTermsByStudentId(@Param("studentId") Long studentId);
	
	/**
	 * 강의 ID로 수강생 ID 목록 조회
	 */
	@Query("SELECT e.student.studentId FROM Enrollment e WHERE e.course.id = :courseId")
	List<Long> findStudentIdsByCourseId(@Param("courseId") Long courseId);
}
