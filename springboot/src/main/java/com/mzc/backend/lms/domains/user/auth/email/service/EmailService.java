package com.mzc.backend.lms.domains.user.auth.email.service;

import com.mzc.backend.lms.domains.user.auth.email.dto.EmailMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 이메일 발송 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final EmailSenderService emailSenderService;

    /**
     * 인증 코드 이메일 발송
     */
    public void sendVerificationCode(String toEmail, String verificationCode) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("code", verificationCode);
        variables.put("expirationMinutes", "5");

        EmailMessage emailMessage = EmailMessage.builder()
            .to(toEmail)
            .subject("[MZC LMS] 이메일 인증 코드")
            .templateName("verification-email")
            .emailType(EmailMessage.EmailType.VERIFICATION)
            .variables(variables)
            .build();

        try {
            emailSenderService.sendEmail(emailMessage);
            log.info("인증 코드 이메일 발송 완료: {}", toEmail);
        } catch (Exception e) {
            log.error("이메일 발송 실패: {}", toEmail, e);
            throw new RuntimeException("이메일 발송에 실패했습니다.", e);
        }
    }

    /**
     * 회원가입 완료 이메일 발송
     */
    public void sendWelcomeEmail(String toEmail, String name, String studentNumber) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("userName", name);
        variables.put("userNumber", studentNumber);

        EmailMessage emailMessage = EmailMessage.builder()
            .to(toEmail)
            .subject("[MZC LMS] 회원가입을 환영합니다!")
            .templateName("welcome-email")
            .emailType(EmailMessage.EmailType.WELCOME)
            .variables(variables)
            .build();

        try {
            emailSenderService.sendEmail(emailMessage);
            log.info("환영 이메일 발송 완료: {}", toEmail);
        } catch (Exception e) {
            log.error("환영 이메일 발송 실패: {}", toEmail, e);
            // 환영 이메일은 실패해도 회원가입은 진행
        }
    }
}