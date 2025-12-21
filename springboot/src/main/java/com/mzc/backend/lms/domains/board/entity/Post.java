package com.mzc.backend.lms.domains.board.entity;

import com.mzc.backend.lms.domains.board.enums.PostType;
import jakarta.persistence.*;
import java.util.*;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

/**
 * 게시글 엔터티
 * 모든 게시판의 게시글을 통합 관리
 */
@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private BoardCategory category;

    @Column(name = "title", nullable = false)
    private String title;

    @Lob
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "post_type", nullable = false, length = 30)
    private PostType postType;

    @Column(name = "is_anonymous", nullable = false)
    private boolean isAnonymous;

    @Column(name = "view_count", nullable = false)
    @ColumnDefault("0")
    private int viewCount = 0;

    @Column(name = "like_count", nullable = false)
    @ColumnDefault("0")
    private int likeCount = 0;

    @org.hibernate.annotations.BatchSize(size = 100)
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    private List<Attachment> attachments = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostHashtag> postHashtags = new ArrayList<>();

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "course_id")
    private Long courseId;

    @Builder
    public Post(BoardCategory category, String title, String content, PostType postType, boolean isAnonymous, Long authorId, Long departmentId, Long courseId) {
        super(authorId); // AuditableEntity의 생성자 호출
        this.authorId = authorId;
        this.category = category;
        this.title = title;
        this.content = content;
        this.postType = postType;
        this.isAnonymous = isAnonymous;
        this.departmentId = departmentId;
        this.courseId = courseId;
    }

    // --- 비즈니스 로직 ---

    public void increaseViewCount() {
        this.viewCount++;
    }

    public void increaseLikeCount() {
        this.likeCount++;
    }

    public void decreaseLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    public void update(String title, String content, boolean isAnonymous) {
        this.title = title;
        this.content = content;
        this.isAnonymous = isAnonymous;
    }

    // --- 해시태그 관리 메서드 ---

    /**
     * 해시태그 추가
     */
    public void addHashtag(Hashtag hashtag, Long userId) {
        PostHashtag postHashtag = PostHashtag.builder()
                .post(this)
                .hashtag(hashtag)
                .createdBy(userId)
                .build();
        this.postHashtags.add(postHashtag);
    }

    /**
     * 모든 해시태그 제거
     */
    public void clearHashtags() {
        this.postHashtags.clear();
    }

    /**
     * 해시태그 목록으로 교체
     */
    public void updateHashtags(List<Hashtag> hashtags, Long userId) {
        clearHashtags();
        for (Hashtag hashtag : hashtags) {
            addHashtag(hashtag, userId);
        }
    }
}
