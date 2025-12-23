package com.mzc.backend.lms.domains.course.notice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CourseNoticeCommentRequest {

    @NotBlank(message = "댓글 내용은 필수입니다.")
    private String content;
}
