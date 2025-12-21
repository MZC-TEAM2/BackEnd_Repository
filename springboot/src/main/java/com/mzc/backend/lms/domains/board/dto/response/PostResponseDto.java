package com.mzc.backend.lms.domains.board.dto.response;

import com.mzc.backend.lms.domains.board.entity.Post;
import com.mzc.backend.lms.domains.board.enums.BoardType;
import com.mzc.backend.lms.domains.board.enums.PostType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 게시글 상세 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
public class PostResponseDto {

    private Long id;
    private Long categoryId;
    private BoardType boardType;
    private String title;
    private String content;
    private PostType postType;
    private boolean isAnonymous;
    private int viewCount;
    private int likeCount;
    private Long createdBy;
    private String createdByName;  // 작성자 이름
    private Long updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CommentResponseDto> comments;
    private List<AttachmentResponseDto> attachments;
    private List<HashtagDto> hashtags;

    /**
     * 작성자 이름 설정 (UserInfoCacheService를 통해 조회 후 설정)
     */
    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public static PostResponseDto from(Post post) {
        return PostResponseDto.builder()
                .id(post.getId())
                .categoryId(post.getCategory().getId())
                .boardType(post.getCategory().getBoardType())
                .title(post.getTitle())
                .content(post.getContent())
                .postType(post.getPostType())
                .isAnonymous(post.isAnonymous())
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .createdBy(post.getCreatedBy())
                .createdByName(null)
                .updatedBy(post.getUpdatedBy())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .comments(post.getComments().stream()
                        .filter(comment -> comment.getParentComment() == null)  // 최상위 댓글만
                        .map(CommentResponseDto::from)
                        .collect(Collectors.toList()))
                .attachments(post.getAttachments().stream()
                        .map(AttachmentResponseDto::from)
                        .collect(Collectors.toList()))
                .hashtags(post.getPostHashtags().stream()
                        .map(postHashtag -> HashtagDto.from(postHashtag.getHashtag()))
                        .collect(Collectors.toList()))
                .build();
    }
}
