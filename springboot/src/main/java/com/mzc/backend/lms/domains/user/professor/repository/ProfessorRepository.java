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
}