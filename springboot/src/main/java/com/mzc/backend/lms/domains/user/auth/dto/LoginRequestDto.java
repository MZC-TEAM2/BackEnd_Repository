package com.mzc.backend.lms.domains.user.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 로그인 요청 DTO
 */
@Data
public class LoginRequestDto {

    @NotBlank(message = "아이디는 필수입니다")
    private String username;  // 이메일 또는 학번/교번

    @NotBlank(message = "비밀번호는 필수입니다")
    private String password;

    private String deviceInfo;  // 디바이스 정보 (옵션)
    private String ipAddress;   // IP 주소 (서버에서 설정)

    /**
     * 이메일 형식인지 확인
     */
    public boolean isEmailFormat() {
        return username != null && username.contains("@");
    }
}