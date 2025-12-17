package com.mzc.backend.lms.domains.course.course.repository;

import com.mzc.backend.lms.domains.course.course.entity.Course;
import com.mzc.backend.lms.domains.course.course.entity.CourseType;
import com.mzc.backend.lms.domains.user.organization.entity.Department;
import com.mzc.backend.lms.domains.user.professor.entity.Professor;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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
     * Subject를 통해 CourseType에 접근
     */
    List<Course> findBySubjectCourseType(CourseType courseType);
    
    /**
     * 강의 유형 코드로 검색 (예: 0: 전공필수, 1: 전공선택, 2: 교양필수, 3: 교양선택)
     * Subject를 통해 CourseType에 접근
     */
    List<Course> findBySubjectCourseTypeTypeCode(int typeCode);

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

    /**
     * 비관적 락을 사용하여 강의 조회 (수강신청 시 동시성 제어용)
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Course c WHERE c.id = :id")
    Optional<Course> findByIdWithLock(@Param("id") Long id);

    /**
     * 교수 ID와 학기 ID로 강의 목록 조회
     */
    List<Course> findByProfessorProfessorIdAndAcademicTermId(Long professorId, Long academicTermId);

    /**
     * Subject, AcademicTerm, SectionNumber로 강의 존재 여부 확인 (중복 체크)
     */
    @Query("SELECT COUNT(c) > 0 FROM Course c " +
           "WHERE c.subject.id = :subjectId " +
           "AND c.academicTerm.id = :academicTermId " +
           "AND c.sectionNumber = :sectionNumber")
    boolean existsBySubjectIdAndAcademicTermIdAndSectionNumber(
            @Param("subjectId") Long subjectId,
            @Param("academicTermId") Long academicTermId,
            @Param("sectionNumber") String sectionNumber);

    /**
     * 교수가 특정 시간에 다른 강의를 개설했는지 확인 (시간표 충돌 체크)
     */
    @Query("SELECT COUNT(c) > 0 FROM Course c " +
           "JOIN c.schedules s " +
           "WHERE c.professor.professorId = :professorId " +
           "AND c.academicTerm.id = :academicTermId " +
           "AND s.dayOfWeek = :dayOfWeek " +
           "AND ((s.startTime <= :startTime AND s.endTime > :startTime) " +
           "OR (s.startTime < :endTime AND s.endTime >= :endTime) " +
           "OR (s.startTime >= :startTime AND s.endTime <= :endTime))")
    boolean existsByProfessorAndTimeConflict(
            @Param("professorId") Long professorId,
            @Param("academicTermId") Long academicTermId,
            @Param("dayOfWeek") java.time.DayOfWeek dayOfWeek,
            @Param("startTime") java.time.LocalTime startTime,
            @Param("endTime") java.time.LocalTime endTime);
}