package com.mzc.backend.lms.domains.course.course.repository;

import com.mzc.backend.lms.domains.course.course.entity.CourseSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * 강의 스케줄 Repository
 */

@Repository
public interface CourseScheduleRepository extends JpaRepository<CourseSchedule, Long> {
    /**
     * 강의 ID로 강의 스케줄 조회
     */
    Optional<CourseSchedule> findByCourseId(Long courseId);

    /**
     * 강의 ID로 강의 스케줄 존재 여부 확인
     */
    boolean existsByCourseId(Long courseId);

}
