package com.mzc.backend.lms.domains.dashboard.student.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 로그인 성공 이벤트
 * 로그인 성공 시 발행되어 오늘의 수업 알림 발송에 사용됨
 */
@Getter
@AllArgsConstructor
public class LoginSuccessEvent {

    /**
     * 로그인한 사용자 ID
     */
    private final Long userId;

    /**
     * 사용자 타입 (STUDENT, PROFESSOR)
     */
    private final String userType;
}
