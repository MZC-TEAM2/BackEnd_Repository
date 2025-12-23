package com.mzc.backend.lms.domains.course.notice.repository;

import com.mzc.backend.lms.domains.course.notice.entity.CourseNotice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CourseNoticeRepository extends JpaRepository<CourseNotice, Long> {

    /**
     * 강의별 공지사항 목록 조회 (페이징, 최신순)
     * Soft Delete 조건 적용
     */
    @Query("SELECT n FROM CourseNotice n " +
           "WHERE n.course.id = :courseId " +
           "AND n.isDeleted = false " +
           "ORDER BY n.createdAt DESC")
    Page<CourseNotice> findByCourseId(@Param("courseId") Long courseId, Pageable pageable);

    /**
     * 공지사항 상세 조회 (Soft Delete 조건 적용)
     */
    @Query("SELECT n FROM CourseNotice n " +
           "WHERE n.id = :noticeId " +
           "AND n.isDeleted = false")
    Optional<CourseNotice> findByIdAndNotDeleted(@Param("noticeId") Long noticeId);

    /**
     * 공지사항 상세 조회 (강의 정보 포함)
     */
    @Query("SELECT n FROM CourseNotice n " +
           "JOIN FETCH n.course c " +
           "WHERE n.id = :noticeId " +
           "AND n.isDeleted = false")
    Optional<CourseNotice> findByIdWithCourse(@Param("noticeId") Long noticeId);

    /**
     * 특정 강의의 공지 존재 여부 확인
     */
    @Query("SELECT CASE WHEN COUNT(n) > 0 THEN true ELSE false END " +
           "FROM CourseNotice n " +
           "WHERE n.id = :noticeId " +
           "AND n.course.id = :courseId " +
           "AND n.isDeleted = false")
    boolean existsByIdAndCourseId(@Param("noticeId") Long noticeId, @Param("courseId") Long courseId);
}
