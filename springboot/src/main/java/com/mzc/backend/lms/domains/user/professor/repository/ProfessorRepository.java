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
public interface ProfessorRepository extends JpaRepository<Professor, Long> {

    /**
     * 교번으로 교수 조회
     */
    Optional<Professor> findByProfessorNumber(String professorNumber);

    /**
     * 교번 존재 여부 확인
     */
    boolean existsByProfessorNumber(String professorNumber);

    /**
     * 임용일자 범위로 교수 목록 조회
     */
    @Query("SELECT p FROM Professor p WHERE p.appointmentDate BETWEEN :startDate AND :endDate")
    List<Professor> findByAppointmentDateBetween(@Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);

    /**
     * 사용자 ID로 교수 정보 조회
     */
    @Query("SELECT p FROM Professor p WHERE p.userId = :userId")
    Optional<Professor> findByUserId(@Param("userId") Long userId);

    /**
     * 교번 접두사로 시작하는 마지막 교수 조회 (교번 생성용)
     */
    Optional<Professor> findTopByProfessorNumberStartingWithOrderByProfessorNumberDesc(String prefix);

    // ==================== View Service용 조인 쿼리 ====================

    /**
     * 교번으로 교수 정보와 연관 데이터 조회 (Fetch Join)
     * User, UserProfile, UserContact, Department 정보를 함께 조회
     */
    @Query("""
        SELECT p FROM Professor p
        JOIN FETCH p.user u
        WHERE p.professorNumber = :professorNumber
        """)
    Optional<Professor> findByProfessorNumberWithUser(@Param("professorNumber") String professorNumber);

    /**
     * User ID로 교수 정보와 연관 데이터 조회
     */
    @Query("""
        SELECT p FROM Professor p
        JOIN FETCH p.user u
        WHERE p.userId = :userId
        """)
    Optional<Professor> findByUserIdWithUser(@Param("userId") Long userId);

    /**
     * 여러 교번으로 교수 정보 일괄 조회
     */
    @Query("""
        SELECT p FROM Professor p
        JOIN FETCH p.user u
        WHERE p.professorNumber IN :professorNumbers
        """)
    List<Professor> findByProfessorNumbersWithUser(@Param("professorNumbers") List<String> professorNumbers);

    /**
     * 여러 User ID로 교수 정보 일괄 조회
     */
    @Query("""
        SELECT p FROM Professor p
        JOIN FETCH p.user u
        WHERE p.userId IN :userIds
        """)
    List<Professor> findByUserIdsWithUser(@Param("userIds") List<Long> userIds);

    /**
     * 교수 전체 정보 조회를 위한 Native Query
     * 모든 관련 테이블을 조인하여 한 번에 조회
     */
    @Query(value = """
        SELECT p.user_id, p.professor_number, p.appointment_date,
               u.email,
               prof.name as profile_name,
               pc.mobile_number as phone_number,
               pc.office_number as office_number,
               d.id as department_id, d.name as department_name,
               col.id as college_id, col.name as college_name,
               pd.join_date as dept_join_date, pd.is_active as dept_active,
               img.image_url as profile_image_url
        FROM professors p
        INNER JOIN users u ON p.user_id = u.id
        LEFT JOIN user_profiles prof ON u.id = prof.user_id
        LEFT JOIN user_primary_contacts pc ON u.id = pc.user_id
        LEFT JOIN professor_departments pd ON p.user_id = pd.professor_id AND pd.is_active = true
        LEFT JOIN departments d ON pd.department_id = d.id
        LEFT JOIN colleges col ON d.college_id = col.id
        LEFT JOIN user_profile_images img ON u.id = img.user_id
        WHERE p.professor_number = :professorNumber
        """, nativeQuery = true)
    Object[] findProfessorFullInfoByProfessorNumber(@Param("professorNumber") String professorNumber);

    /**
     * 여러 교수의 전체 정보 조회를 위한 Native Query
     */
    @Query(value = """
        SELECT p.user_id, p.professor_number, p.appointment_date,
               u.email,
               prof.name as profile_name,
               pc.mobile_number as phone_number,
               pc.office_number as office_number,
               d.id as department_id, d.name as department_name,
               col.id as college_id, col.name as college_name,
               pd.join_date as dept_join_date, pd.is_active as dept_active,
               img.image_url as profile_image_url
        FROM professors p
        INNER JOIN users u ON p.user_id = u.id
        LEFT JOIN user_profiles prof ON u.id = prof.user_id
        LEFT JOIN user_primary_contacts pc ON u.id = pc.user_id
        LEFT JOIN professor_departments pd ON p.user_id = pd.professor_id AND pd.is_active = true
        LEFT JOIN departments d ON pd.department_id = d.id
        LEFT JOIN colleges col ON d.college_id = col.id
        LEFT JOIN user_profile_images img ON u.id = img.user_id
        WHERE p.professor_number IN :professorNumbers
        """, nativeQuery = true)
    List<Object[]> findProfessorsFullInfoByProfessorNumbers(@Param("professorNumbers") List<String> professorNumbers);
}