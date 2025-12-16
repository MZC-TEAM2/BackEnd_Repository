package com.mzc.backend.lms.domains.board.enums;

import java.util.List;

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
    
    /**
     * 해당 게시판에서 허용되는 게시글 유형 목록 반환
     * 
     * @return 허용되는 PostType 리스트
     */
    public List<PostType> getAllowedPostTypes() {
        switch (this) {
            case NOTICE:
                // 학교 공지사항: 공지, 긴급만
                return List.of(PostType.NOTICE, PostType.URGENT);
                
            case FREE:
                // 자유 게시판: 일반, 긴급
                return List.of(PostType.NORMAL, PostType.URGENT);
                
            case QUESTION:
            case DISCUSSION:
            case STUDENT:
            case CONTEST:
            case CAREER:
                // 질문, 토론, 학생, 공모전, 취업: 일반만
                return List.of(PostType.NORMAL);
                
            case DEPARTMENT:
                // 학과 게시판: 일반, 공지, 긴급
                return List.of(PostType.NORMAL, PostType.NOTICE, PostType.URGENT);
                
            case PROFESSOR:
                // 교수 게시판: 일반, 공지
                return List.of(PostType.NORMAL, PostType.NOTICE);
                
            case ASSIGNMENT:
                // 과제 게시판: 과제 전용
                return List.of(PostType.ASSIGNMENT);
                
            case EXAM:
                // 시험 게시판: 시험 전용
                return List.of(PostType.EXAM);
                
            case QUIZ:
                // 퀴즈 게시판: 퀴즈 전용
                return List.of(PostType.QUIZ);
                
            case STUDY_RECRUITMENT:
                // 스터디모집 게시판: 스터디모집 전용
                return List.of(PostType.STUDY_RECRUITMENT);
                
            default:
                // 기본값: 일반만
                return List.of(PostType.NORMAL);
        }
    }
    
    /**
     * 특정 게시글 유형이 이 게시판에서 허용되는지 확인
     * 
     * @param postType 확인할 게시글 유형
     * @return 허용 여부
     */
    public boolean isPostTypeAllowed(PostType postType) {
        return getAllowedPostTypes().contains(postType);
    }
}