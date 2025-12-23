package com.mzc.backend.lms.domains.course.notice.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 강의 공지사항 생성 이벤트
 * 공지사항 생성 시 발행되어 수강생들에게 알림 발송에 사용됨
 */
@Getter
@AllArgsConstructor
public class CourseNoticeCreatedEvent {

    /**
     * 공지사항 ID
     */
    private final Long noticeId;

    /**
     * 강의 ID
     */
    private final Long courseId;

    /**
     * 강의명
     */
    private final String courseName;

    /**
     * 공지사항 제목
     */
    private final String noticeTitle;

    /**
     * 작성자 ID (교수)
     */
    private final Long professorId;
}
