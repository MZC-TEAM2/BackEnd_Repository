package com.mzc.backend.lms.domains.course.notice.dto.response;

import com.mzc.backend.lms.domains.course.notice.entity.CourseNoticeComment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class CourseNoticeCommentResponse {

    private Long id;
    private Long noticeId;
    private Long parentId;
    private String content;
    private Long authorId;
    private String authorName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CourseNoticeCommentResponse> children;

    public static CourseNoticeCommentResponse from(CourseNoticeComment comment, String authorName) {
        return CourseNoticeCommentResponse.builder()
                .id(comment.getId())
                .noticeId(comment.getNotice().getId())
                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .content(comment.getContent())
                .authorId(comment.getAuthorId())
                .authorName(authorName)
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }

    public static CourseNoticeCommentResponse from(CourseNoticeComment comment, String authorName,
                                                    List<CourseNoticeCommentResponse> children) {
        return CourseNoticeCommentResponse.builder()
                .id(comment.getId())
                .noticeId(comment.getNotice().getId())
                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .content(comment.getContent())
                .authorId(comment.getAuthorId())
                .authorName(authorName)
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .children(children)
                .build();
    }
}
