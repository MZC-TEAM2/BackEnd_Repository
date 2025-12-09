package com.mzc.backend.lms.domains.course.course.repository;

import com.mzc.backend.lms.domains.course.course.entity.Course;
import com.mzc.backend.lms.domains.course.course.entity.CourseType;
import com.mzc.backend.lms.domains.user.organization.entity.Department;
import com.mzc.backend.lms.domains.user.professor.entity.Professor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 강의 Repository
 * 학과, 이수구분, 학점, 과목명/코드, 교수명으로 검색
 */
@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    // ==================== 학과 필터 ====================
    
    /**
     * 학과로 강의 목록 조회
     */
    List<Course> findBySubjectDepartment(Department department);
    
    /**
     * 학과 ID로 강의 목록 조회
     */
    List<Course> findBySubjectDepartmentId(Long departmentId);

    // ==================== 이수구분 필터 ====================
    
    /**
     * 강의 유형으로 강의 목록 조회
     */
    List<Course> findByCourseType(CourseType courseType);
    
    /**
     * 강의 유형 코드로 검색 (예: "MAJOR_REQ", "GEN_ELEC")
     */
    List<Course> findByCourseTypeTypeCode(int typeCode);

    // ==================== 학점 필터 ====================
    
    /**
     * 학점으로 강의 목록 조회
     */
    List<Course> findBySubjectCredits(Integer credits);

    // ==================== 검색창 (과목명, 과목코드, 교수명) ====================
    
    /**
     * 과목명으로 검색 (부분 일치)
     */
    List<Course> findBySubjectSubjectNameContaining(String subjectName);
    
    /**
     * 과목 코드로 검색
     */
    List<Course> findBySubjectSubjectCode(String subjectCode);
    
    /**
     * 교수로 강의 목록 조회
     */
    List<Course> findByProfessor(Professor professor);
    
    /**
     * 교수 ID로 강의 목록 조회
     */
    List<Course> findByProfessorProfessorId(Long professorId);

    // ==================== 학점 필터 (필요시 사용) ====================

    /**
     * 학기 ID로 강의 목록 조회
     */
    List<Course> findByAcademicTermId(Long academicTermId);
}