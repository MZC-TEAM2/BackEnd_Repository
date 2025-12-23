package com.mzc.backend.lms.domains.attendance.repository;

import com.mzc.backend.lms.domains.attendance.entity.StudentContentProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 학생 콘텐츠 진행 상황 Repository
 */
@Repository
public interface StudentContentProgressRepository extends JpaRepository<StudentContentProgress, Long> {
	
	/**
	 * 학생 ID와 콘텐츠 ID로 진행 상황 조회
	 */
	Optional<StudentContentProgress> findByStudentStudentIdAndContent_Id(Long studentId, Long contentId);
	
	/**
	 * 학생 ID와 콘텐츠 ID 목록으로 진행 상황 조회
	 */
	List<StudentContentProgress> findByStudentStudentIdAndContent_IdIn(Long studentId, List<Long> contentIds);
	
	/**
	 * 학생 ID와 콘텐츠 ID 목록 중 완료된 것만 조회
	 */
	@Query("SELECT scp FROM StudentContentProgress scp " +
			"WHERE scp.student.studentId = :studentId " +
			"AND scp.content.id IN :contentIds " +
			"AND scp.isCompleted = true")
	List<StudentContentProgress> findCompletedByStudentAndContentIds(
			@Param("studentId") Long studentId,
			@Param("contentIds") List<Long> contentIds);
	
	/**
	 * 학생이 특정 주차에서 완료한 VIDEO 콘텐츠 수 조회
	 */
	@Query("SELECT COUNT(scp) FROM StudentContentProgress scp " +
			"JOIN scp.content wc " +
			"WHERE scp.student.studentId = :studentId " +
			"AND wc.week.id = :weekId " +
			"AND wc.contentType = 'VIDEO' " +
			"AND scp.isCompleted = true")
	int countCompletedVideosByStudentAndWeek(
			@Param("studentId") Long studentId,
			@Param("weekId") Long weekId);
	
	/**
	 * 학생의 특정 강의 전체 진행 상황 조회
	 */
	@Query("SELECT scp FROM StudentContentProgress scp " +
			"JOIN scp.content wc " +
			"JOIN wc.week cw " +
			"WHERE scp.student.studentId = :studentId " +
			"AND cw.course.id = :courseId")
	List<StudentContentProgress> findByStudentAndCourse(
			@Param("studentId") Long studentId,
			@Param("courseId") Long courseId);
}
