package com.mzc.backend.lms.domains.user.student.repository;

import com.mzc.backend.lms.domains.user.student.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 학생 Repository
 */
@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    /**
     * 학번으로 학생 조회
     */
    Optional<Student> findByStudentNumber(String studentNumber);

    /**
     * 학번 존재 여부 확인
     */
    boolean existsByStudentNumber(String studentNumber);

    /**
     * 입학년도로 학생 목록 조회
     */
    List<Student> findByAdmissionYear(Integer admissionYear);

    /**
     * 사용자 ID로 학생 정보 조회
     */
    @Query("SELECT s FROM Student s WHERE s.userId = :userId")
    Optional<Student> findByUserId(@Param("userId") Long userId);

    /**
     * 학번 패턴으로 학생 검색
     */
    @Query("SELECT s FROM Student s WHERE s.studentNumber LIKE :pattern")
    List<Student> findByStudentNumberPattern(@Param("pattern") String pattern);
}