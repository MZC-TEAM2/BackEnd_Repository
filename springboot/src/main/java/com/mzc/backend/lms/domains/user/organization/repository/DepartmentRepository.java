package com.mzc.backend.lms.domains.user.organization.repository;

import com.mzc.backend.lms.domains.user.organization.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 학과 Repository
 */
@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    /**
     * 학과 코드로 조회
     */
    Optional<Department> findByDepartmentCode(String departmentCode);

    /**
     * 학과 코드 존재 여부 확인
     */
    boolean existsByDepartmentCode(String departmentCode);

    /**
     * 단과대학별 학과 목록 조회
     */
    @Query("SELECT d FROM Department d WHERE d.college.id = :collegeId")
    List<Department> findByCollegeId(@Param("collegeId") Long collegeId);

    /**
     * 학과명으로 검색
     */
    List<Department> findByDepartmentNameContaining(String departmentName);
}