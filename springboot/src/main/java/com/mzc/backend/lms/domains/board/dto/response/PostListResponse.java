package com.mzc.backend.lms.domains.board.dto.response;

import com.mzc.backend.lms.domains.board.entity.Post;
import com.mzc.backend.lms.domains.board.enums.BoardType;
import com.mzc.backend.lms.domains.board.enums.PostType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 게시글 목록 응답 DTO (간단한 정보만)
 */
@Getter
@Builder
@AllArgsConstructor
public class PostListResponse {

    private Long id;
    private Long categoryId;
    private BoardType boardType;
    private String title;
    private PostType postType;
    private boolean isAnonymous;
    private int viewCount;
    private int likeCount;
    private int commentCount;
    private LocalDateTime createdAt;

    public static PostListResponse from(Post post) {
        return PostListResponse.builder()
                .id(post.getId())
                .categoryId(post.getCategory().getId())
                .boardType(post.getCategory().getBoardType())
                .title(post.getTitle())
                .postType(post.getPostType())
                .isAnonymous(post.isAnonymous())
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .commentCount(post.getComments().size())
                .createdAt(post.getCreatedAt())
                .build();
    }
}
