package com.mzc.backend.lms.domains.course.notice.entity;

import com.mzc.backend.lms.domains.board.entity.AuditableEntity;
import com.mzc.backend.lms.domains.course.course.entity.Course;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 강의 공지사항 엔티티
 * 담당 교수만 작성 가능, 수강생만 조회 가능
 */
@Entity
@Table(name = "course_notices", indexes = {
        @Index(name = "idx_course_notice_course_id", columnList = "course_id"),
        @Index(name = "idx_course_notice_is_deleted", columnList = "is_deleted")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseNotice extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Lob
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "allow_comments", nullable = false)
    private Boolean allowComments;

    @OneToMany(mappedBy = "notice", cascade = CascadeType.ALL, orphanRemoval = true)
    @org.hibernate.annotations.BatchSize(size = 100)
    private List<CourseNoticeComment> comments = new ArrayList<>();

    // === Factory Method ===

    /**
     * 강의 공지사항 생성
     *
     * @param course        강의
     * @param title         제목
     * @param content       내용
     * @param allowComments 댓글 허용 여부
     * @param professorId   작성 교수 ID
     * @return CourseNotice
     */
    public static CourseNotice create(Course course, String title, String content,
                                       Boolean allowComments, Long professorId) {
        CourseNotice notice = new CourseNotice(professorId);
        notice.course = course;
        notice.title = title;
        notice.content = content;
        notice.allowComments = allowComments;
        return notice;
    }

    // === Private Constructor ===

    private CourseNotice(Long createdBy) {
        super(createdBy);
    }

    // === Business Methods ===

    /**
     * 공지사항 수정
     *
     * @param title         제목
     * @param content       내용
     * @param allowComments 댓글 허용 여부
     * @param modifierId    수정자 ID
     */
    public void update(String title, String content, Boolean allowComments, Long modifierId) {
        this.title = title;
        this.content = content;
        this.allowComments = allowComments;
        updateModifier(modifierId);
    }

    /**
     * 댓글 허용 여부 확인
     *
     * @return 댓글 허용 여부
     */
    public boolean isCommentsAllowed() {
        return Boolean.TRUE.equals(this.allowComments);
    }

    /**
     * 해당 강의의 공지인지 확인
     *
     * @param courseId 강의 ID
     * @return 일치 여부
     */
    public boolean belongsToCourse(Long courseId) {
        return this.course != null && this.course.getId().equals(courseId);
    }
}