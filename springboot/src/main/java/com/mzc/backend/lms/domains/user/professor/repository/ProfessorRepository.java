package com.mzc.backend.lms.domains.user.professor.repository;

import com.mzc.backend.lms.domains.user.professor.entity.Professor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 교수 Repository
 */
@Repository
public interface ProfessorRepository extends JpaRepository<Professor, String> {

    /**
     * 교번 존재 여부 확인
     */
    boolean existsById(String professorId);

    /**
     * 임용일자 범위로 교수 목록 조회
     */
    @Query("SELECT p FROM Professor p WHERE p.appointmentDate BETWEEN :startDate AND :endDate")
    List<Professor> findByAppointmentDateBetween(@Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);

    // ==================== View Service용 조인 쿼리 ====================

    /**
     * 교번으로 교수 정보와 연관 데이터 조회 (Fetch Join)
     */
    @Query("""
        SELECT p FROM Professor p
        JOIN FETCH p.user u
        WHERE p.professorId = :professorId
        """)
    Optional<Professor> findByIdWithUser(@Param("professorId") String professorId);

    /**
     * 여러 교번으로 교수 정보 일괄 조회
     */
    @Query("""
        SELECT p FROM Professor p
        JOIN FETCH p.user u
        WHERE p.professorId IN :professorIds
        """)
    List<Professor> findByIdsWithUser(@Param("professorIds") List<String> professorIds);

    /**
     * 교수 전체 정보 조회를 위한 Native Query
     */
    @Query(value = """
        SELECT p.professor_id, u.id as user_id, p.appointment_date,
               u.email,
               prof.name as profile_name,
               pc.mobile_number as phone_number,
               pc.office_number as office_number,
               d.id as department_id, d.department_name as department_name,
               col.id as college_id, col.college_name as college_name,
               pd.start_date as dept_start_date, pd.is_primary as dept_primary,
               img.image_url as profile_image_url
        FROM professors p
        INNER JOIN users u ON p.professor_id = u.id
        LEFT JOIN user_profiles prof ON u.id = prof.user_id
        LEFT JOIN user_primary_contacts pc ON u.id = pc.user_id
        LEFT JOIN professor_departments pd ON p.professor_id = pd.professor_id AND pd.is_primary = true
        LEFT JOIN departments d ON pd.department_id = d.id
        LEFT JOIN colleges col ON d.college_id = col.id
        LEFT JOIN user_profile_images img ON u.id = img.user_id
        WHERE p.professor_id = :professorId
        """, nativeQuery = true)
    Object[] findProfessorFullInfoById(@Param("professorId") String professorId);

    /**
     * 여러 교수의 전체 정보 조회를 위한 Native Query
     */
    @Query(value = """
        SELECT p.professor_id, u.id as user_id, p.appointment_date,
               u.email,
               prof.name as profile_name,
               pc.mobile_number as phone_number,
               pc.office_number as office_number,
               d.id as department_id, d.department_name as department_name,
               col.id as college_id, col.college_name as college_name,
               pd.start_date as dept_start_date, pd.is_primary as dept_primary,
               img.image_url as profile_image_url
        FROM professors p
        INNER JOIN users u ON p.professor_id = u.id
        LEFT JOIN user_profiles prof ON u.id = prof.user_id
        LEFT JOIN user_primary_contacts pc ON u.id = pc.user_id
        LEFT JOIN professor_departments pd ON p.professor_id = pd.professor_id AND pd.is_primary = true
        LEFT JOIN departments d ON pd.department_id = d.id
        LEFT JOIN colleges col ON d.college_id = col.id
        LEFT JOIN user_profile_images img ON u.id = img.user_id
        WHERE p.professor_id IN :professorIds
        """, nativeQuery = true)
    List<Object[]> findProfessorsFullInfoByIds(@Param("professorIds") List<String> professorIds);
}