package com.mzc.backend.lms.domains.user.auth.email.service

import com.mzc.backend.lms.domains.user.auth.email.dto.EmailMessage
import jakarta.mail.internet.MimeMessage
import org.springframework.mail.javamail.JavaMailSender
import spock.lang.Specification
import spock.lang.Subject

import java.lang.reflect.Field

/**
 * EmailSenderService 테스트
 * 이메일 발송 기능 테스트
 */
class EmailSenderServiceSpec extends Specification {

    def mailSender = Mock(JavaMailSender)
    def mimeMessage = Mock(MimeMessage)

    @Subject
    EmailSenderService emailSenderService

    def setup() {
        emailSenderService = new EmailSenderService(mailSender)
        setField(emailSenderService, "fromEmail", "noreply@lms.example.com")
        setField(emailSenderService, "fromName", "LMS System")
        mailSender.createMimeMessage() >> mimeMessage
    }

    private void setField(Object target, String fieldName, Object value) {
        Field field = target.class.getDeclaredField(fieldName)
        field.setAccessible(true)
        field.set(target, value)
    }

    def "이메일을 성공적으로 발송한다"() {
        given: "이메일 메시지"
        def emailMessage = EmailMessage.builder()
                .to("test@example.com")
                .subject("테스트 이메일")
                .content("테스트 내용입니다.")
                .html(false)
                .build()

        when: "이메일을 발송하면"
        emailSenderService.sendEmail(emailMessage)

        then: "메일이 발송된다"
        1 * mailSender.send(mimeMessage)
    }

    def "HTML 형식의 이메일을 발송한다"() {
        given: "HTML 이메일 메시지"
        def emailMessage = EmailMessage.builder()
                .to("test@example.com")
                .subject("HTML 이메일")
                .content("<h1>Hello</h1><p>HTML 내용입니다.</p>")
                .html(true)
                .build()

        when: "이메일을 발송하면"
        emailSenderService.sendEmail(emailMessage)

        then: "메일이 발송된다"
        1 * mailSender.send(mimeMessage)
    }

    def "템플릿 기반 이메일을 발송한다"() {
        given: "템플릿 이메일 메시지"
        def variables = ["code": "ABC12", "expirationMinutes": "5"]
        def emailMessage = EmailMessage.builder()
                .to("test@example.com")
                .subject("인증 코드")
                .templateName("verification")
                .variables(variables)
                .html(true)
                .build()

        when: "이메일을 발송하면"
        emailSenderService.sendEmail(emailMessage)

        then: "메일이 발송된다"
        1 * mailSender.send(mimeMessage)
    }

    def "변수 없이 기본 템플릿으로 이메일을 발송한다"() {
        given: "변수 없는 이메일 메시지"
        def emailMessage = EmailMessage.builder()
                .to("test@example.com")
                .subject("기본 이메일")
                .html(true)
                .build()

        when: "이메일을 발송하면"
        emailSenderService.sendEmail(emailMessage)

        then: "메일이 발송된다"
        1 * mailSender.send(mimeMessage)
    }

    def "회원가입 환영 이메일을 발송한다"() {
        given: "환영 이메일 메시지"
        def variables = ["userName": "홍길동", "userNumber": "2025010001"]
        def emailMessage = EmailMessage.builder()
                .to("student@example.com")
                .subject("회원가입을 환영합니다")
                .templateName("welcome")
                .variables(variables)
                .emailType(EmailMessage.EmailType.WELCOME)
                .html(true)
                .build()

        when: "이메일을 발송하면"
        emailSenderService.sendEmail(emailMessage)

        then: "메일이 발송된다"
        1 * mailSender.send(mimeMessage)
    }

    def "비밀번호 재설정 이메일을 발송한다"() {
        given: "비밀번호 재설정 이메일 메시지"
        def variables = ["resetToken": "RESET123", "expirationMinutes": "30"]
        def emailMessage = EmailMessage.builder()
                .to("user@example.com")
                .subject("비밀번호 재설정")
                .templateName("password-reset")
                .variables(variables)
                .emailType(EmailMessage.EmailType.PASSWORD_RESET)
                .html(true)
                .build()

        when: "이메일을 발송하면"
        emailSenderService.sendEmail(emailMessage)

        then: "메일이 발송된다"
        1 * mailSender.send(mimeMessage)
    }

    def "이메일 발송 실패 시 RuntimeException이 발생한다"() {
        given: "이메일 메시지"
        def emailMessage = EmailMessage.builder()
                .to("test@example.com")
                .subject("테스트")
                .content("내용")
                .build()

        mailSender.send(mimeMessage) >> { throw new RuntimeException("SMTP 서버 오류") }

        when: "이메일 발송이 실패하면"
        emailSenderService.sendEmail(emailMessage)

        then: "RuntimeException이 발생한다"
        def exception = thrown(RuntimeException)
        exception.message == "이메일 전송 실패"
    }

    def "다양한 이메일 타입으로 발송할 수 있다"() {
        given: "특정 타입의 이메일 메시지"
        def emailMessage = EmailMessage.builder()
                .to("test@example.com")
                .subject("알림")
                .content("알림 내용입니다.")
                .emailType(emailType)
                .build()

        when: "이메일을 발송하면"
        emailSenderService.sendEmail(emailMessage)

        then: "메일이 발송된다"
        1 * mailSender.send(mimeMessage)

        where:
        emailType << [
                EmailMessage.EmailType.VERIFICATION,
                EmailMessage.EmailType.NOTIFICATION,
                EmailMessage.EmailType.ANNOUNCEMENT,
                EmailMessage.EmailType.COURSE_UPDATE,
                EmailMessage.EmailType.ASSIGNMENT,
                EmailMessage.EmailType.GRADE
        ]
    }
}
