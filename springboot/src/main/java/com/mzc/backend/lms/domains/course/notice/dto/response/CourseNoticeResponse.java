package com.mzc.backend.lms.domains.course.notice.dto.response;

import com.mzc.backend.lms.domains.course.notice.entity.CourseNotice;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CourseNoticeResponse {

    private Long id;
    private Long courseId;
    private String title;
    private Boolean allowComments;
    private Long authorId;
    private String authorName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CourseNoticeResponse from(CourseNotice notice) {
        return CourseNoticeResponse.builder()
                .id(notice.getId())
                .courseId(notice.getCourse().getId())
                .title(notice.getTitle())
                .allowComments(notice.getAllowComments())
                .authorId(notice.getCreatedBy())
                .createdAt(notice.getCreatedAt())
                .updatedAt(notice.getUpdatedAt())
                .build();
    }

    public static CourseNoticeResponse from(CourseNotice notice, String authorName) {
        return CourseNoticeResponse.builder()
                .id(notice.getId())
                .courseId(notice.getCourse().getId())
                .title(notice.getTitle())
                .allowComments(notice.getAllowComments())
                .authorId(notice.getCreatedBy())
                .authorName(authorName)
                .createdAt(notice.getCreatedAt())
                .updatedAt(notice.getUpdatedAt())
                .build();
    }
}
