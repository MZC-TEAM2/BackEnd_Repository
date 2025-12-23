package com.mzc.backend.lms.domains.course.course.repository;

import com.mzc.backend.lms.domains.course.course.entity.WeekContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 주차별 콘텐츠 Repository
 */
@Repository
public interface WeekContentRepository extends JpaRepository<WeekContent, Long> {
	
	/**
	 * 주차 ID로 콘텐츠 목록 조회 (displayOrder 순으로 정렬)
	 */
	@Query("SELECT wc FROM WeekContent wc WHERE wc.week.id = :weekId ORDER BY wc.displayOrder ASC")
	List<WeekContent> findByWeekIdOrderByDisplayOrder(@Param("weekId") Long weekId);
	
	/**
	 * 주차 ID로 콘텐츠 목록 조회
	 */
	List<WeekContent> findByWeekId(Long weekId);
	
	/**
	 * 강의 ID로 콘텐츠 목록 조회 (주차별로 그룹화)
	 */
	@Query("SELECT wc FROM WeekContent wc " +
			"JOIN wc.week w " +
			"WHERE w.course.id = :courseId " +
			"ORDER BY w.weekNumber ASC, wc.displayOrder ASC")
	List<WeekContent> findByCourseId(@Param("courseId") Long courseId);
	
	/**
	 * 주차 ID와 displayOrder로 콘텐츠 조회
	 */
	Optional<WeekContent> findByWeekIdAndDisplayOrder(Long weekId, Integer displayOrder);
	
	/**
	 * 주차 ID와 displayOrder로 콘텐츠 존재 여부 확인
	 */
	boolean existsByWeekIdAndDisplayOrder(Long weekId, Integer displayOrder);
	
	/**
	 * 주차 ID로 모든 콘텐츠 삭제
	 */
	void deleteByWeekId(Long weekId);
}

