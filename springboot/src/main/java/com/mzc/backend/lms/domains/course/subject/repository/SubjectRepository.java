package com.mzc.backend.lms.domains.course.subject.repository;

import com.mzc.backend.lms.domains.course.subject.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import com.mzc.backend.lms.domains.user.organization.entity.Department;
import com.mzc.backend.lms.domains.course.course.entity.CourseType;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {
    /**
     * 학과로 과목 목록 조회
     */
    List<Subject> findByDepartment(Department department);

    /**
     * 학과 ID로 과목 목록 조회
     */
    List<Subject> findByDepartmentId(Long departmentId);

    /**
     * 강의 유형으로 과목 목록 조회
     */
    List<Subject> findByCourseType(CourseType courseType);

    /**
     * 강의 유형 ID로 과목 목록 조회
     */
    List<Subject> findByCourseTypeId(Long courseTypeId);

    /**
     * 학점으로 과목 목록 조회
     */
    List<Subject> findByCredits(Integer credits);
}
