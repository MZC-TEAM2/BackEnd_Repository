package com.mzc.backend.lms.domains.user.professor.repository;

import com.mzc.backend.lms.domains.user.organization.entity.Department;
import com.mzc.backend.lms.domains.user.professor.entity.Professor;
import com.mzc.backend.lms.domains.user.professor.entity.ProfessorDepartment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 교수-학과 관계 Repository
 */
@Repository
public interface ProfessorDepartmentRepository extends JpaRepository<ProfessorDepartment, Long> {

    /**
     * 교수로 학과 정보 조회
     */
    Optional<ProfessorDepartment> findByProfessor(Professor professor);

    /**
     * 학과별 교수 목록 조회
     */
    List<ProfessorDepartment> findByDepartment(Department department);

    /**
     * 교수 ID로 학과 정보 조회
     */
    @Query("SELECT pd FROM ProfessorDepartment pd WHERE pd.professor.professorId = :professorId")
    Optional<ProfessorDepartment> findByProfessorId(@Param("professorId") Long professorId);

    /**
     * 활성 상태인 교수-학과 관계 조회
     */
    @Query("SELECT pd FROM ProfessorDepartment pd WHERE pd.professor = :professor AND pd.endDate IS NULL")
    Optional<ProfessorDepartment> findActiveByProfessor(@Param("professor") Professor professor);


}
