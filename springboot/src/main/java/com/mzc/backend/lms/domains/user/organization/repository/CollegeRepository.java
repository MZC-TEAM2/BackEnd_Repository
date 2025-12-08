package com.mzc.backend.lms.domains.user.organization.repository;

import com.mzc.backend.lms.domains.user.organization.entity.College;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 단과대학 Repository
 */
@Repository
public interface CollegeRepository extends JpaRepository<College, Long> {

    /**
     * 단과대학 코드로 조회
     */
    Optional<College> findByCollegeCode(String collegeCode);

    /**
     * 단과대학 코드 존재 여부 확인
     */
    boolean existsByCollegeCode(String collegeCode);

    /**
     * 단과대학명으로 조회
     */
    Optional<College> findByCollegeName(String collegeName);
}