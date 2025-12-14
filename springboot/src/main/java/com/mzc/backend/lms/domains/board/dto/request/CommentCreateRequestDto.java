package com.mzc.backend.lms.domains.board.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 댓글 생성 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentCreateRequestDto {

    @NotNull(message = "게시글 ID는 필수입니다")
    private Long postId;
    
    @NotNull(message = "작성자 ID는 필수입니다")
    private Long authorId;

    private Long parentCommentId; // null이면 일반 댓글, 값이 있으면 대댓글

    @NotBlank(message = "내용은 필수입니다")
    private String content;

    private List<Long> attachmentIds; // 첨부파일 ID 목록
}
