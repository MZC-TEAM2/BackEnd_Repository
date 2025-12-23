package com.mzc.backend.lms.domains.course.notice.dto.response;

import com.mzc.backend.lms.domains.course.notice.entity.CourseNotice;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class CourseNoticeDetailResponse {
	
	private Long id;
	private Long courseId;
	private String title;
	private String content;
	private Boolean allowComments;
	private Long authorId;
	private String authorName;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private List<CourseNoticeCommentResponse> comments;
	
	public static CourseNoticeDetailResponse from(CourseNotice notice, String authorName,
	                                              List<CourseNoticeCommentResponse> comments) {
		return CourseNoticeDetailResponse.builder()
				.id(notice.getId())
				.courseId(notice.getCourse().getId())
				.title(notice.getTitle())
				.content(notice.getContent())
				.allowComments(notice.getAllowComments())
				.authorId(notice.getCreatedBy())
				.authorName(authorName)
				.createdAt(notice.getCreatedAt())
				.updatedAt(notice.getUpdatedAt())
				.comments(comments)
				.build();
	}
}
