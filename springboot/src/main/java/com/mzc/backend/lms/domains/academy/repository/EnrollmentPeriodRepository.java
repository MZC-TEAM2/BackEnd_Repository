package com.mzc.backend.lms.domains.academy.repository;

import com.mzc.backend.lms.domains.academy.entity.EnrollmentPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

import com.mzc.backend.lms.domains.academy.entity.AcademicTerm;

@Repository
public interface EnrollmentPeriodRepository extends JpaRepository<EnrollmentPeriod, Long> {

    //  학기와 기간명으로 수강신청 기간 조회
    Optional<EnrollmentPeriod> findByAcademicTermAndPeriodName(AcademicTerm academicTerm, String periodName);

    //  학기와 기간명 존재 여부 확인
    boolean existsByAcademicTermAndPeriodName(AcademicTerm academicTerm, String periodName);

    //  학기로 수강신청 기간 목록 조회
    List<EnrollmentPeriod> findByAcademicTerm(AcademicTerm academicTerm);

    //  기간명으로 수강신청 기간 목록 조회
    List<EnrollmentPeriod> findByPeriodName(String periodName);
}
