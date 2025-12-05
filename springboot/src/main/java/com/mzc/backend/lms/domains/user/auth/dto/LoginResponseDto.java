package com.mzc.backend.lms.domains.user.auth.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 로그인 응답 DTO
 */
@Data
@Builder
public class LoginResponseDto {

    private String accessToken;
    private String refreshToken;
    private String userType;      // STUDENT or PROFESSOR
    private String userNumber;    // 학번 또는 교번
    private String name;
    private String email;
    private String userId;        // 학번 또는 교번 (User의 PK)

    /**
     * 토큰 정보만 포함한 응답 생성
     */
    public static LoginResponseDto ofTokens(String accessToken, String refreshToken) {
        return LoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * 전체 정보를 포함한 응답 생성
     */
    public static LoginResponseDto of(String accessToken, String refreshToken,
                                     String userType, String userNumber,
                                     String name, String email, String userId) {
        return LoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userType(userType)
                .userNumber(userNumber)
                .name(name)
                .email(email)
                .userId(userId)
                .build();
    }
}