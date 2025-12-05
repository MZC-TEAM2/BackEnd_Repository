package com.mzc.backend.lms.domains.user.auth.controller;

import com.mzc.backend.lms.domains.user.auth.dto.*;
import com.mzc.backend.lms.domains.user.auth.email.service.EmailVerificationService;
import com.mzc.backend.lms.domains.user.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 인증 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final EmailVerificationService emailVerificationService;

    /**
     * 이메일 인증 코드 발송
     */
    @PostMapping("/signup/email-verification")
    public ResponseEntity<?> sendVerificationCode(@Valid @RequestBody EmailVerificationRequestDto dto) {
        try {
            // 이메일 중복 확인
            if (!authService.isEmailAvailable(dto.getEmail())) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("이미 가입된 이메일입니다."));
            }

            emailVerificationService.sendVerificationCode(dto.getEmail());

            return ResponseEntity.ok(createSuccessResponse("인증 코드가 발송되었습니다."));
        } catch (Exception e) {
            log.error("이메일 인증 코드 발송 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("인증 코드 발송에 실패했습니다."));
        }
    }

    /**
     * 인증 코드 확인
     */
    @PostMapping("/signup/verify-code")
    public ResponseEntity<?> verifyCode(@Valid @RequestBody VerifyCodeRequestDto dto) {
        try {
            boolean verified = emailVerificationService.verifyCode(dto.getEmail(), dto.getCode());

            if (verified) {
                return ResponseEntity.ok(createSuccessResponse("이메일 인증이 완료되었습니다."));
            } else {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("인증 코드가 올바르지 않거나 만료되었습니다."));
            }
        } catch (Exception e) {
            log.error("인증 코드 확인 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("인증 코드 확인에 실패했습니다."));
        }
    }

    /**
     * 회원가입
     */
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequestDto dto) {
        try {
            String userId = authService.signup(dto);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "회원가입이 완료되었습니다.");
            response.put("userId", userId);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("회원가입 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("회원가입에 실패했습니다."));
        }
    }

    /**
     * 로그인
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDto dto,
                                  HttpServletRequest request) {
        try {
            // IP 주소 추출
            String ipAddress = getClientIpAddress(request);
            dto.setIpAddress(ipAddress);

            LoginResponseDto response = authService.login(dto, ipAddress);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("로그인 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("로그인에 실패했습니다."));
        }
    }

    /**
     * 토큰 갱신
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequestDto dto) {
        try {
            RefreshTokenResponseDto response = authService.refreshToken(dto);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("토큰 갱신 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("토큰 갱신에 실패했습니다."));
        }
    }

    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Refresh-Token", required = false) String refreshToken) {
        try {
            if (refreshToken != null && !refreshToken.isEmpty()) {
                authService.logout(refreshToken);
            }

            return ResponseEntity.ok(createSuccessResponse("로그아웃되었습니다."));
        } catch (Exception e) {
            log.error("로그아웃 실패: {}", e.getMessage());
            // 로그아웃은 실패해도 성공으로 처리
            return ResponseEntity.ok(createSuccessResponse("로그아웃되었습니다."));
        }
    }

    /**
     * 이메일 중복 확인
     */
    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmailAvailability(@RequestParam String email) {
        try {
            boolean available = authService.isEmailAvailable(email);

            Map<String, Object> response = new HashMap<>();
            response.put("available", available);
            response.put("message", available ? "사용 가능한 이메일입니다." : "이미 사용 중인 이메일입니다.");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("이메일 중복 확인 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("이메일 확인에 실패했습니다."));
        }
    }

    /**
     * 클라이언트 IP 주소 추출
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headers = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR",
            "X-Real-IP"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For는 여러 IP가 콤마로 구분될 수 있음
                if (ip.contains(",")) {
                    return ip.split(",")[0].trim();
                }
                return ip;
            }
        }

        return request.getRemoteAddr();
    }

    /**
     * 성공 응답 생성
     */
    private Map<String, Object> createSuccessResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        return response;
    }

    /**
     * 에러 응답 생성
     */
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return response;
    }
}
