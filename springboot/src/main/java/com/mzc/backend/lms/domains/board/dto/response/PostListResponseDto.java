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
 * 게시글 목록 응답 DTO (간단한 정보만)
 */
@Getter
@Builder
@AllArgsConstructor
public class PostListResponseDto {

    private Long id;
    private Long categoryId;
    private BoardType boardType;
    private String title;
    private PostType postType;
    private boolean isAnonymous;
    private int viewCount;
    private int likeCount;
    private int commentCount;
    private Long createdBy;
    private String createdByName;  // 작성자 이름
    private LocalDateTime createdAt;
    private List<HashtagDto> hashtags;

    /**
     * 작성자 이름 설정 (UserInfoCacheService를 통해 조회 후 설정)
     */
    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public static PostListResponseDto from(Post post) {
        return PostListResponseDto.builder()
                .id(post.getId())
                .categoryId(post.getCategory().getId())
                .boardType(post.getCategory().getBoardType())
                .title(post.getTitle())
                .postType(post.getPostType())
                .isAnonymous(post.isAnonymous())
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .commentCount(post.getComments().size())
                .createdBy(post.getCreatedBy())
                .createdByName(null)  // PostService에서 enrichWithUserInfo()로 설정됨
                .createdAt(post.getCreatedAt())
                .hashtags(post.getPostHashtags().stream()
                        .map(postHashtag -> HashtagDto.from(postHashtag.getHashtag()))
                        .collect(Collectors.toList()))
                .build();
    }
}
