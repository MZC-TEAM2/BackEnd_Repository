package com.mzc.backend.lms.domains.academy.repository;

import com.mzc.backend.lms.domains.academy.entity.AcademicTerm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
    
    
}
