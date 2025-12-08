package com.mzc.backend.lms.domains.board.enums;

/**
 * 게시판 유형 Enum
 * LMS 시스템의 13가지 게시판 유형을 정의
 */
public enum BoardType {
    // 기본 게시판 (5개)
    NOTICE("학교 공지사항"),
    FREE("자유 게시판"), 
    QUESTION("질문 게시판"),
    DISCUSSION("토론 게시판"),
    DEPARTMENT("학과 게시판"),
    
    // 역할별 제한 게시판 (2개)
    PROFESSOR("교수 게시판"),
    STUDENT("학생 게시판"),
    
    // 특수 목적 게시판 (2개)
    CONTEST("공모전 게시판"),
    CAREER("취업 게시판"),
    
    // 학습관리 시스템 (4개)
    ASSIGNMENT("과제 게시판"),
    EXAM("시험 게시판"),
    QUIZ("퀴즈 게시판"),
    STUDY_RECRUITMENT("스터디모집 게시판");
    
    private final String description;
    
    BoardType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 기본 게시판 여부 확인
     */
    public boolean isBasicBoard() {
        return this == NOTICE || this == FREE || this == QUESTION || 
               this == DISCUSSION || this == DEPARTMENT;
    }
    
    /**
     * 역할 제한 게시판 여부 확인
     */
    public boolean isRoleRestrictedBoard() {
        return this == PROFESSOR || this == STUDENT;
    }
    
    /**
     * 학습관리 게시판 여부 확인
     */
    public boolean isLearningManagementBoard() {
        return this == ASSIGNMENT || this == EXAM || this == QUIZ || this == STUDY_RECRUITMENT;
    }
}