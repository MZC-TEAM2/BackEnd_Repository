package com.mzc.backend.lms.domains.user.auth.swagger;

import com.mzc.backend.lms.domains.user.auth.dto.EmailVerificationRequestDto;
import com.mzc.backend.lms.domains.user.auth.dto.LoginRequestDto;
import com.mzc.backend.lms.domains.user.auth.dto.RefreshTokenRequestDto;
import com.mzc.backend.lms.domains.user.auth.dto.SignupRequestDto;
import com.mzc.backend.lms.domains.user.auth.dto.VerifyCodeRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

@Tag(name = "Auth", description = "인증 API")
public interface AuthControllerSwagger {

    @Operation(summary = "이메일 인증 코드 발송", description = "회원가입을 위한 이메일 인증 코드를 발송합니다")
    ResponseEntity<?> sendVerificationCode(EmailVerificationRequestDto dto);

    @Operation(summary = "인증 코드 확인", description = "발송된 이메일 인증 코드를 확인합니다")
    ResponseEntity<?> verifyCode(VerifyCodeRequestDto dto);

    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다")
    ResponseEntity<?> signup(SignupRequestDto dto);

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인합니다")
    ResponseEntity<?> login(LoginRequestDto dto, HttpServletRequest request);

    @Operation(summary = "토큰 갱신", description = "Refresh Token을 사용하여 Access Token을 갱신합니다")
    ResponseEntity<?> refreshToken(RefreshTokenRequestDto dto);

    @Operation(summary = "로그아웃", description = "현재 세션을 로그아웃합니다")
    ResponseEntity<?> logout(
            @Parameter(description = "Refresh Token") String refreshToken);

    @Operation(summary = "이메일 중복 확인", description = "이메일 사용 가능 여부를 확인합니다")
    ResponseEntity<?> checkEmailAvailability(
            @Parameter(description = "확인할 이메일 주소") String email);
}
