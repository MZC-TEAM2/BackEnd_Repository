package com.mzc.backend.lms.domains.board.dto.request;

import com.mzc.backend.lms.domains.board.enums.PostType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class PostCreateRequest {

    @NotNull(message = "카테고리 ID는 필수입니다")
    private Long categoryId;

    @NotBlank(message = "제목은 필수입니다")
    private String title;

    @NotBlank(message = "내용은 필수입니다")
    private String content;

    @NotNull(message = "게시글 타입은 필수입니다")
    private PostType postType;

    @NotNull(message = "익명 여부는 필수입니다")
    private Boolean isAnonymous;
}
