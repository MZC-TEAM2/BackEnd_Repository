package com.mzc.backend.lms.domains.user.auth.email.service

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations
import spock.lang.Specification
import spock.lang.Subject

import java.lang.reflect.Field
import java.util.concurrent.TimeUnit

/**
 * EmailVerificationService 테스트
 * 이메일 인증 코드 발송, 검증, 상태 확인 기능 테스트
 */
class EmailVerificationServiceSpec extends Specification {

    def redisTemplate = Mock(RedisTemplate)
    def valueOperations = Mock(ValueOperations)
    def emailService = Mock(EmailService)

    @Subject
    EmailVerificationService emailVerificationService

    def setup() {
        emailVerificationService = new EmailVerificationService(redisTemplate, emailService)
        setField(emailVerificationService, "emailVerificationEnabled", true)
        redisTemplate.opsForValue() >> valueOperations
    }

    private void setField(Object target, String fieldName, Object value) {
        Field field = target.class.getDeclaredField(fieldName)
        field.setAccessible(true)
        field.set(target, value)
    }

    def "인증 코드를 발송하면 Redis에 저장하고 이메일을 발송한다"() {
        given: "이메일 주소"
        def email = "test@example.com"

        when: "인증 코드 발송을 요청하면"
        emailVerificationService.sendVerificationCode(email)

        then: "Redis에 코드가 저장된다"
        1 * valueOperations.set({ it.startsWith("email:code:") }, _, 5, TimeUnit.MINUTES)

        and: "이메일이 발송된다"
        1 * emailService.sendVerificationCode(email, _)
    }

    def "이메일 발송이 비활성화된 경우 Redis 저장만 수행한다"() {
        given: "이메일 발송 비활성화 상태"
        setField(emailVerificationService, "emailVerificationEnabled", false)
        def email = "test@example.com"

        when: "인증 코드 발송을 요청하면"
        emailVerificationService.sendVerificationCode(email)

        then: "Redis에 코드가 저장된다"
        1 * valueOperations.set({ it.startsWith("email:code:") }, _, 5, TimeUnit.MINUTES)

        and: "이메일은 발송되지 않는다"
        0 * emailService.sendVerificationCode(_, _)
    }

    def "마스터 코드로 인증 시 즉시 성공한다"() {
        given: "이메일과 마스터 코드"
        def email = "test@example.com"
        def masterCode = "dding"

        when: "마스터 코드로 인증하면"
        def result = emailVerificationService.verifyCode(email, masterCode)

        then: "인증 완료로 표시된다"
        1 * valueOperations.set("email:verified:" + email, "true", 30, TimeUnit.MINUTES)

        and: "true를 반환한다"
        result
    }

    def "올바른 인증 코드로 검증 시 성공한다"() {
        given: "이메일과 저장된 인증 코드"
        def email = "test@example.com"
        def code = "ABC12"
        valueOperations.get("email:code:" + email) >> code

        when: "올바른 코드로 인증하면"
        def result = emailVerificationService.verifyCode(email, code)

        then: "인증 코드가 삭제된다"
        1 * redisTemplate.delete("email:code:" + email)

        and: "인증 완료로 표시된다"
        1 * valueOperations.set("email:verified:" + email, "true", 30, TimeUnit.MINUTES)

        and: "true를 반환한다"
        result
    }

    def "잘못된 인증 코드로 검증 시 실패한다"() {
        given: "이메일과 다른 인증 코드"
        def email = "test@example.com"
        def storedCode = "ABC12"
        def wrongCode = "WRONG"
        valueOperations.get("email:code:" + email) >> storedCode

        when: "잘못된 코드로 인증하면"
        def result = emailVerificationService.verifyCode(email, wrongCode)

        then: "인증 코드가 삭제되지 않는다"
        0 * redisTemplate.delete(_)

        and: "인증 완료 표시가 되지 않는다"
        0 * valueOperations.set({ it.startsWith("email:verified:") }, _, _, _)

        and: "false를 반환한다"
        !result
    }

    def "저장된 인증 코드가 없으면 검증 실패한다"() {
        given: "인증 코드가 없는 이메일"
        def email = "test@example.com"
        valueOperations.get("email:code:" + email) >> null

        when: "인증을 시도하면"
        def result = emailVerificationService.verifyCode(email, "ANYCODE")

        then: "false를 반환한다"
        !result
    }

    def "이메일 인증 완료 여부를 확인한다 - 인증된 경우"() {
        given: "인증 완료된 이메일"
        def email = "verified@example.com"
        redisTemplate.hasKey("email:verified:" + email) >> true

        when: "인증 완료 여부를 확인하면"
        def result = emailVerificationService.isEmailVerified(email)

        then: "true를 반환한다"
        result
    }

    def "이메일 인증 완료 여부를 확인한다 - 미인증 경우"() {
        given: "미인증 이메일"
        def email = "unverified@example.com"
        redisTemplate.hasKey("email:verified:" + email) >> false

        when: "인증 완료 여부를 확인하면"
        def result = emailVerificationService.isEmailVerified(email)

        then: "false를 반환한다"
        !result
    }

    def "인증 정보를 초기화한다"() {
        given: "이메일"
        def email = "test@example.com"

        when: "인증 정보를 초기화하면"
        emailVerificationService.clearVerification(email)

        then: "인증 코드가 삭제된다"
        1 * redisTemplate.delete("email:code:" + email)

        and: "인증 완료 정보가 삭제된다"
        1 * redisTemplate.delete("email:verified:" + email)
    }
}
