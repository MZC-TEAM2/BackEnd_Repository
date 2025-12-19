package com.mzc.backend.lms.domains.course.course.repository;

import com.mzc.backend.lms.domains.course.course.entity.CourseWeek;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseWeekRepository extends JpaRepository<CourseWeek, Long> {
    /**
     * 강의 ID로 강의 주차 목록 조회
     */
    List<CourseWeek> findByCourseId(Long courseId);

    /**
     * 강의 주차 번호로 강의 주차 조회
     */
    Optional<CourseWeek> findByCourseIdAndWeekNumber(Long courseId, Integer weekNumber);

    /**
     * 강의 주차 번호로 강의 주차 존재 여부 확인
     */
    boolean existsByCourseIdAndWeekNumber(Long courseId, Integer weekNumber);

    /**
     * 강의 ID로 강의 주차 개수 조회
     */
    long countByCourseId(Long courseId);
}
