package com.mzc.backend.lms.domains.course.course.repository;

import com.mzc.backend.lms.domains.course.course.entity.CourseTA;
import com.mzc.backend.lms.domains.course.course.entity.Course;
import com.mzc.backend.lms.domains.user.student.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 강의 TA Repository
 */

@Repository
public interface CourseTARepository extends JpaRepository<CourseTA, Long> {
    /**
     * 강의 ID로 강의 TA 조회
     */
    Optional<CourseTA> findByCourseId(Long courseId);

    /**
     * 강의 ID로 강의 TA 존재 여부 확인
     */
    boolean existsByCourseId(Long courseId);

    /**
     * 강의 ID로 강의 TA 목록 조회
     */
    List<CourseTA> findByCourseId(Course course);

    /**
     * 학생 ID로 강의 TA 목록 조회
     */
    List<CourseTA> findByStudentId(Long studentId);

    /**
     * 학생 ID로 강의 TA 목록 조회
     */

    List<CourseTA> findByStudentId(Student student);
}
