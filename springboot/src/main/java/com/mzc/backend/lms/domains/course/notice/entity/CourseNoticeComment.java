package com.mzc.backend.lms.domains.course.notice.entity;

import com.mzc.backend.lms.domains.board.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 강의 공지사항 댓글 엔티티
 * 대댓글 지원 (2depth)
 * 수강생 및 담당 교수 작성 가능
 */
@Entity
@Table(name = "course_notice_comments", indexes = {
        @Index(name = "idx_course_notice_comment_notice_id", columnList = "notice_id"),
        @Index(name = "idx_course_notice_comment_parent_id", columnList = "parent_id"),
        @Index(name = "idx_course_notice_comment_is_deleted", columnList = "is_deleted")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseNoticeComment extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id", nullable = false)
    private CourseNotice notice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private CourseNoticeComment parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @org.hibernate.annotations.BatchSize(size = 100)
    private List<CourseNoticeComment> children = new ArrayList<>();

    @Lob
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    // === Factory Methods ===

    /**
     * 댓글 생성
     *
     * @param notice   공지사항
     * @param content  내용
     * @param authorId 작성자 ID
     * @return CourseNoticeComment
     */
    public static CourseNoticeComment create(CourseNotice notice, String content, Long authorId) {
        CourseNoticeComment comment = new CourseNoticeComment(authorId);
        comment.notice = notice;
        comment.content = content;
        comment.authorId = authorId;
        return comment;
    }

    /**
     * 대댓글 생성
     *
     * @param notice   공지사항
     * @param parent   부모 댓글
     * @param content  내용
     * @param authorId 작성자 ID
     * @return CourseNoticeComment
     */
    public static CourseNoticeComment createReply(CourseNotice notice, CourseNoticeComment parent,
                                                   String content, Long authorId) {
        CourseNoticeComment reply = new CourseNoticeComment(authorId);
        reply.notice = notice;
        reply.parent = parent;
        reply.content = content;
        reply.authorId = authorId;
        return reply;
    }

    // === Private Constructor ===

    private CourseNoticeComment(Long createdBy) {
        super(createdBy);
    }

    // === Business Methods ===

    /**
     * 댓글 수정
     *
     * @param content    내용
     * @param modifierId 수정자 ID
     */
    public void update(String content, Long modifierId) {
        this.content = content;
        updateModifier(modifierId);
    }

    /**
     * 대댓글 여부 확인
     *
     * @return 대댓글이면 true
     */
    public boolean isReply() {
        return this.parent != null;
    }

    /**
     * 최상위 댓글 여부 확인
     *
     * @return 최상위 댓글이면 true
     */
    public boolean isRootComment() {
        return this.parent == null;
    }

    /**
     * 작성자 확인
     *
     * @param userId 사용자 ID
     * @return 작성자이면 true
     */
    public boolean isAuthor(Long userId) {
        return this.authorId != null && this.authorId.equals(userId);
    }

    /**
     * 해당 공지의 댓글인지 확인
     *
     * @param noticeId 공지 ID
     * @return 일치 여부
     */
    public boolean belongsToNotice(Long noticeId) {
        return this.notice != null && this.notice.getId().equals(noticeId);
    }
}
