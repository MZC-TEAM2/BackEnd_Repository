package com.mzc.backend.lms.domains.enrollment.repository;

import com.mzc.backend.lms.domains.enrollment.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
