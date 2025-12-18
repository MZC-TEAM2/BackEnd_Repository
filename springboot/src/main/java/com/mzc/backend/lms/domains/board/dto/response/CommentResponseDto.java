package com.mzc.backend.lms.domains.board.dto.response;

import com.mzc.backend.lms.domains.board.entity.Comment;
import com.mzc.backend.lms.domains.user.profile.dto.UserBasicInfoDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 댓글 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponseDto {

    private Long id;
    private Long postId;
    private String content;
    private int depth;
    private boolean isDeletedByAdmin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 작성자 정보 (중첩)
    private AuthorInfo author;

    // 부모 댓글 정보 (중첩, 대댓글인 경우)
    private ParentCommentInfo parentComment;

    // 알림용 게시글 작성자 ID
    private Long postAuthorId;

    // 하위 댓글
    private List<CommentResponseDto> childComments;

    // 첨부파일
    private List<AttachmentResponseDto> attachments;

    /**
     * 작성자 정보 중첩 DTO
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthorInfo {
        private Long id;
        private String name;
        private String userType;

        public static AuthorInfo from(UserBasicInfoDto userInfo) {
            if (userInfo == null) {
                return null;
            }
            return AuthorInfo.builder()
                    .id(userInfo.getId())
                    .name(userInfo.getName())
                    .userType(userInfo.getUserType())
                    .build();
        }
    }

    /**
     * 부모 댓글 정보 중첩 DTO
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParentCommentInfo {
        private Long id;
        private Long authorId;
    }

    /**
     * Entity에서 DTO 변환 (작성자 정보 없이)
     */
    public static CommentResponseDto from(Comment comment) {
        return CommentResponseDto.builder()
                .id(comment.getId())
                .postId(comment.getPost().getId())
                .postAuthorId(comment.getPost().getAuthorId())
                .content(comment.getContent())
                .depth(comment.getDepth())
                .isDeletedByAdmin(comment.isDeletedByAdmin())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .author(AuthorInfo.builder()
                        .id(comment.getAuthorId())
                        .build())
                .parentComment(comment.getParentComment() != null ?
                        ParentCommentInfo.builder()
                                .id(comment.getParentComment().getId())
                                .authorId(comment.getParentComment().getAuthorId())
                                .build() : null)
                .childComments(comment.getChildComments().stream()
                        .map(CommentResponseDto::from)
                        .collect(Collectors.toList()))
                .attachments(comment.getAttachments() != null ?
                        comment.getAttachments().stream()
                                .map(AttachmentResponseDto::from)
                                .collect(Collectors.toList()) : List.of())
                .build();
    }

    /**
     * Entity에서 DTO 변환 (작성자 정보 포함)
     */
    public static CommentResponseDto from(Comment comment, Map<Long, UserBasicInfoDto> userInfoMap) {
        UserBasicInfoDto authorInfo = userInfoMap.get(comment.getAuthorId());

        return CommentResponseDto.builder()
                .id(comment.getId())
                .postId(comment.getPost().getId())
                .postAuthorId(comment.getPost().getAuthorId())
                .content(comment.getContent())
                .depth(comment.getDepth())
                .isDeletedByAdmin(comment.isDeletedByAdmin())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .author(AuthorInfo.from(authorInfo))
                .parentComment(comment.getParentComment() != null ?
                        ParentCommentInfo.builder()
                                .id(comment.getParentComment().getId())
                                .authorId(comment.getParentComment().getAuthorId())
                                .build() : null)
                .childComments(comment.getChildComments().stream()
                        .map(child -> CommentResponseDto.from(child, userInfoMap))
                        .collect(Collectors.toList()))
                .attachments(comment.getAttachments() != null ?
                        comment.getAttachments().stream()
                                .map(AttachmentResponseDto::from)
                                .collect(Collectors.toList()) : List.of())
                .build();
    }
}