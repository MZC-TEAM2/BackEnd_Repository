package com.mzc.backend.lms.domains.academy.repository;

import com.mzc.backend.lms.domains.academy.entity.AcademicTerm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AcademicTermRepository extends JpaRepository<AcademicTerm, Long> {

    //  학년도와 학기 구분으로 학기 조회
    Optional<AcademicTerm> findByYearAndTermType(Integer year, String termType);

    //  학년도와 학기 구분 존재 여부 확인
    boolean existsByYearAndTermType(Integer year, String termType);

    //  학년도로 학기 목록 조회
    List<AcademicTerm> findByYear(Integer year);

    //  학기 구분으로 학기 목록 조회
    List<AcademicTerm> findByTermType(String termType);

    /**
     * 현재 날짜에 해당하는 학기 조회
     * - start_date <= today <= end_date
     * - 여러 개가 겹치면(비정상 데이터) 가장 최근 시작한 학기를 반환
     */
    @Query("""
            SELECT t
            FROM AcademicTerm t
            WHERE t.startDate <= :today AND t.endDate >= :today
            ORDER BY t.startDate DESC
            """)
    List<AcademicTerm> findCurrentTerms(@Param("today") LocalDate today);
}
