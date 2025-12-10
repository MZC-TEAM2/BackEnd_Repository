package com.mzc.backend.lms.domains.course.course.repository;

import com.mzc.backend.lms.domains.course.course.entity.CourseType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 강의 유형 Repository
 */

@Repository
public interface CourseTypeRepository extends JpaRepository<CourseType, Long> {
    /**
     * 강의 유형 코드로 조회
     */
    Optional<CourseType> findByTypeCode(int typeCode);

    /**
     * 강의 유형 코드 존재 여부 확인
     */
    boolean existsByTypeCode(int typeCode);

    /**
     * 강의 유형 카테고리로 조회
     */
    Optional<CourseType> findByCategory(int category);

    /**
     * 강의 유형 카테고리 존재 여부 확인
     */
    boolean existsByCategory(int category);
}
