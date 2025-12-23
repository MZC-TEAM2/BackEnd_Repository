package com.mzc.backend.lms.domains.course.notice.service;

import com.mzc.backend.lms.domains.course.notice.dto.request.CourseNoticeCommentRequest;
import com.mzc.backend.lms.domains.course.notice.dto.request.CourseNoticeCreateRequest;
import com.mzc.backend.lms.domains.course.notice.dto.request.CourseNoticeUpdateRequest;
import com.mzc.backend.lms.domains.course.notice.dto.response.CourseNoticeCommentResponse;
import com.mzc.backend.lms.domains.course.notice.dto.response.CourseNoticeDetailResponse;
import com.mzc.backend.lms.domains.course.notice.dto.response.CourseNoticeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 강의 공지사항 서비스 인터페이스
 */
public interface CourseNoticeService {

    // === 공지 CRUD ===

    /**
     * 공지사항 생성
     */
    CourseNoticeResponse createNotice(Long courseId, CourseNoticeCreateRequest request, Long professorId);

    /**
     * 공지사항 목록 조회
     */
    Page<CourseNoticeResponse> getNotices(Long courseId, Long userId, Pageable pageable);

    /**
     * 공지사항 상세 조회
     */
    CourseNoticeDetailResponse getNotice(Long courseId, Long noticeId, Long userId);

    /**
     * 공지사항 수정
     */
    CourseNoticeResponse updateNotice(Long courseId, Long noticeId, CourseNoticeUpdateRequest request, Long professorId);

    /**
     * 공지사항 삭제
     */
    void deleteNotice(Long courseId, Long noticeId, Long professorId);

    // === 댓글 CRUD ===

    /**
     * 댓글 작성
     */
    CourseNoticeCommentResponse createComment(Long courseId, Long noticeId, CourseNoticeCommentRequest request, Long userId);

    /**
     * 대댓글 작성
     */
    CourseNoticeCommentResponse createReply(Long courseId, Long noticeId, Long parentId, CourseNoticeCommentRequest request, Long userId);

    /**
     * 댓글 수정
     */
    CourseNoticeCommentResponse updateComment(Long courseId, Long noticeId, Long commentId, CourseNoticeCommentRequest request, Long userId);

    /**
     * 댓글 삭제
     */
    void deleteComment(Long courseId, Long noticeId, Long commentId, Long userId);
}
