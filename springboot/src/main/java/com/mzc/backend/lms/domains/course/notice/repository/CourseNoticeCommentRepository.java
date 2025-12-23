package com.mzc.backend.lms.domains.course.notice.repository;

import com.mzc.backend.lms.domains.course.notice.entity.CourseNotice;
import com.mzc.backend.lms.domains.course.notice.entity.CourseNoticeComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseNoticeCommentRepository extends JpaRepository<CourseNoticeComment, Long> {

    /**
     * 공지사항의 최상위 댓글 목록 조회 (대댓글 제외)
     * 작성일 기준 오래된 순
     */
    @Query("SELECT c FROM CourseNoticeComment c " +
           "WHERE c.notice = :notice " +
           "AND c.parent IS NULL " +
           "AND c.isDeleted = false " +
           "ORDER BY c.createdAt ASC")
    List<CourseNoticeComment> findRootCommentsByNotice(@Param("notice") CourseNotice notice);

    /**
     * 공지사항의 모든 댓글 조회 (대댓글 포함)
     */
    @Query("SELECT c FROM CourseNoticeComment c " +
           "LEFT JOIN FETCH c.children " +
           "WHERE c.notice.id = :noticeId " +
           "AND c.parent IS NULL " +
           "AND c.isDeleted = false " +
           "ORDER BY c.createdAt ASC")
    List<CourseNoticeComment> findAllByNoticeIdWithChildren(@Param("noticeId") Long noticeId);

    /**
     * 특정 댓글의 대댓글 목록 조회
     */
    @Query("SELECT c FROM CourseNoticeComment c " +
           "WHERE c.parent = :parent " +
           "AND c.isDeleted = false " +
           "ORDER BY c.createdAt ASC")
    List<CourseNoticeComment> findByParent(@Param("parent") CourseNoticeComment parent);

    /**
     * 댓글 상세 조회 (Soft Delete 조건 적용)
     */
    @Query("SELECT c FROM CourseNoticeComment c " +
           "WHERE c.id = :commentId " +
           "AND c.isDeleted = false")
    Optional<CourseNoticeComment> findByIdAndNotDeleted(@Param("commentId") Long commentId);

    /**
     * 특정 공지의 댓글 존재 여부 확인
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
           "FROM CourseNoticeComment c " +
           "WHERE c.id = :commentId " +
           "AND c.notice.id = :noticeId " +
           "AND c.isDeleted = false")
    boolean existsByIdAndNoticeId(@Param("commentId") Long commentId, @Param("noticeId") Long noticeId);
}
