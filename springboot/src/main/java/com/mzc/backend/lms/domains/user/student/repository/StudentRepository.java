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
     * 학번 존재 여부 확인
     */
    boolean existsById(Long studentId);

    /**
     * 입학년도로 학생 목록 조회
     */
    List<Student> findByAdmissionYear(Integer admissionYear);

    /**
     * 학번 패턴으로 학생 검색
     */
    @Query("SELECT s FROM Student s WHERE CAST(s.studentId AS string) LIKE :pattern")
    List<Student> findByStudentIdPattern(@Param("pattern") String pattern);

    // ==================== View Service용 조인 쿼리 ====================

    /**
     * 학번으로 학생 정보와 연관 데이터 조회 (Fetch Join)
     */
    @Query("""
        SELECT s FROM Student s
        JOIN FETCH s.user u
        WHERE s.studentId = :studentId
        """)
    Optional<Student> findByIdWithUser(@Param("studentId") Long studentId);

    /**
     * 여러 학번으로 학생 정보 일괄 조회
     */
    @Query("""
        SELECT s FROM Student s
        JOIN FETCH s.user u
        WHERE s.studentId IN :studentIds
        """)
    List<Student> findByIdsWithUser(@Param("studentIds") List<Long> studentIds);

    /**
     * 학생 전체 정보 조회를 위한 Native Query
     */
    @Query(value = """
        SELECT s.student_id, u.id as user_id, s.grade, s.admission_year,
               u.email,
               p.name as profile_name,
               pc.mobile_number as phone_number,
               d.id as department_id, d.department_name as department_name,
               col.id as college_id, col.college_name as college_name,
               sd.enrolled_date, sd.is_primary as dept_primary,
               img.image_url as profile_image_url
        FROM students s
        INNER JOIN users u ON s.student_id = u.id
        LEFT JOIN user_profiles p ON u.id = p.user_id
        LEFT JOIN user_primary_contacts pc ON u.id = pc.user_id
        LEFT JOIN student_departments sd ON s.student_id = sd.student_id AND sd.is_primary = true
        LEFT JOIN departments d ON sd.department_id = d.id
        LEFT JOIN colleges col ON d.college_id = col.id
        LEFT JOIN user_profile_images img ON u.id = img.user_id
        WHERE s.student_id = :studentId
        """, nativeQuery = true)
    Object[] findStudentFullInfoById(@Param("studentId") Long studentId);

    /**
     * 여러 학생의 전체 정보 조회를 위한 Native Query
     */
    @Query(value = """
        SELECT s.student_id, u.id as user_id, s.grade, s.admission_year,
               u.email,
               p.name as profile_name,
               pc.mobile_number as phone_number,
               d.id as department_id, d.department_name as department_name,
               col.id as college_id, col.college_name as college_name,
               sd.enrolled_date, sd.is_primary as dept_primary,
               img.image_url as profile_image_url
        FROM students s
        INNER JOIN users u ON s.student_id = u.id
        LEFT JOIN user_profiles p ON u.id = p.user_id
        LEFT JOIN user_primary_contacts pc ON u.id = pc.user_id
        LEFT JOIN student_departments sd ON s.student_id = sd.student_id AND sd.is_primary = true
        LEFT JOIN departments d ON sd.department_id = d.id
        LEFT JOIN colleges col ON d.college_id = col.id
        LEFT JOIN user_profile_images img ON u.id = img.user_id
        WHERE s.student_id IN :studentIds
        """, nativeQuery = true)
    List<Object[]> findStudentsFullInfoByIds(@Param("studentIds") List<Long> studentIds);
}
