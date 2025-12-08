package com.mzc.backend.lms.domains.user.student.repository;

import com.mzc.backend.lms.domains.user.student.entity.StudentNumberSequence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 학번 시퀀스 Repository
 */
@Repository
public interface StudentNumberSequenceRepository extends JpaRepository<StudentNumberSequence, Long> {

    /**
     * 년도/단과대학/학과별 시퀀스 조회 (비관적 락)
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM StudentNumberSequence s " +
           "WHERE s.year = :year AND s.collegeId = :collegeId AND s.departmentId = :departmentId")
    Optional<StudentNumberSequence> findByYearAndCollegeAndDepartmentWithLock(
            @Param("year") Integer year,
            @Param("collegeId") Long collegeId,
            @Param("departmentId") Long departmentId);

    /**
     * 년도/단과대학/학과별 시퀀스 조회
     */
    Optional<StudentNumberSequence> findByYearAndCollegeIdAndDepartmentId(
            Integer year, Long collegeId, Long departmentId);
}