package com.mzc.backend.lms.domains.user.auth.encryption

import com.mzc.backend.lms.domains.user.auth.encryption.service.EncryptionService
import com.mzc.backend.lms.domains.user.auth.encryption.strategy.AES256EncryptionStrategy
import com.mzc.backend.lms.domains.user.auth.encryption.strategy.BCryptEncryptionStrategy
import spock.lang.Specification
import spock.lang.Subject

/**
 * 암호화 서비스 Spock 테스트
 * BDD 스타일의 given-when-then 블록과 data-driven 테스트 예시
 */
class EncryptionServiceSpec extends Specification {

    def aes256Strategy = Mock(AES256EncryptionStrategy)
    def bcryptStrategy = Mock(BCryptEncryptionStrategy)

    @Subject
    def encryptionService = new EncryptionService(aes256Strategy, bcryptStrategy)

    def "이메일 암호화 시 AES256 전략을 사용한다"() {
        given: "암호화할 이메일"
        def email = "test@example.com"
        def encryptedEmail = "encrypted_email"

        when: "이메일을 암호화하면"
        def result = encryptionService.encryptEmail(email)

        then: "AES256 전략이 호출되고 암호화된 결과를 반환한다"
        1 * aes256Strategy.encrypt(email) >> encryptedEmail
        result == encryptedEmail
    }

    def "이메일 복호화 시 AES256 전략을 사용한다"() {
        given: "암호화된 이메일"
        def encryptedEmail = "encrypted_email"
        def email = "test@example.com"

        when: "이메일을 복호화하면"
        def result = encryptionService.decryptEmail(encryptedEmail)

        then: "AES256 전략이 호출되고 복호화된 결과를 반환한다"
        1 * aes256Strategy.decrypt(encryptedEmail) >> email
        result == email
    }

    def "비밀번호 암호화 시 BCrypt 전략을 사용한다"() {
        given: "암호화할 비밀번호"
        def password = "password123"
        def hashedPassword = "hashed_password"

        when: "비밀번호를 암호화하면"
        def result = encryptionService.encryptPassword(password)

        then: "BCrypt 전략이 호출되고 해시된 결과를 반환한다"
        1 * bcryptStrategy.encrypt(password) >> hashedPassword
        result == hashedPassword
    }

    def "비밀번호 매칭 성공 테스트"() {
        given: "올바른 비밀번호"
        def plainPassword = "correct_password"
        def hashedPassword = "hashed_password"

        when: "비밀번호를 검증하면"
        def result = encryptionService.matchesPassword(plainPassword, hashedPassword)

        then: "true를 반환한다"
        1 * bcryptStrategy.matches(plainPassword, hashedPassword) >> true
        result == true
    }

    def "비밀번호 매칭 실패 테스트"() {
        given: "잘못된 비밀번호"
        def plainPassword = "wrong_password"
        def hashedPassword = "hashed_password"

        when: "비밀번호를 검증하면"
        def result = encryptionService.matchesPassword(plainPassword, hashedPassword)

        then: "false를 반환한다"
        1 * bcryptStrategy.matches(plainPassword, hashedPassword) >> false
        result == false
    }

    def "PASSWORD 타입에 대해 BCrypt 전략을 반환한다"() {
        when: "PASSWORD 타입으로 전략을 조회하면"
        def strategy = encryptionService.getStrategy(EncryptionService.DataType.PASSWORD)

        then: "BCrypt 전략을 반환한다"
        strategy == bcryptStrategy
    }

    def "EMAIL 타입에 대해 AES256 전략을 반환한다"() {
        when: "EMAIL 타입으로 전략을 조회하면"
        def strategy = encryptionService.getStrategy(EncryptionService.DataType.EMAIL)

        then: "AES256 전략을 반환한다"
        strategy == aes256Strategy
    }

    def "PHONE_NUMBER 타입에 대해 AES256 전략을 반환한다"() {
        when: "PHONE_NUMBER 타입으로 전략을 조회하면"
        def strategy = encryptionService.getStrategy(EncryptionService.DataType.PHONE_NUMBER)

        then: "AES256 전략을 반환한다"
        strategy == aes256Strategy
    }

    def "전화번호 암호화 테스트"() {
        given: "전화번호"
        def phoneNumber = "010-1234-5678"
        def encryptedPhone = "encrypted_phone"

        when: "전화번호를 암호화하면"
        def result = encryptionService.encryptPhoneNumber(phoneNumber)

        then: "암호화된 값을 반환한다"
        1 * aes256Strategy.encrypt(phoneNumber) >> encryptedPhone
        result == encryptedPhone
    }

    def "전화번호 복호화 테스트"() {
        given: "암호화된 전화번호"
        def encryptedPhone = "encrypted_phone"
        def phoneNumber = "010-1234-5678"

        when: "전화번호를 복호화하면"
        def result = encryptionService.decryptPhoneNumber(encryptedPhone)

        then: "원래 전화번호를 반환한다"
        1 * aes256Strategy.decrypt(encryptedPhone) >> phoneNumber
        result == phoneNumber
    }

    def "개인정보 암호화 테스트"() {
        given: "개인정보"
        def personalInfo = "personal_info"
        def encryptedInfo = "encrypted_personal_info"

        when: "개인정보를 암호화하면"
        def result = encryptionService.encryptPersonalInfo(personalInfo)

        then: "암호화된 값을 반환한다"
        1 * aes256Strategy.encrypt(personalInfo) >> encryptedInfo
        result == encryptedInfo
    }

    def "개인정보 복호화 테스트"() {
        given: "암호화된 개인정보"
        def encryptedInfo = "encrypted_personal_info"
        def personalInfo = "personal_info"

        when: "개인정보를 복호화하면"
        def result = encryptionService.decryptPersonalInfo(encryptedInfo)

        then: "원래 개인정보를 반환한다"
        1 * aes256Strategy.decrypt(encryptedInfo) >> personalInfo
        result == personalInfo
    }
}
