package com.mzc.backend.lms.domains.academy.repository;

import com.mzc.backend.lms.domains.academy.entity.PeriodType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 기간 타입 Repository
 */
@Repository
public interface PeriodTypeRepository extends JpaRepository<PeriodType, Integer> {
    
    /**
     * 타입 코드로 기간 타입 조회
     */
    Optional<PeriodType> findByTypeCode(String typeCode);

    /**
     * 타입 코드로 기간 타입 존재 여부 확인
     */
    boolean existsByTypeCode(String typeCode);
}

