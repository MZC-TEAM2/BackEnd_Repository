package com.mzc.backend.lms.domains.board.assignment.repository;

import com.mzc.backend.lms.domains.board.assignment.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 과제 Repository
 */
@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    /**
     * 게시글 ID로 과제 조회
     */
    @Query("SELECT a FROM Assignment a WHERE a.post.id = :postId AND a.isDeleted = false")
    Optional<Assignment> findByPostId(@Param("postId") Long postId);

    /**
     * 강의 ID로 과제 목록 조회
     */
    @Query("SELECT a FROM Assignment a WHERE a.courseId = :courseId AND a.isDeleted = false ORDER BY a.dueDate DESC")
    List<Assignment> findByCourseId(@Param("courseId") Long courseId);

    /**
     * 마감일이 임박한 과제 목록 조회
     */
    @Query("SELECT a FROM Assignment a WHERE a.dueDate BETWEEN :startDate AND :endDate AND a.isDeleted = false ORDER BY a.dueDate ASC")
    List<Assignment> findUpcomingAssignments(@Param("startDate") LocalDateTime startDate, 
                                             @Param("endDate") LocalDateTime endDate);

    /**
     * 마감일이 지난 과제 목록 조회
     */
    @Query("SELECT a FROM Assignment a WHERE a.dueDate < :now AND a.isDeleted = false ORDER BY a.dueDate DESC")
    List<Assignment> findOverdueAssignments(@Param("now") LocalDateTime now);

    /**
     * 강의별 과제 수 조회
     */
    @Query("SELECT COUNT(a) FROM Assignment a WHERE a.courseId = :courseId AND a.isDeleted = false")
    Long countByCourseId(@Param("courseId") Long courseId);

    /**
     * 생성자 ID로 과제 목록 조회
     */
    @Query("SELECT a FROM Assignment a WHERE a.createdBy = :creatorId AND a.isDeleted = false ORDER BY a.createdAt DESC")
    List<Assignment> findByCreatorId(@Param("creatorId") Long creatorId);
}
