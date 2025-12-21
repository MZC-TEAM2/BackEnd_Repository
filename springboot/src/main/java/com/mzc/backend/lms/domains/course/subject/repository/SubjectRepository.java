package com.mzc.backend.lms.domains.course.subject.repository;

import com.mzc.backend.lms.domains.course.subject.entity.Subject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    /**
     * 과목 코드로 과목 조회
     */
    java.util.Optional<Subject> findBySubjectCode(String subjectCode);

    /**
     * 과목 코드 존재 여부 확인
     */
    boolean existsBySubjectCode(String subjectCode);

    /**
     * 과목 ID로 상세 정보 조회 (Fetch Join)
     */
    @Query("""
        SELECT s FROM Subject s
        LEFT JOIN FETCH s.department d
        LEFT JOIN FETCH d.college
        LEFT JOIN FETCH s.courseType
        LEFT JOIN FETCH s.prerequisites
        WHERE s.id = :subjectId
        """)
    java.util.Optional<Subject> findByIdWithDetails(@Param("subjectId") Long subjectId);

    /**
     * 과목 검색 - 과목명 또는 과목코드로 검색 (페이징 지원)
     */
    @Query("""
        SELECT s FROM Subject s
        JOIN FETCH s.department d
        JOIN FETCH s.courseType ct
        WHERE (LOWER(s.subjectName) LIKE LOWER(CONCAT('%', :query, '%'))
           OR LOWER(s.subjectCode) LIKE LOWER(CONCAT('%', :query, '%')))
        ORDER BY s.subjectCode
        """)
    Page<Subject> searchSubjects(@Param("query") String query, Pageable pageable);

    /**
     * 학과 ID로 과목 페이지 조회 (검색어, 필터 포함)
     */
    @Query("""
        SELECT s FROM Subject s
        JOIN FETCH s.department d
        JOIN FETCH d.college
        JOIN FETCH s.courseType ct
        WHERE (:departmentId IS NULL OR s.department.id = :departmentId)
          AND (:keyword IS NULL OR LOWER(s.subjectName) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(s.subjectCode) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:courseTypeId IS NULL OR s.courseType.id = :courseTypeId)
          AND (:credits IS NULL OR s.credits = :credits)
        """)
    Page<Subject> findSubjectsWithFilters(
        @Param("departmentId") Long departmentId,
        @Param("keyword") String keyword,
        @Param("courseTypeId") Long courseTypeId,
        @Param("credits") Integer credits,
        Pageable pageable
    );

    /**
     * 학과와 과목코드로 중복 체크
     */
    boolean existsByDepartmentIdAndSubjectCode(Long departmentId, String subjectCode);
}
