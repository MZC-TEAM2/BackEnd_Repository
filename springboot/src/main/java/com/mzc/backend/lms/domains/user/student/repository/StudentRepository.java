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

    /**
     * 학번 접두사로 시작하는 마지막 학생 조회 (학번 생성용)
     */
    Optional<Student> findTopByStudentNumberStartingWithOrderByStudentNumberDesc(String prefix);

    // ==================== View Service용 조인 쿼리 ====================

    /**
     * 학번으로 학생 정보와 연관 데이터 조회 (Fetch Join)
     * User, UserProfile, UserContact, Department 정보를 함께 조회
     */
    @Query("""
        SELECT s FROM Student s
        JOIN FETCH s.user u
        WHERE s.studentNumber = :studentNumber
        """)
    Optional<Student> findByStudentNumberWithUser(@Param("studentNumber") String studentNumber);

    /**
     * User ID로 학생 정보와 연관 데이터 조회
     */
    @Query("""
        SELECT s FROM Student s
        JOIN FETCH s.user u
        WHERE s.userId = :userId
        """)
    Optional<Student> findByUserIdWithUser(@Param("userId") Long userId);

    /**
     * 여러 학번으로 학생 정보 일괄 조회
     */
    @Query("""
        SELECT s FROM Student s
        JOIN FETCH s.user u
        WHERE s.studentNumber IN :studentNumbers
        """)
    List<Student> findByStudentNumbersWithUser(@Param("studentNumbers") List<String> studentNumbers);

    /**
     * 여러 User ID로 학생 정보 일괄 조회
     */
    @Query("""
        SELECT s FROM Student s
        JOIN FETCH s.user u
        WHERE s.userId IN :userIds
        """)
    List<Student> findByUserIdsWithUser(@Param("userIds") List<Long> userIds);

    /**
     * 학생 전체 정보 조회를 위한 Native Query
     * 모든 관련 테이블을 조인하여 한 번에 조회
     */
    @Query(value = """
        SELECT s.user_id, s.student_number, s.grade, s.admission_year,
               u.email,
               p.name as profile_name,
               c.contact_value as phone_number,
               d.id as department_id, d.name as department_name,
               col.id as college_id, col.name as college_name,
               sd.enrollment_date, sd.is_active as dept_active,
               img.image_url as profile_image_url
        FROM students s
        INNER JOIN users u ON s.user_id = u.id
        LEFT JOIN user_profiles p ON u.id = p.user_id
        LEFT JOIN user_contacts c ON u.id = c.user_id AND c.contact_type = 'MOBILE' AND c.is_primary = true
        LEFT JOIN student_departments sd ON s.user_id = sd.student_id AND sd.is_active = true
        LEFT JOIN departments d ON sd.department_id = d.id
        LEFT JOIN colleges col ON d.college_id = col.id
        LEFT JOIN user_profile_images img ON u.id = img.user_id AND img.is_current = true
        WHERE s.student_number = :studentNumber
        """, nativeQuery = true)
    Object[] findStudentFullInfoByStudentNumber(@Param("studentNumber") String studentNumber);

    /**
     * 여러 학생의 전체 정보 조회를 위한 Native Query
     */
    @Query(value = """
        SELECT s.user_id, s.student_number, s.grade, s.admission_year,
               u.email,
               p.name as profile_name,
               c.contact_value as phone_number,
               d.id as department_id, d.name as department_name,
               col.id as college_id, col.name as college_name,
               sd.enrollment_date, sd.is_active as dept_active,
               img.image_url as profile_image_url
        FROM students s
        INNER JOIN users u ON s.user_id = u.id
        LEFT JOIN user_profiles p ON u.id = p.user_id
        LEFT JOIN user_contacts c ON u.id = c.user_id AND c.contact_type = 'MOBILE' AND c.is_primary = true
        LEFT JOIN student_departments sd ON s.user_id = sd.student_id AND sd.is_active = true
        LEFT JOIN departments d ON sd.department_id = d.id
        LEFT JOIN colleges col ON d.college_id = col.id
        LEFT JOIN user_profile_images img ON u.id = img.user_id AND img.is_current = true
        WHERE s.student_number IN :studentNumbers
        """, nativeQuery = true)
    List<Object[]> findStudentsFullInfoByStudentNumbers(@Param("studentNumbers") List<String> studentNumbers);
}