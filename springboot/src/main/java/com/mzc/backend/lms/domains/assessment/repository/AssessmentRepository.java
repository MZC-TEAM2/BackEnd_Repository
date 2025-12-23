package com.mzc.backend.lms.domains.assessment.repository;

import com.mzc.backend.lms.domains.assessment.entity.Assessment;
import com.mzc.backend.lms.domains.assessment.enums.AssessmentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AssessmentRepository extends JpaRepository<Assessment, Long> {

    @Query("""
        SELECT a FROM Assessment a
        JOIN FETCH a.post p
        JOIN FETCH p.category c
        WHERE a.id = :id AND a.deletedAt IS NULL
    """)
    Optional<Assessment> findActiveWithPost(@Param("id") Long id);

    @Query("""
        SELECT a FROM Assessment a
        JOIN FETCH a.post p
        JOIN FETCH p.category c
        WHERE a.courseId = :courseId
          AND a.type = :type
          AND a.deletedAt IS NULL
        ORDER BY a.startAt DESC
    """)
    List<Assessment> findActiveByCourse(@Param("courseId") Long courseId, @Param("type") AssessmentType type);

    @Query("""
        SELECT a FROM Assessment a
        JOIN FETCH a.post p
        JOIN FETCH p.category c
        WHERE a.courseId = :courseId
          AND a.type = :type
          AND a.deletedAt IS NULL
          AND a.startAt <= :now
        ORDER BY a.startAt DESC
    """)
    List<Assessment> findVisibleByCourseForStudent(@Param("courseId") Long courseId,
                                                   @Param("type") AssessmentType type,
                                                   @Param("now") LocalDateTime now);
}


