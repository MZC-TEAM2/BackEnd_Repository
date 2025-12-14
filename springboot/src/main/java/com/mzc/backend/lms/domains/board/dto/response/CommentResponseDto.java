package com.mzc.backend.lms.domains.board.dto.response;

import com.mzc.backend.lms.domains.board.entity.Comment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 댓글 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
public class CommentResponseDto {

    private Long id;
    private Long postId;
    private Long parentCommentId;
    private String content;
    private int depth;
    private boolean isDeletedByAdmin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CommentResponseDto> childComments;
    private List<AttachmentResponseDto> attachments;

    public static CommentResponseDto from(Comment comment) {
        return CommentResponseDto.builder()
                .id(comment.getId())
                .postId(comment.getPost().getId())
                .parentCommentId(comment.getParentComment() != null ? 
                        comment.getParentComment().getId() : null)
                .content(comment.getContent())
                .depth(comment.getDepth())
                .isDeletedByAdmin(comment.isDeletedByAdmin())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .childComments(comment.getChildComments().stream()
                        .map(CommentResponseDto::from)
                        .collect(Collectors.toList()))
                .attachments(comment.getAttachments() != null ? 
                        comment.getAttachments().stream()
                                .map(AttachmentResponseDto::from)
                                .collect(Collectors.toList()) : List.of())
                .build();
    }
}
