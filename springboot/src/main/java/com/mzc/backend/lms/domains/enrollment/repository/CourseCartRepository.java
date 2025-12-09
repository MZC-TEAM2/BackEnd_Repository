package com.mzc.backend.lms.domains.enrollment.repository;

import com.mzc.backend.lms.domains.enrollment.entity.CourseCart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 코스 카트 Repository
 */

@Repository
public interface CourseCartRepository extends JpaRepository<CourseCart, Long> {
    /**
     * 학생 ID와 강의 ID로 코스 카트 조회
     */
    Optional<CourseCart> findByStudentIdAndCourseId(Long studentId, Long courseId);
    /**
     * 학생 ID와 강의 ID로 코스 카트 존재 여부 확인
     */
    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);

    /**
     * 학생 ID로 코스 카트 목록 조회
     */
    List<CourseCart> findByStudentId(Long studentId);

    /**
     * 강의 ID로 코스 카트 목록 조회
     */
    List<CourseCart> findByCourseId(Long courseId);
}
