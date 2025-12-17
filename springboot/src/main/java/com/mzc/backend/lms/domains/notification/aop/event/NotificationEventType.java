package com.mzc.backend.lms.domains.notification.aop.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 알림 이벤트 타입 열거형
 * NotificationType 테이블의 type_code와 매핑됨
 */
@Getter
@RequiredArgsConstructor
public enum NotificationEventType {

    // ========== 강의 관련 ==========
    /**
     * 새 강의 개설
     */
    COURSE_CREATED("COURSE_CREATED", "강의", "새 강의가 개설되었습니다."),

    /**
     * 강의 수강 승인
     */
    COURSE_ENROLLMENT_APPROVED("COURSE_ENROLLMENT_APPROVED", "강의", "수강 신청이 승인되었습니다."),

    /**
     * 강의 수강 거절
     */
    COURSE_ENROLLMENT_REJECTED("COURSE_ENROLLMENT_REJECTED", "강의", "수강 신청이 거절되었습니다."),

    // ========== 과제 관련 ==========
    /**
     * 새 과제 등록
     */
    ASSIGNMENT_CREATED("ASSIGNMENT_CREATED", "과제", "새 과제가 등록되었습니다."),

    /**
     * 과제 수정
     */
    ASSIGNMENT_UPDATED("ASSIGNMENT_UPDATED", "과제", "과제가 수정되었습니다."),

    /**
     * 과제 마감 임박 (D-1, D-3 등)
     */
    ASSIGNMENT_DEADLINE_APPROACHING("ASSIGNMENT_DEADLINE_APPROACHING", "과제", "과제 마감일이 다가왔습니다."),

    /**
     * 과제 제출 완료
     */
    ASSIGNMENT_SUBMITTED("ASSIGNMENT_SUBMITTED", "과제", "과제가 제출되었습니다."),

    /**
     * 과제 채점 완료
     */
    ASSIGNMENT_GRADED("ASSIGNMENT_GRADED", "과제", "과제 채점이 완료되었습니다."),

    // ========== 공지사항 관련 ==========
    /**
     * 강의 공지사항 등록
     */
    COURSE_NOTICE_CREATED("COURSE_NOTICE_CREATED", "공지", "새 공지사항이 등록되었습니다."),

    /**
     * 전체 공지사항 등록
     */
    SYSTEM_NOTICE_CREATED("SYSTEM_NOTICE_CREATED", "공지", "새 시스템 공지사항이 등록되었습니다."),

    // ========== 게시판 관련 ==========
    /**
     * 질문 게시글 등록
     */
    QUESTION_CREATED("QUESTION_CREATED", "게시판", "새 질문이 등록되었습니다."),

    /**
     * 질문에 답변 등록
     */
    QUESTION_ANSWERED("QUESTION_ANSWERED", "게시판", "질문에 답변이 등록되었습니다."),

    /**
     * 토론 게시글 등록
     */
    DISCUSSION_CREATED("DISCUSSION_CREATED", "게시판", "새 토론 주제가 등록되었습니다."),

    /**
     * 토론 댓글 등록
     */
    DISCUSSION_COMMENT_CREATED("DISCUSSION_COMMENT_CREATED", "게시판", "토론에 새 댓글이 등록되었습니다."),

    /**
     * 게시글에 댓글 등록
     */
    COMMENT_CREATED("COMMENT_CREATED", "게시판", "내 게시글에 새 댓글이 등록되었습니다."),

    /**
     * 댓글에 대댓글 등록
     */
    REPLY_CREATED("REPLY_CREATED", "게시판", "내 댓글에 답글이 등록되었습니다."),

    // ========== 성적 관련 ==========
    /**
     * 성적 등록/업데이트
     */
    GRADE_UPDATED("GRADE_UPDATED", "성적", "성적이 업데이트되었습니다."),

    /**
     * 최종 성적 확정
     */
    GRADE_FINALIZED("GRADE_FINALIZED", "성적", "최종 성적이 확정되었습니다."),

    // ========== 시스템 관련 ==========
    /**
     * 비밀번호 변경
     */
    PASSWORD_CHANGED("PASSWORD_CHANGED", "시스템", "비밀번호가 변경되었습니다."),

    /**
     * 프로필 변경
     */
    PROFILE_UPDATED("PROFILE_UPDATED", "시스템", "프로필이 업데이트되었습니다.");

    /**
     * 알림 타입 코드 (notification_types.type_code와 매핑)
     */
    private final String typeCode;

    /**
     * 알림 카테고리
     */
    private final String category;

    /**
     * 기본 메시지 템플릿
     */
    private final String defaultMessage;

    /**
     * 타입 코드로 이벤트 타입 조회
     */
    public static NotificationEventType fromTypeCode(String typeCode) {
        for (NotificationEventType type : values()) {
            if (type.getTypeCode().equals(typeCode)) {
                return type;
            }
        }
        throw new IllegalArgumentException("알 수 없는 알림 이벤트 타입: " + typeCode);
    }
}
