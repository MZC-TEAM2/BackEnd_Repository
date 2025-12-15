package com.mzc.backend.lms.domains.board.dto.request;

import java.util.List;

import com.mzc.backend.lms.domains.board.enums.PostType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 게시글 생성 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostCreateRequestDto {

    private Long categoryId;

    private Long authorId;

    private String title;

    private String content;

    private PostType postType;

    private Boolean isAnonymous;

    private List<Long> attachmentIds;

    private List<String> hashtags;
}
