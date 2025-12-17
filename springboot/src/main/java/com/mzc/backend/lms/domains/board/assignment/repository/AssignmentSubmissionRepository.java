package com.mzc.backend.lms.domains.board.assignment.repository;

import com.mzc.backend.lms.domains.board.assignment.entity.AssignmentSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 과제 제출 Repository
 */
@Repository
public interface AssignmentSubmissionRepository extends JpaRepository<AssignmentSubmission, Long> {

    /**
     * 과제 ID와 학생 ID로 제출 조회
     */
    @Query("SELECT s FROM AssignmentSubmission s WHERE s.assignment.id = :assignmentId AND s.userId = :userId AND s.isDeleted = false")
    Optional<AssignmentSubmission> findByAssignmentIdAndUserId(@Param("assignmentId") Long assignmentId, 
                                                                @Param("userId") Long userId);

    /**
     * 과제 ID로 전체 제출 목록 조회
     */
    @Query("SELECT s FROM AssignmentSubmission s WHERE s.assignment.id = :assignmentId AND s.isDeleted = false ORDER BY s.submittedAt DESC")
    List<AssignmentSubmission> findByAssignmentId(@Param("assignmentId") Long assignmentId);

    /**
     * 학생 ID로 제출 목록 조회
     */
    @Query("SELECT s FROM AssignmentSubmission s WHERE s.userId = :userId AND s.isDeleted = false ORDER BY s.submittedAt DESC")
    List<AssignmentSubmission> findByUserId(@Param("userId") Long userId);

    /**
     * 과제별 제출 수 조회
     */
    @Query("SELECT COUNT(s) FROM AssignmentSubmission s WHERE s.assignment.id = :assignmentId AND s.isDeleted = false")
    Long countByAssignmentId(@Param("assignmentId") Long assignmentId);

    /**
     * 채점 대기 중인 제출 목록 조회
     */
    @Query("SELECT s FROM AssignmentSubmission s WHERE s.status IN ('SUBMITTED', 'LATE') AND s.isDeleted = false ORDER BY s.submittedAt ASC")
    List<AssignmentSubmission> findPendingGrading();

    /**
     * 과제별 채점 대기 목록 조회
     */
    @Query("SELECT s FROM AssignmentSubmission s WHERE s.assignment.id = :assignmentId AND s.status IN ('SUBMITTED', 'LATE') AND s.isDeleted = false ORDER BY s.submittedAt ASC")
    List<AssignmentSubmission> findPendingGradingByAssignment(@Param("assignmentId") Long assignmentId);

    /**
     * 채점 완료된 제출 목록 조회
     */
    @Query("SELECT s FROM AssignmentSubmission s WHERE s.status = 'GRADED' AND s.isDeleted = false ORDER BY s.gradedAt DESC")
    List<AssignmentSubmission> findGraded();

    /**
     * 지각 제출 목록 조회
     */
    @Query("SELECT s FROM AssignmentSubmission s WHERE s.status = 'LATE' AND s.isDeleted = false ORDER BY s.submittedAt DESC")
    List<AssignmentSubmission> findLateSubmissions();

    /**
     * 과제별 평균 점수 조회
     */
    @Query("SELECT AVG(s.score) FROM AssignmentSubmission s WHERE s.assignment.id = :assignmentId AND s.status = 'GRADED' AND s.isDeleted = false")
    Double getAverageScoreByAssignment(@Param("assignmentId") Long assignmentId);

    /**
     * 학생별 과제 제출 여부 확인
     */
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM AssignmentSubmission s WHERE s.assignment.id = :assignmentId AND s.userId = :userId AND s.isDeleted = false")
    boolean existsByAssignmentIdAndUserId(@Param("assignmentId") Long assignmentId, 
                                          @Param("userId") Long userId);
}
