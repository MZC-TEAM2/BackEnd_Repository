package com.mzc.backend.lms.domains.board.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 댓글 엔터티
 * 계층형 댓글(대댓글) 구조 지원
 */
@Entity
@Table(name = "comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment;

    @Lob
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "depth", nullable = false)
    private int depth = 0;

    @Column(name = "is_deleted_by_admin", nullable = false)
    private boolean isDeletedByAdmin = false;

    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL)
    private List<Comment> childComments = new ArrayList<>();

    @Builder
    public Comment(Post post, Comment parentComment, String content) {
        this.post = post;
        this.parentComment = parentComment;
        this.content = content;
        this.depth = (parentComment != null) ? parentComment.getDepth() + 1 : 0;
    }

    // --- 비즈니스 로직 ---

    public void updateContent(String content) {
        this.content = content;
    }

    public void deleteByAdmin() {
        this.isDeletedByAdmin = true;
        this.delete(); // BaseEntity의 soft delete 호출
    }
}
