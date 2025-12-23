package com.mzc.backend.lms.domains.assessment.repository;

import com.mzc.backend.lms.domains.assessment.entity.AssessmentAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface AssessmentAttemptRepository extends JpaRepository<AssessmentAttempt, Long> {
	
	@Query("""
			    SELECT at FROM AssessmentAttempt at
			    JOIN FETCH at.assessment a
			    JOIN FETCH a.post p
			    JOIN FETCH p.category c
			    WHERE at.id = :id AND at.deletedAt IS NULL
			""")
	Optional<AssessmentAttempt> findActiveWithAssessment(@Param("id") Long id);
	
	@Query("""
			    SELECT at FROM AssessmentAttempt at
			    JOIN FETCH at.assessment a
			    WHERE a.id = :assessmentId
			      AND at.deletedAt IS NULL
			      AND (
			           :status = 'ALL'
			           OR (:status = 'SUBMITTED' AND at.submittedAt IS NOT NULL)
			           OR (:status = 'IN_PROGRESS' AND at.submittedAt IS NULL)
			      )
			    ORDER BY at.createdAt DESC
			""")
	List<AssessmentAttempt> findActiveByAssessmentIdAndStatus(@Param("assessmentId") Long assessmentId,
	                                                          @Param("status") String status);
	
	@Query("""
			    SELECT COUNT(at) > 0 FROM AssessmentAttempt at
			    WHERE at.assessment.id IN :assessmentIds
			      AND at.deletedAt IS NULL
			      AND at.submittedAt IS NOT NULL
			      AND at.score IS NULL
			""")
	boolean existsUngradedSubmittedByAssessmentIds(@Param("assessmentIds") List<Long> assessmentIds);
	
	@Query("""
			    SELECT COALESCE(SUM(at.score), 0) FROM AssessmentAttempt at
			    WHERE at.assessment.id IN :assessmentIds
			      AND at.userId = :userId
			      AND at.deletedAt IS NULL
			      AND at.submittedAt IS NOT NULL
			      AND at.score IS NOT NULL
			""")
	BigDecimal sumGradedScoreByUserAndAssessmentIds(@Param("userId") Long userId,
	                                                @Param("assessmentIds") List<Long> assessmentIds);
	
	@Query("""
			    SELECT at FROM AssessmentAttempt at
			    WHERE at.assessment.id = :assessmentId
			      AND at.userId = :userId
			      AND at.deletedAt IS NULL
			""")
	Optional<AssessmentAttempt> findActiveByAssessmentIdAndUserId(@Param("assessmentId") Long assessmentId,
	                                                              @Param("userId") Long userId);
}


