package com.mzc.backend.lms.domains.course.grade.repository;

import com.mzc.backend.lms.domains.course.grade.entity.Grade;
import com.mzc.backend.lms.domains.course.grade.enums.GradeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {
	Optional<Grade> findByCourseIdAndStudentId(Long courseId, Long studentId);
	
	List<Grade> findByStudentIdAndStatusOrderByAcademicTermIdDescCourseIdAsc(Long studentId, GradeStatus status);
	
	List<Grade> findByStudentIdAndAcademicTermIdAndStatusOrderByCourseIdAsc(Long studentId, Long academicTermId, GradeStatus status);
	
	List<Grade> findByCourseIdAndStudentIdIn(Long courseId, List<Long> studentIds);
	
	List<Grade> findByCourseIdAndStudentIdInAndStatus(Long courseId, List<Long> studentIds, GradeStatus status);
	
	/**
	 * 선수과목 판정용: 특정 과목(subject)들에 대해, 대상 학기 이전 학기에서 받은 최종 등급 조회
	 * - grades.status=PUBLISHED만 대상으로 함
	 */
	@Query("""
			SELECT c.subject.id as subjectId, g.finalGrade as finalGrade
			FROM Grade g
			JOIN com.mzc.backend.lms.domains.course.course.entity.Course c ON c.id = g.courseId
			JOIN com.mzc.backend.lms.domains.academy.entity.AcademicTerm t ON t.id = g.academicTermId
			WHERE g.studentId = :studentId
			  AND c.subject.id IN :subjectIds
			  AND t.startDate < :targetStartDate
			  AND g.status = com.mzc.backend.lms.domains.course.grade.enums.GradeStatus.PUBLISHED
			  AND g.finalGrade IS NOT NULL
			""")
	List<SubjectFinalGradeView> findPriorSubjectFinalGrades(
			@Param("studentId") Long studentId,
			@Param("subjectIds") List<Long> subjectIds,
			@Param("targetStartDate") LocalDate targetStartDate
	);
	
	interface SubjectFinalGradeView {
		Long getSubjectId();
		
		String getFinalGrade();
	}
}


