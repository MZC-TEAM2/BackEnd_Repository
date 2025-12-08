package com.mzc.backend.lms.domains.user.auth.email.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 이메일 메시지 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailMessage {

    /**
     * 수신자 이메일 주소
     */
    private String to;

    /**
     * 이메일 제목
     */
    private String subject;

    /**
     * 이메일 본문 (HTML 또는 Plain Text)
     */
    private String content;

    /**
     * HTML 형식 여부
     */
    @Builder.Default
    private boolean html = true;

    /**
     * 이메일 템플릿 이름
     */
    private String templateName;

    /**
     * 템플릿 변수
     */
    @Builder.Default
    private Map<String, Object> variables = new HashMap<>();

    /**
     * 이메일 타입
     */
    private EmailType emailType;


    /**
     * 이메일 타입 열거형
     */
    public enum EmailType {
        VERIFICATION,      // 인증 코드
        WELCOME,          // 회원가입 환영
        PASSWORD_RESET,   // 비밀번호 재설정
        NOTIFICATION,     // 일반 알림
        ANNOUNCEMENT,     // 공지사항
        COURSE_UPDATE,    // 강좌 업데이트
        ASSIGNMENT,       // 과제 관련
        GRADE             // 성적 관련
    }
}