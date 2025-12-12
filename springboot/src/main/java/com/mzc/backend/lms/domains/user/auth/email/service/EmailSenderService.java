package com.mzc.backend.lms.domains.user.auth.email.service;

import com.mzc.backend.lms.domains.user.auth.email.dto.EmailMessage;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 * 이메일 발송 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailSenderService {

    private final JavaMailSender mailSender;

    @Value("${email.from}")
    private String fromEmail;

    @Value("${email.from-name}")
    private String fromName;

    /**
     * 이메일 비동기 전송
     *
     * @param emailMessage 이메일 메시지
     */
    @Async("emailExecutor")
    public void sendEmail(EmailMessage emailMessage) {
        try {
            MimeMessage mimeMessage = createMimeMessage(emailMessage);
            mailSender.send(mimeMessage);
            log.info("이메일 전송 완료: to={}, subject={}",
                emailMessage.getTo(), emailMessage.getSubject());
        } catch (Exception e) {
            log.error("이메일 전송 실패: to={}, error={}",
                emailMessage.getTo(), e.getMessage(), e);
            throw new RuntimeException("이메일 전송 실패", e);
        }
    }

    /**
     * MimeMessage 생성
     *
     * @param emailMessage 이메일 메시지
     * @return MimeMessage
     * @throws MessagingException 메시지 생성 실패 시
     */
    private MimeMessage createMimeMessage(EmailMessage emailMessage) throws MessagingException, UnsupportedEncodingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(
            mimeMessage,
            true,
            StandardCharsets.UTF_8.name()
        );

        // 발신자 설정 (환경변수에서 읽어온 값 사용)
        helper.setFrom(fromEmail, fromName);

        // 수신자 설정
        helper.setTo(emailMessage.getTo());

        // 제목 설정
        helper.setSubject(emailMessage.getSubject());

        // 본문 설정
        String content = getEmailContent(emailMessage);
        helper.setText(content, emailMessage.isHtml());

        return mimeMessage;
    }

    /**
     * 이메일 본문 내용 생성
     *
     * @param emailMessage 이메일 메시지
     * @return 본문 내용
     */
    private String getEmailContent(EmailMessage emailMessage) {
        // 템플릿 사용하는 경우
        if (emailMessage.getTemplateName() != null && !emailMessage.getTemplateName().isEmpty()) {
            return processTemplate(emailMessage.getTemplateName(), emailMessage.getVariables());
        }

        // 직접 작성한 콘텐츠 사용
        if (emailMessage.getContent() != null && !emailMessage.getContent().isEmpty()) {
            return emailMessage.getContent();
        }

        // 기본 템플릿 처리
        return generateDefaultContent(emailMessage);
    }

    /**
     * 템플릿 처리 (간단한 HTML 생성)
     *
     * @param templateName 템플릿 이름
     * @param variables 템플릿 변수
     * @return 처리된 HTML
     */
    private String processTemplate(String templateName, java.util.Map<String, Object> variables) {
        // 템플릿 엔진 없이 직접 HTML 생성
        return generateDefaultTemplate(variables);
    }

    /**
     * 기본 템플릿 생성
     *
     * @param variables 템플릿 변수
     * @return HTML 콘텐츠
     */
    private String generateDefaultTemplate(java.util.Map<String, Object> variables) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<style>");
        html.append("body { font-family: 'Noto Sans KR', sans-serif; line-height: 1.6; color: #333; }");
        html.append(".container { max-width: 600px; margin: 0 auto; padding: 20px; }");
        html.append(".header { background: #007bff; color: white; padding: 20px; text-align: center; }");
        html.append(".content { padding: 20px; background: #f9f9f9; }");
        html.append(".footer { text-align: center; padding: 10px; color: #666; font-size: 12px; }");
        html.append(".code { font-size: 24px; font-weight: bold; color: #007bff; text-align: center; padding: 20px; background: white; border-radius: 5px; margin: 20px 0; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        html.append("<div class='container'>");
        html.append("<div class='header'><h1>LMS System</h1></div>");
        html.append("<div class='content'>");

        // 변수에 따른 내용 구성
        if (variables != null) {
            if (variables.containsKey("code")) {
                html.append("<h2>이메일 인증</h2>");
                html.append("<p>아래 인증 코드를 입력해주세요:</p>");
                html.append("<div class='code'>").append(variables.get("code")).append("</div>");
                html.append("<p>이 코드는 ").append(variables.getOrDefault("expirationMinutes", "5"))
                    .append("분 동안 유효합니다.</p>");
            } else if (variables.containsKey("userName")) {
                html.append("<h2>환영합니다, ").append(variables.get("userName")).append("님!</h2>");
                html.append("<p>LMS 시스템에 가입해주셔서 감사합니다.</p>");
                if (variables.containsKey("userNumber")) {
                    html.append("<p>귀하의 학번/교번: <strong>").append(variables.get("userNumber"))
                        .append("</strong></p>");
                }
            } else if (variables.containsKey("resetToken")) {
                html.append("<h2>비밀번호 재설정</h2>");
                html.append("<p>비밀번호를 재설정하려면 아래 토큰을 사용해주세요:</p>");
                html.append("<div class='code'>").append(variables.get("resetToken")).append("</div>");
                html.append("<p>이 토큰은 ").append(variables.getOrDefault("expirationMinutes", "30"))
                    .append("분 동안 유효합니다.</p>");
            }
        }

        html.append("</div>");
        html.append("<div class='footer'>");
        html.append("<p>이 메일은 LMS System에서 자동으로 발송되었습니다.</p>");
        html.append("<p>문의사항이 있으시면 관리자에게 연락해주세요.</p>");
        html.append("</div>");
        html.append("</div>");
        html.append("</body>");
        html.append("</html>");

        return html.toString();
    }

    /**
     * 기본 콘텐츠 생성
     *
     * @param emailMessage 이메일 메시지
     * @return 기본 콘텐츠
     */
    private String generateDefaultContent(EmailMessage emailMessage) {
        return generateDefaultTemplate(emailMessage.getVariables());
    }
}
