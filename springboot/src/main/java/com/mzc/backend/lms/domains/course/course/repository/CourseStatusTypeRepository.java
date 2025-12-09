package com.mzc.backend.lms.domains.course.course.repository;

import com.mzc.backend.lms.domains.course.course.entity.CourseStatusType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CourseStatusTypeRepository extends JpaRepository<CourseStatusType, Long> {
    /**
     * 상태 코드로 조회
     */
    Optional<CourseStatusType> findByStatusCode(String statusCode);

    /**
     * 상태 코드 존재 여부 확인
     */
    boolean existsByStatusCode(String statusCode);

    /**
     * 상태 이름으로 조회
     */
    Optional<CourseStatusType> findByStatusName(String statusName);

    /**
     * 상태 이름 존재 여부 확인
     */

    boolean existsByStatusName(String statusName);

    /**
     * 상태 카테고리로 조회
     */
    Optional<CourseStatusType> findByCategory(int category);

    /**
     * 상태 카테고리 존재 여부 확인
     */

    boolean existsByCategory(int category);
}
