package com.mzc.backend.lms.domains.board.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 게시글-해시태그 연결 엔티티
 * Post와 Hashtag의 다대다 관계를 중간 테이블로 관리
 */
@Entity
@Table(name = "post_hashtags")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostHashtag extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 게시글
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    /**
     * 해시태그
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hashtag_id", nullable = false)
    private Hashtag hashtag;

    @Builder
    public PostHashtag(Post post, Hashtag hashtag, Long createdBy) {
        super(createdBy);
        this.post = post;
        this.hashtag = hashtag;
    }

    // --- 연관관계 편의 메서드 ---

    /**
     * 게시글 설정 (양방향 관계 설정)
     */
    public void setPost(Post post) {
        if (this.post != null) {
            this.post.getPostHashtags().remove(this);
        }
        this.post = post;
        if (post != null && !post.getPostHashtags().contains(this)) {
            post.getPostHashtags().add(this);
        }
    }
}
