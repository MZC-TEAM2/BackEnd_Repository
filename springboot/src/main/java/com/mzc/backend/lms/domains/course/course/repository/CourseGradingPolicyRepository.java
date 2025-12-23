package com.mzc.backend.lms.domains.course.course.repository;

import com.mzc.backend.lms.domains.course.course.entity.CourseGradingPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseGradingPolicyRepository extends JpaRepository<CourseGradingPolicy, Long> {
}


