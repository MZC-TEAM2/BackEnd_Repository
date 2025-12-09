package com.mzc.backend.lms.domains.board.enums;

/**
 * 게시글 유형 Enum
 * 게시글의 성격을 구분하는 용도
 */
public enum PostType {
    NORMAL("일반 게시글"),
    NOTICE("공지사항"),
    PINNED("고정 게시글"),
    URGENT("긴급 공지"),
    FAQ("자주묻는질문"),
    
    // 학습관리 시스템 전용
    ASSIGNMENT("과제"),
    EXAM("시험"),
    QUIZ("퀴즈"),
    STUDY_RECRUITMENT("스터디모집");
    
    private final String description;
    
    PostType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 관리자 전용 포스트 타입 여부 확인
     */
    public boolean isAdminOnly() {
        return this == NOTICE || this == PINNED || this == URGENT;
    }
    
    /**
     * 학습관리 포스트 타입 여부 확인
     */
    public boolean isLearningManagement() {
        return this == ASSIGNMENT || this == EXAM || this == QUIZ || this == STUDY_RECRUITMENT;
    }
}